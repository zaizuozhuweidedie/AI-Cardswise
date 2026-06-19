package com.cardwise.repository;

import com.cardwise.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {
    List<Card> findByDeckIdOrderByCreatedAtAsc(UUID deckId);

    @Query("SELECT c FROM Card c WHERE c.userId = :userId AND c.nextReviewAt <= :now ORDER BY c.nextReviewAt ASC")
    List<Card> findDueCardsByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    @Query("SELECT c FROM Card c WHERE c.deckId = :deckId AND c.userId = :userId AND c.nextReviewAt <= :now ORDER BY c.nextReviewAt ASC")
    List<Card> findDueCardsByDeckIdAndUserId(@Param("deckId") UUID deckId, @Param("userId") UUID userId, @Param("now") LocalDateTime now);

    long countByUserId(UUID userId);

    @Query("SELECT COUNT(c) FROM Card c WHERE c.userId = :userId AND c.repetitions >= 5")
    long countMasteredByUserId(@Param("userId") UUID userId);

    long countByDeckIdAndUserId(UUID deckId, UUID userId);
}
