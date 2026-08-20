package com.devtrack.dto;

import java.util.List;

public class RedFlagAnalyzeResponse {
    private List<String> missingJdKeywords;
    private List<String> matchedJdKeywords;
    private List<RedFlagHit> redFlagPhrases;
    private int keywordMatchScore;

    public RedFlagAnalyzeResponse(List<String> missingJdKeywords, List<String> matchedJdKeywords,
                                   List<RedFlagHit> redFlagPhrases, int keywordMatchScore) {
        this.missingJdKeywords = missingJdKeywords;
        this.matchedJdKeywords = matchedJdKeywords;
        this.redFlagPhrases = redFlagPhrases;
        this.keywordMatchScore = keywordMatchScore;
    }

    public List<String> getMissingJdKeywords() { return missingJdKeywords; }
    public List<String> getMatchedJdKeywords() { return matchedJdKeywords; }
    public List<RedFlagHit> getRedFlagPhrases() { return redFlagPhrases; }
    public int getKeywordMatchScore() { return keywordMatchScore; }
}
