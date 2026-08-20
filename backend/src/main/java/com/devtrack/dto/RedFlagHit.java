package com.devtrack.dto;

public class RedFlagHit {
    private String phrase;
    private String foundInSentence;
    private String reason;

    public RedFlagHit(String phrase, String foundInSentence, String reason) {
        this.phrase = phrase;
        this.foundInSentence = foundInSentence;
        this.reason = reason;
    }

    public String getPhrase() { return phrase; }
    public String getFoundInSentence() { return foundInSentence; }
    public String getReason() { return reason; }
}
