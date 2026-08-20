package com.devtrack.service;

import com.devtrack.dto.AtsCheckItem;
import com.devtrack.dto.AtsScoreResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Rule-based ATS (Applicant Tracking System) friendliness score for an uploaded resume.
 * Checks the kind of things real ATS parsers (Workday, Greenhouse, Taleo, etc.) commonly
 * choke on: missing standard sections, missing contact info, layouts that don't extract
 * to clean text (multi-column, tables, text boxes, scanned images), and how well the
 * resume's language actually matches the target job description.
 */
@Service
public class AtsScoringService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("(\\+?\\d[\\d\\-\\s()]{8,}\\d)");

    private static final String[] EXPECTED_SECTIONS = {
        "experience", "education", "skills", "projects", "summary", "objective", "certification"
    };

    public AtsScoreResponse score(String resumeText, int jdKeywordMatchScore) {
        List<AtsCheckItem> checks = new ArrayList<>();
        String lower = resumeText.toLowerCase(Locale.ROOT);

        // 1) Standard section headers (25 pts) - ATS parsers map content to fields
        // using these headers; missing them means content may not get categorized at all.
        long sectionsFound = Arrays.stream(EXPECTED_SECTIONS).filter(lower::contains).count();
        int sectionScore = (int) Math.min(25, sectionsFound * 6);
        checks.add(new AtsCheckItem("Standard section headers", sectionScore, 25,
                sectionsFound + " of " + EXPECTED_SECTIONS.length +
                " expected sections found (Experience, Education, Skills, Projects, etc.)"));

        // 2) Contact info (15 pts)
        boolean hasEmail = EMAIL_PATTERN.matcher(resumeText).find();
        boolean hasPhone = PHONE_PATTERN.matcher(resumeText).find();
        int contactScore = (hasEmail ? 8 : 0) + (hasPhone ? 7 : 0);
        checks.add(new AtsCheckItem("Contact info detected", contactScore, 15,
                (hasEmail ? "Email found. " : "No email found. ") +
                (hasPhone ? "Phone number found." : "No phone number found.")));

        // 3) Parsing / formatting friendliness (20 pts) - a low letter-density or very
        // low word count after extraction usually means the PDF used multi-column layout,
        // tables, text boxes, or is a scanned image - all of which break most ATS parsers
        // even though the file looks fine to a human.
        int wordCount = resumeText.trim().isEmpty() ? 0 : resumeText.trim().split("\\s+").length;
        long letters = resumeText.chars().filter(Character::isLetter).count();
        double letterRatio = resumeText.isEmpty() ? 0 : (double) letters / resumeText.length();
        boolean parsesCleanly = letterRatio > 0.5 && wordCount > 30;
        int formatScore = parsesCleanly ? 20 : 6;
        checks.add(new AtsCheckItem("ATS-parseable formatting", formatScore, 20,
                parsesCleanly
                    ? "Text extracted cleanly - looks like a single-column, text-based layout most ATS systems can read."
                    : "Extracted text looks sparse or irregular - this usually means multi-column layouts, tables, " +
                      "text boxes, or a scanned/image-based PDF, which many ATS parsers fail to read correctly."));

        // 4) Length appropriateness (15 pts)
        boolean lengthOk = wordCount >= 150 && wordCount <= 1200;
        int lengthScore = lengthOk ? 15 : 7;
        checks.add(new AtsCheckItem("Resume length", lengthScore, 15,
                wordCount + " words - " + (lengthOk
                        ? "within the typical 1-2 page sweet spot"
                        : "outside the typical ~150-1200 word range for a 1-2 page resume")));

        // 5) JD keyword match (25 pts) - reuses the keyword-match score from the
        // red-flag scan so the ATS score reflects actual relevance to this job, not
        // just generic resume hygiene.
        int keywordScore = (int) Math.round(jdKeywordMatchScore * 0.25);
        checks.add(new AtsCheckItem("Job description keyword match", keywordScore, 25,
                jdKeywordMatchScore + "% of extracted JD keywords were found in the resume text"));

        int overall = checks.stream().mapToInt(AtsCheckItem::getScore).sum();
        return new AtsScoreResponse(overall, checks);
    }
}
