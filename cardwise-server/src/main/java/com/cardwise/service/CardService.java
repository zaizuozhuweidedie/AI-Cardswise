package com.cardwise.service;

import com.cardwise.dto.CardRequest;
import com.cardwise.dto.ReviewRequest;
import com.cardwise.dto.SM2ResultResponse;
import com.cardwise.exception.ResourceNotFoundException;
import com.cardwise.model.Card;
import com.cardwise.model.ReviewLog;
import com.cardwise.repository.CardRepository;
import com.cardwise.repository.DeckRepository;
import com.cardwise.repository.ReviewLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;
    private final ReviewLogRepository reviewLogRepository;
    private final SM2Algorithm sm2Algorithm;

    public CardService(CardRepository cardRepository, DeckRepository deckRepository,
                       ReviewLogRepository reviewLogRepository, SM2Algorithm sm2Algorithm) {
        this.cardRepository = cardRepository;
        this.deckRepository = deckRepository;
        this.reviewLogRepository = reviewLogRepository;
        this.sm2Algorithm = sm2Algorithm;
    }

    public List<Card> getCardsByDeck(UUID deckId, UUID userId) {
        verifyDeckOwnership(deckId, userId);
        return cardRepository.findByDeckIdOrderByCreatedAtAsc(deckId);
    }

    public Card createCard(UUID deckId, CardRequest request, UUID userId) {
        verifyDeckOwnership(deckId, userId);
        Card card = new Card();
        card.setFront(request.getFront());
        card.setBack(request.getBack());
        card.setTags(request.getTags());
        card.setDeckId(deckId);
        card.setUserId(userId);
        card.setNextReviewAt(LocalDateTime.now());
        return cardRepository.save(card);
    }

    public Card updateCard(UUID cardId, CardRequest request, UUID userId) {
        Card card = findCardByIdAndUserId(cardId, userId);
        card.setFront(request.getFront());
        card.setBack(request.getBack());
        card.setTags(request.getTags());
        return cardRepository.save(card);
    }

    public void deleteCard(UUID cardId, UUID userId) {
        Card card = findCardByIdAndUserId(cardId, userId);
        cardRepository.delete(card);
    }

    public List<Card> getDueCards(UUID userId, UUID deckId) {
        if (deckId != null) {
            verifyDeckOwnership(deckId, userId);
            return cardRepository.findDueCardsByDeckIdAndUserId(deckId, userId, LocalDateTime.now());
        }
        return cardRepository.findDueCardsByUserId(userId, LocalDateTime.now());
    }

    @Transactional
    public SM2ResultResponse reviewCard(UUID cardId, ReviewRequest request, UUID userId) {
        Card card = findCardByIdAndUserId(cardId, userId);
        int quality = request.getQuality();

        SM2ResultResponse result = sm2Algorithm.calculate(
                quality, card.getEaseFactor(), card.getIntervalDays(), card.getRepetitions());

        card.setEaseFactor(result.getNewEaseFactor());
        card.setIntervalDays(result.getNewIntervalDays());
        card.setRepetitions(result.getNewRepetitions());
        card.setNextReviewAt(result.getNextReviewAt());
        card.setLastReviewAt(LocalDateTime.now());
        cardRepository.save(card);

        ReviewLog log = new ReviewLog();
        log.setCardId(cardId);
        log.setUserId(userId);
        log.setQuality(quality);
        log.setEaseFactor(result.getNewEaseFactor());
        log.setIntervalDays(result.getNewIntervalDays());
        log.setRepetitions(result.getNewRepetitions());
        reviewLogRepository.save(log);

        return result;
    }

    private Card findCardByIdAndUserId(UUID cardId, UUID userId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
        if (!card.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Card not found");
        }
        return card;
    }

    private void verifyDeckOwnership(UUID deckId, UUID userId) {
        if (!deckRepository.existsByIdAndUserId(deckId, userId)) {
            throw new ResourceNotFoundException("Deck not found");
        }
    }
}
