package com.cardwise.dto;

import java.util.List;
import java.util.Map;

public class StatsResponse {
    private long totalCards;
    private long dueCards;
    private long studiedToday;
    private long masteredCards;
    private List<Map<String, Object>> dailyActivity;

    public long getTotalCards() { return totalCards; }
    public void setTotalCards(long totalCards) { this.totalCards = totalCards; }
    public long getDueCards() { return dueCards; }
    public void setDueCards(long dueCards) { this.dueCards = dueCards; }
    public long getStudiedToday() { return studiedToday; }
    public void setStudiedToday(long studiedToday) { this.studiedToday = studiedToday; }
    public long getMasteredCards() { return masteredCards; }
    public void setMasteredCards(long masteredCards) { this.masteredCards = masteredCards; }
    public List<Map<String, Object>> getDailyActivity() { return dailyActivity; }
    public void setDailyActivity(List<Map<String, Object>> dailyActivity) { this.dailyActivity = dailyActivity; }
}
