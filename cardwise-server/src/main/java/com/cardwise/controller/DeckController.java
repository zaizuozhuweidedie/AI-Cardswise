package com.cardwise.controller;

import com.cardwise.config.JwtAuthenticationToken;
import com.cardwise.dto.DeckRequest;
import com.cardwise.exception.ResourceNotFoundException;
import com.cardwise.model.Deck;
import com.cardwise.repository.DeckRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/decks")
public class DeckController {

    private final DeckRepository deckRepository;

    public DeckController(DeckRepository deckRepository) {
        this.deckRepository = deckRepository;
    }

    @GetMapping
    public ResponseEntity<List<Deck>> getAllDecks(@AuthenticationPrincipal JwtAuthenticationToken auth) {
        return ResponseEntity.ok(deckRepository.findByUserIdOrderByCreatedAtDesc(auth.getUserId()));
    }

    @PostMapping
    public ResponseEntity<Deck> createDeck(@Valid @RequestBody DeckRequest request,
                                           @AuthenticationPrincipal JwtAuthenticationToken auth) {
        Deck deck = new Deck();
        deck.setName(request.getName());
        deck.setDescription(request.getDescription());
        deck.setColor(request.getColor() != null ? request.getColor() : "#6366f1");
        deck.setUserId(auth.getUserId());
        return ResponseEntity.ok(deckRepository.save(deck));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Deck> updateDeck(@PathVariable UUID id,
                                           @Valid @RequestBody DeckRequest request,
                                           @AuthenticationPrincipal JwtAuthenticationToken auth) {
        Deck deck = deckRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deck not found"));
        if (!deck.getUserId().equals(auth.getUserId())) {
            throw new ResourceNotFoundException("Deck not found");
        }
        deck.setName(request.getName());
        deck.setDescription(request.getDescription());
        deck.setColor(request.getColor() != null ? request.getColor() : deck.getColor());
        return ResponseEntity.ok(deckRepository.save(deck));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeck(@PathVariable UUID id,
                                           @AuthenticationPrincipal JwtAuthenticationToken auth) {
        Deck deck = deckRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deck not found"));
        if (!deck.getUserId().equals(auth.getUserId())) {
            throw new ResourceNotFoundException("Deck not found");
        }
        deckRepository.delete(deck);
        return ResponseEntity.noContent().build();
    }
}
