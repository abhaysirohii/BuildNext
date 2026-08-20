package com.devtrack.dto;

/** Combined response for the PDF-upload flow: ATS score + both existing analyzer features,
 *  all run against the text extracted from the uploaded resume. */
public class FullResumeAnalysisResponse {
    private String extractedResumeText;
    private AtsScoreResponse atsScore;
    private RedFlagAnalyzeResponse redFlagAnalysis;
    private DescriptionScoreResponse descriptionScore;
    private String geminiAnalysis;
    public FullResumeAnalysisResponse(String extractedResumeText, AtsScoreResponse atsScore,
                                       RedFlagAnalyzeResponse redFlagAnalysis,
                                       DescriptionScoreResponse descriptionScore, String geminiAnalysis) {
        this.extractedResumeText = extractedResumeText;
        this.atsScore = atsScore;
        this.redFlagAnalysis = redFlagAnalysis;
        this.descriptionScore = descriptionScore;
        this.geminiAnalysis = geminiAnalysis;
    }

    public String getExtractedResumeText() { return extractedResumeText; }
    public AtsScoreResponse getAtsScore() { return atsScore; }
    public RedFlagAnalyzeResponse getRedFlagAnalysis() { return redFlagAnalysis; }
    public String getGeminiAnalysis() { return geminiAnalysis; }
    public DescriptionScoreResponse getDescriptionScore() { return descriptionScore; }
}
