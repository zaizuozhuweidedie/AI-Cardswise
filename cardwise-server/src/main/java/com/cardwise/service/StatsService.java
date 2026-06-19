package com.cardwise.service;

import com.cardwise.dto.StatsResponse;
import com.cardwise.model.Card;
import com.cardwise.repository.CardRepository;
import com.cardwise.repository.ReviewLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class StatsService {

    private final CardRepository cardRepository;
    private final ReviewLogRepository reviewLogRepository;

    public StatsService(CardRepository cardRepository, ReviewLogRepository reviewLogRepository) {
        this.cardRepository = cardRepository;
        this.reviewLogRepository = reviewLogRepository;
    }

    public StatsResponse getStats(UUID userId) {
        StatsResponse stats = new StatsResponse();
        stats.setTotalCards(cardRepository.countByUserId(userId));

        List<Card> dueCards = cardRepository.findDueCardsByUserId(userId, LocalDateTime.now());
        stats.setDueCards(dueCards.size());

        stats.setStudiedToday(
                reviewLogRepository.countByUserIdAndReviewedAtAfter(
                        userId, LocalDateTime.now().withHour(0).withMinute(0).withSecond(0)));

        stats.setMasteredCards(cardRepository.countMasteredByUserId(userId));

        List<Map<String, Object>> activity = new ArrayList<>();
        List<Object[]> rawActivity = reviewLogRepository.countDailyActivity(
                userId, LocalDateTime.now().minusDays(30));
        for (Object[] row : rawActivity) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("date", row[0].toString());
            entry.put("count", ((Number) row[1]).longValue());
            activity.add(entry);
        }
        stats.setDailyActivity(activity);
        return stats;
    }
}
