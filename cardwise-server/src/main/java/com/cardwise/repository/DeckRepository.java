package com.cardwise.repository;

import com.cardwise.model.Deck;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface DeckRepository extends JpaRepository<Deck, UUID> {
    List<Deck> findByUserIdOrderByCreatedAtDesc(UUID userId);
    boolean existsByIdAndUserId(UUID id, UUID userId);
}
