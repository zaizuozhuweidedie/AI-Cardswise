package com.cardwise.dto;

import java.time.LocalDateTime;

public class SM2ResultResponse {
    private double newEaseFactor;
    private int newIntervalDays;
    private int newRepetitions;
    private LocalDateTime nextReviewAt;

    public SM2ResultResponse(double newEaseFactor, int newIntervalDays, int newRepetitions, LocalDateTime nextReviewAt) {
        this.newEaseFactor = newEaseFactor;
        this.newIntervalDays = newIntervalDays;
        this.newRepetitions = newRepetitions;
        this.nextReviewAt = nextReviewAt;
    }

    public double getNewEaseFactor() { return newEaseFactor; }
    public int getNewIntervalDays() { return newIntervalDays; }
    public int getNewRepetitions() { return newRepetitions; }
    public LocalDateTime getNextReviewAt() { return nextReviewAt; }
}
