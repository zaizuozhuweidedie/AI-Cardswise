package com.cardwise.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cards", indexes = {
    @Index(name = "idx_cards_user_next_review", columnList = "userId, nextReviewAt"),
    @Index(name = "idx_cards_deck_id", columnList = "deckId")
})
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String front;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String back;
    @Column(columnDefinition = "TEXT")
    private String tags;
    @Column(name = "deck_id", nullable = false)
    private UUID deckId;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(name = "ease_factor", nullable = false)
    private Double easeFactor = 2.5;
    @Column(name = "interval_days", nullable = false)
    private Integer intervalDays = 0;
    @Column(nullable = false)
    private Integer repetitions = 0;
    @Column(name = "next_review_at", nullable = false)
    private LocalDateTime nextReviewAt;
    @Column(name = "last_review_at")
    private LocalDateTime lastReviewAt;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (nextReviewAt == null) nextReviewAt = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    // ALL getters and setters (every single field)
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getFront() { return front; }
    public void setFront(String front) { this.front = front; }
    public String getBack() { return back; }
    public void setBack(String back) { this.back = back; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public UUID getDeckId() { return deckId; }
    public void setDeckId(UUID deckId) { this.deckId = deckId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public Double getEaseFactor() { return easeFactor; }
    public void setEaseFactor(Double easeFactor) { this.easeFactor = easeFactor; }
    public Integer getIntervalDays() { return intervalDays; }
    public void setIntervalDays(Integer intervalDays) { this.intervalDays = intervalDays; }
    public Integer getRepetitions() { return repetitions; }
    public void setRepetitions(Integer repetitions) { this.repetitions = repetitions; }
    public LocalDateTime getNextReviewAt() { return nextReviewAt; }
    public void setNextReviewAt(LocalDateTime nextReviewAt) { this.nextReviewAt = nextReviewAt; }
    public LocalDateTime getLastReviewAt() { return lastReviewAt; }
    public void setLastReviewAt(LocalDateTime lastReviewAt) { this.lastReviewAt = lastReviewAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
