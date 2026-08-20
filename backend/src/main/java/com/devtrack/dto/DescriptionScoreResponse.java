package com.devtrack.dto;

import java.util.List;

public class DescriptionScoreResponse {
    private int overallScore;
    private List<ScoreBreakdownItem> breakdown;
    private List<String> issues;
    private String suggestedDescription;

    public DescriptionScoreResponse(int overallScore, List<ScoreBreakdownItem> breakdown,
                                     List<String> issues, String suggestedDescription) {
        this.overallScore = overallScore;
        this.breakdown = breakdown;
        this.issues = issues;
        this.suggestedDescription = suggestedDescription;
    }

    public int getOverallScore() { return overallScore; }
    public List<ScoreBreakdownItem> getBreakdown() { return breakdown; }
    public List<String> getIssues() { return issues; }
    public String getSuggestedDescription() { return suggestedDescription; }
}
