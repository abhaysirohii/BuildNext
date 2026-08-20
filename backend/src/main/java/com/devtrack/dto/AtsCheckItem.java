package com.devtrack.dto;

public class AtsCheckItem {
    private String checkName;
    private int score;
    private int maxScore;
    private String detail;

    public AtsCheckItem(String checkName, int score, int maxScore, String detail) {
        this.checkName = checkName;
        this.score = score;
        this.maxScore = maxScore;
        this.detail = detail;
    }

    public String getCheckName() { return checkName; }
    public int getScore() { return score; }
    public int getMaxScore() { return maxScore; }
    public String getDetail() { return detail; }
}
