package com.cardwise.controller;

import com.cardwise.config.JwtAuthenticationToken;
import com.cardwise.dto.CardRequest;
import com.cardwise.dto.ReviewRequest;
import com.cardwise.dto.SM2ResultResponse;
import com.cardwise.model.Card;
import com.cardwise.service.CardService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping("/api/decks/{deckId}/cards")
    public ResponseEntity<List<Card>> getCards(@PathVariable UUID deckId,
                                               @AuthenticationPrincipal JwtAuthenticationToken auth) {
        return ResponseEntity.ok(cardService.getCardsByDeck(deckId, auth.getUserId()));
    }

    @PostMapping("/api/decks/{deckId}/cards")
    public ResponseEntity<Card> createCard(@PathVariable UUID deckId,
                                           @Valid @RequestBody CardRequest request,
                                           @AuthenticationPrincipal JwtAuthenticationToken auth) {
        return ResponseEntity.ok(cardService.createCard(deckId, request, auth.getUserId()));
    }

    @PutMapping("/api/cards/{id}")
    public ResponseEntity<Card> updateCard(@PathVariable UUID id,
                                           @Valid @RequestBody CardRequest request,
                                           @AuthenticationPrincipal JwtAuthenticationToken auth) {
        return ResponseEntity.ok(cardService.updateCard(id, request, auth.getUserId()));
    }

    @DeleteMapping("/api/cards/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable UUID id,
                                           @AuthenticationPrincipal JwtAuthenticationToken auth) {
        cardService.deleteCard(id, auth.getUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/cards/due")
    public ResponseEntity<List<Card>> getDueCards(@RequestParam(required = false) UUID deckId,
                                                  @AuthenticationPrincipal JwtAuthenticationToken auth) {
        return ResponseEntity.ok(cardService.getDueCards(auth.getUserId(), deckId));
    }

    @PostMapping("/api/cards/{id}/review")
    public ResponseEntity<SM2ResultResponse> reviewCard(@PathVariable UUID id,
                                                        @Valid @RequestBody ReviewRequest request,
                                                        @AuthenticationPrincipal JwtAuthenticationToken auth) {
        return ResponseEntity.ok(cardService.reviewCard(id, request, auth.getUserId()));
    }
}
