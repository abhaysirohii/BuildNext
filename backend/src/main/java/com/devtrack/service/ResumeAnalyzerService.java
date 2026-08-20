package com.devtrack.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.devtrack.dto.AtsScoreResponse;
import com.devtrack.dto.DescriptionScoreResponse;
import com.devtrack.dto.FullResumeAnalysisResponse;
import com.devtrack.dto.RedFlagAnalyzeResponse;
import com.devtrack.dto.RedFlagHit;
import com.devtrack.dto.ScoreBreakdownItem;
import com.devtrack.exception.BadRequestException;
import com.devtrack.util.PdfTextExtractor;
import com.devtrack.util.ResumeKeywordData;

/**
 * Core logic for the resume-analysis features:
 *  1) Red-flag keyword detection: compares text against a target job description (JD)
 *     and flags missing JD keywords + weak/generic phrasing already present.
 *  2) Description quality scoring + rewrite suggestion.
 *  3) PDF upload flow: extracts text from an uploaded resume PDF and runs both (1) and (2)
 *     against it, plus an ATS-friendliness score, in one call.
 *
 * Deliberately rule-based (no external AI call) so it's a self-contained, offline-runnable
 * module - see ResumeKeywordData for the word lists that drive it, the first place to extend.
 */
@Service
public class ResumeAnalyzerService {

    private static final Pattern WORD_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z+#./-]{1,}");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+(\\.\\d+)?%?");

    private final AtsScoringService atsScoringService;
    private final GeminiResumeAnalyzerService geminiResumeAnalyzerService;

    public ResumeAnalyzerService(
        AtsScoringService atsScoringService,
        GeminiResumeAnalyzerService geminiResumeAnalyzerService) {

    this.atsScoringService = atsScoringService;
    this.geminiResumeAnalyzerService = geminiResumeAnalyzerService;
}

    // ---------- Feature 1: JD red-flag / keyword-gap analysis ----------

    public RedFlagAnalyzeResponse analyzeRedFlags(String jobDescription, String projectDescription) {
        Set<String> jdKeywords = extractKeywords(jobDescription, 25);
        String descLower = projectDescription.toLowerCase(Locale.ROOT);

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (String kw : jdKeywords) {
            if (descLower.contains(kw)) {
                matched.add(kw);
            } else {
                missing.add(kw);
            }
        }
        matched.sort(String.CASE_INSENSITIVE_ORDER);
        missing.sort(String.CASE_INSENSITIVE_ORDER);

        List<RedFlagHit> hits = findWeakPhrases(projectDescription);

        int score = jdKeywords.isEmpty() ? 0 : (int) Math.round(100.0 * matched.size() / jdKeywords.size());

        return new RedFlagAnalyzeResponse(missing, matched, hits, score);
    }

    private List<RedFlagHit> findWeakPhrases(String description) {
        List<RedFlagHit> hits = new ArrayList<>();
        String[] sentences = ResumeKeywordData.splitSentences(description);

        for (String[] entry : ResumeKeywordData.WEAK_PHRASES) {
            String phrase = entry[0];
            String reason = entry[1];
            for (String sentence : sentences) {
                if (sentence.toLowerCase(Locale.ROOT).contains(phrase)) {
                    hits.add(new RedFlagHit(phrase, sentence.trim(), reason));
                    break;
                }
            }
        }
        return hits;
    }

    private Set<String> extractKeywords(String text, int maxKeywords) {
        Map<String, Integer> freq = new HashMap<>();
        Matcher m = WORD_PATTERN.matcher(text.toLowerCase(Locale.ROOT));
        while (m.find()) {
            String word = m.group();
            if (word.length() < 2 || ResumeKeywordData.STOPWORDS.contains(word)) continue;
            freq.merge(word, 1, Integer::sum);
        }

        List<String> techHits = freq.keySet().stream()
                .filter(ResumeKeywordData.TECH_DICTIONARY::contains)
                .sorted((a, b) -> freq.get(b) - freq.get(a))
                .toList();

        List<String> otherHits = freq.entrySet().stream()
                .filter(e -> !ResumeKeywordData.TECH_DICTIONARY.contains(e.getKey()))
                .filter(e -> e.getValue() >= 2)
                .sorted((a, b) -> b.getValue() - a.getValue())
                .map(Map.Entry::getKey)
                .toList();

        LinkedHashSet<String> result = new LinkedHashSet<>();
        result.addAll(techHits);
        for (String w : otherHits) {
            if (result.size() >= maxKeywords) break;
            result.add(w);
        }
        return result.stream().limit(maxKeywords).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    // ---------- Feature 2: description quality scoring + rewrite ----------

    public DescriptionScoreResponse scoreDescription(String description, String jobDescriptionOrNull) {
        List<ScoreBreakdownItem> breakdown = new ArrayList<>();
        List<String> issues = new ArrayList<>();
        String lower = description.toLowerCase(Locale.ROOT);

        String[] sentences = ResumeKeywordData.splitSentences(description);
        long strongOpeners = Arrays.stream(sentences)
                .filter(s -> {
                    String firstWord = s.trim().split("\\s+")[0]
                            .toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
                    return ResumeKeywordData.STRONG_ACTION_VERBS.contains(firstWord);
                })
                .count();
        int actionScore = sentences.length == 0 ? 0 :
                (int) Math.round(25.0 * strongOpeners / sentences.length);
        breakdown.add(new ScoreBreakdownItem("Action-verb openers", actionScore, 25,
                strongOpeners + "/" + sentences.length + " lines start with a strong action verb"));
        if (actionScore < 15) issues.add("Most lines don't open with a strong action verb (e.g. Built, Engineered, Automated).");

        boolean hasMetric = NUMBER_PATTERN.matcher(description).find();
        int metricScore = hasMetric ? 25 : 0;
        breakdown.add(new ScoreBreakdownItem("Quantifiable impact", metricScore, 25,
                hasMetric ? "Contains at least one number/metric" : "No numbers, %, or metrics found"));
        if (!hasMetric) issues.add("No quantifiable results (%, counts, latency, coverage, users, etc.) - recruiters skim for numbers first.");

        List<RedFlagHit> weakHits = findWeakPhrases(description);
        int weakScore = Math.max(0, 20 - weakHits.size() * 5);
        breakdown.add(new ScoreBreakdownItem("Avoids generic phrasing", weakScore, 20,
                weakHits.isEmpty() ? "No generic/weak phrases found" : weakHits.size() + " weak phrase(s) found"));
        if (!weakHits.isEmpty()) issues.add("Contains " + weakHits.size() + " generic/weak phrase(s) - see red-flag scan for details.");

        Set<String> techMentioned = ResumeKeywordData.TECH_DICTIONARY.stream()
                .filter(lower::contains)
                .collect(Collectors.toSet());
        int techScore = Math.min(15, techMentioned.size() * 3);
        breakdown.add(new ScoreBreakdownItem("Tech-stack specificity", techScore, 15,
                techMentioned.isEmpty() ? "No specific technologies named" : techMentioned.size() + " named: " + String.join(", ", techMentioned)));
        if (techMentioned.size() < 2) issues.add("Names very few concrete technologies - be specific (e.g. 'PostgreSQL' not 'a database').");

        int wordCount = description.trim().isEmpty() ? 0 : description.trim().split("\\s+").length;
        int avgWordsPerSentence = sentences.length == 0 ? 0 : wordCount / Math.max(1, sentences.length);
        boolean lengthOk = avgWordsPerSentence >= 10 && avgWordsPerSentence <= 40 && sentences.length >= 1 && sentences.length <= 8;
        int lengthScore = lengthOk ? 15 : 7;
        breakdown.add(new ScoreBreakdownItem("Length & structure", lengthScore, 15,
                sentences.length + " line(s), ~" + avgWordsPerSentence + " words/line"));
        if (!lengthOk) issues.add("Line length/count is outside the readable sweet spot (aim for 3-6 bullets, ~15-30 words each).");

        int overall = actionScore + metricScore + weakScore + techScore + lengthScore;

        String suggestion = buildSuggestion(jobDescriptionOrNull, techMentioned, hasMetric);

        return new DescriptionScoreResponse(overall, breakdown, issues, suggestion);
    }

    private String buildSuggestion(String jdOrNull, Set<String> techMentioned, boolean hasMetric) {
        List<String> techList = new ArrayList<>(techMentioned);
        if (jdOrNull != null && !jdOrNull.isBlank()) {
            Set<String> jdKeywords = extractKeywords(jdOrNull, 25);
            for (String kw : jdKeywords) {
                if (ResumeKeywordData.TECH_DICTIONARY.contains(kw) && !techList.contains(kw) && techList.size() < 5) {
                    techList.add(kw);
                }
            }
        }
        String techPhrase = techList.isEmpty() ? "[name your core stack, e.g. Spring Boot, React, PostgreSQL]"
                : String.join(", ", techList.subList(0, Math.min(4, techList.size())));

        String metricPhrase = hasMetric
                ? "measurably improving performance/reliability"
                : "[add a real metric - e.g. reduced API response time by 40%, achieved 65%+ test coverage]";

        return String.format(
            "Engineered a full-stack platform using %s, implementing role-based access control (JWT) " +
            "and RESTful APIs for core business workflows. Containerized services with Docker Compose for " +
            "consistent local and cloud deployment, and %s through targeted optimization and automated testing.",
            techPhrase, metricPhrase
        );
    }

    // ---------- Feature 3: PDF upload - extract, then run both analyses + ATS score ----------

    public FullResumeAnalysisResponse analyzeResumePdf(MultipartFile file, String jobDescription) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please upload a resume PDF file.");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new BadRequestException("Only PDF files are supported for resume upload.");
        }
        if (jobDescription == null || jobDescription.isBlank()) {
            throw new BadRequestException("Job description is required.");
        }

        String resumeText;
        try {
            resumeText = PdfTextExtractor.extractText(file.getBytes());
        } catch (IOException e) {
            throw new BadRequestException("Could not read the PDF: " + e.getMessage());
        }

        if (resumeText == null || resumeText.trim().isEmpty()) {
            throw new BadRequestException(
                "No extractable text found in this PDF - it may be a scanned image without OCR text.");
        }

        RedFlagAnalyzeResponse redFlags = analyzeRedFlags(jobDescription, resumeText);
        DescriptionScoreResponse descScore = scoreDescription(resumeText, jobDescription);
        AtsScoreResponse ats = atsScoringService.score(resumeText, redFlags.getKeywordMatchScore());

        return new FullResumeAnalysisResponse(
        resumeText,
        ats,
        redFlags,
        descScore,
        geminiResumeAnalyzerService.analyzeResume(resumeText, jobDescription)
);
    }
}
