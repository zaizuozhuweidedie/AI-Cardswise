package com.cardwise.repository;

import com.cardwise.model.ReviewLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ReviewLogRepository extends JpaRepository<ReviewLog, UUID> {
    long countByUserIdAndReviewedAtAfter(UUID userId, LocalDateTime after);

    @Query("SELECT CAST(r.reviewedAt AS LocalDate) as date, COUNT(DISTINCT r.cardId) as count " +
           "FROM ReviewLog r WHERE r.userId = :userId AND r.reviewedAt >= :since " +
           "GROUP BY CAST(r.reviewedAt AS LocalDate) ORDER BY date ASC")
    List<Object[]> countDailyActivity(@Param("userId") UUID userId, @Param("since") LocalDateTime since);
}
