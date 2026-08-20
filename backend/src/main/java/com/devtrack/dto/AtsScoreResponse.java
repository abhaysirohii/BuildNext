package com.devtrack.dto;

import java.util.List;

public class AtsScoreResponse {
    private int overallAtsScore;
    private List<AtsCheckItem> checks;

    public AtsScoreResponse(int overallAtsScore, List<AtsCheckItem> checks) {
        this.overallAtsScore = overallAtsScore;
        this.checks = checks;
    }

    public int getOverallAtsScore() { return overallAtsScore; }
    public List<AtsCheckItem> getChecks() { return checks; }
}
