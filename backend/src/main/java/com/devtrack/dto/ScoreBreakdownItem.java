package com.devtrack.dto;

public class ScoreBreakdownItem {
    private String criterion;
    private int score;
    private int maxScore;
    private String note;

    public ScoreBreakdownItem(String criterion, int score, int maxScore, String note) {
        this.criterion = criterion;
        this.score = score;
        this.maxScore = maxScore;
        this.note = note;
    }

    public String getCriterion() { return criterion; }
    public int getScore() { return score; }
    public int getMaxScore() { return maxScore; }
    public String getNote() { return note; }
}
