package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.*;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardNumberGenerator;
import com.example.bankcards.util.EncryptionUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;
    private final EncryptionUtil encryptionUtil;

    public CardDto getById(Long id) {
        log.info("Retrieving card by ID: {}", id);
        return cardRepository.findById(id)
                .map(cardMapper::mapToDto)
                .orElseThrow(() -> {
                    log.warn("Card ID {} not found", id);
                    return new CardNotFoundException(id);
                });
    }

    public Collection<CardDto> getAll() {
        log.info("Retrieving all cards");
        return cardRepository.findAll().stream()
                .map(cardMapper::mapToDto)
                .toList();
    }

    public Page<CardDto> getCardsByUsername(String username, CardStatus status, int page, int size) {
        log.info("Retrieving all cards for user '{}'", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User '{}' not found", username);
                    return new UserNotFoundException(username);
                });
        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cards;

        if (status != null) {
            cards = cardRepository.findByHolderAndStatus(user, status, pageable);
        } else {
            cards = cardRepository.findByHolder(user, pageable);
        }
        return cards.map(cardMapper::mapToDto);
    }

    @Transactional
    public CardDto create(Long id) {
        log.info("Creating new card for user ID {}", id);
        User user = userRepository.findById(id).orElseThrow(() -> {
            log.warn("User ID: {} not found", id);
            return new UserNotFoundException(id);
        });

        String cardNumber;
        do {
            cardNumber = encryptionUtil.encrypt(CardNumberGenerator.generateCardNumber());
        } while (cardRepository.existsByNumber(cardNumber));

        Card card = new Card();
        card.setNumber(cardNumber);
        card.setHolder(user);
        card.setExpirationDate(LocalDate.now().plusYears(5));
        card.setStatus(CardStatus.ACTIVE);
        int randomAmount = ThreadLocalRandom.current().nextInt(20, 101);
        card.setBalance(BigDecimal.valueOf(randomAmount * 1_000L));

        Card savedCard = cardRepository.save(card);
        log.info("Card created successfully with ID {} for user '{}'", savedCard.getId(), user.getUsername());
        return cardMapper.mapToDto(savedCard);
    }

    @Transactional
    public CardDto changeCardStatus(Long cardId, CardStatus newStatus) {
        log.info("Changing status of card ID {} to {}", cardId, newStatus);
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new CardNotFoundException(cardId));
        if (card.getStatus().equals(newStatus)) {
            log.warn("Card ID '{}' status is already '{}'", cardId, newStatus);
            return cardMapper.mapToDto(card);
        }
        card.setStatus(newStatus);
        Card updated = cardRepository.save(card);
        log.info("Card ID {} status changed to {}", updated.getId(), updated.getStatus());
        return cardMapper.mapToDto(updated);
    }

    @Transactional
    public void deleteById(Long id) {
        log.info("Deleting card ID {}", id);
        if (cardRepository.findById(id).isEmpty()) {
            log.warn("Card ID {} not found", id);
            throw new CardNotFoundException(id);
        }
        cardRepository.deleteById(id);
        log.info("Card ID {} deleted successfully", id);
    }

    public BigDecimal getBalanceByUsername(String username) {
        log.info("Calculating total balance for user '{}'", username);
        List<Card> cards = cardRepository.findByHolderUsername(username);
        return cards.stream()
                .map(Card::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public void transfer(@Valid TransferDto transferDto, String username) {
        Long fromCardId = transferDto.getFromCardId();
        Long toCardId = transferDto.getToCardId();
        log.info("Initiating transfer from card {} to {}", fromCardId, toCardId);

        Card fromCard = cardRepository.findById(fromCardId)
                .orElseThrow(() -> {
                    log.warn("Card ID {} not found", fromCardId);
                    return new CardNotFoundException(fromCardId);
                });

        Card toCard = cardRepository.findById(toCardId)
                .orElseThrow(() -> {
                    log.warn("Card ID {} not found", toCardId);
                    return new CardNotFoundException(toCardId);
                });

        if (!fromCard.getHolder().getUsername().equals(username) ||
                !toCard.getHolder().getUsername().equals(username)) {
            log.warn("Unauthorized transfer attempt: user does not own the cards");
            throw new UnauthorizedActionException("You can transfer only between your own cards.");
        }
        if (fromCard.getStatus() != CardStatus.ACTIVE) {
            log.warn("Card ID {} is not active", fromCardId);
            throw new CardNotActiveException(fromCardId);
        }
        if (toCard.getStatus() != CardStatus.ACTIVE) {
            log.warn("Card ID {} is not active", toCardId);
            throw new CardNotActiveException(toCardId);
        }
        BigDecimal amount = transferDto.getAmount();
        if (fromCard.getBalance().compareTo(amount) < 0) {
            log.warn("Insufficient funds on card ID '{}'", fromCardId);
            throw new InsufficientFundsException(fromCardId);
        }

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        log.info("Transfer completed: user '{}' transferred {} from card '{}' to card '{}'",
                username, amount, fromCardId, toCardId);
    }

    @Transactional
    public void requestBlock(Long cardId, String username) {
        log.info("User '{}' is requesting to block card ID {}", username, cardId);
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> {
                    log.warn("Card ID {} not found", cardId);
                    return new CardNotFoundException(cardId);
                });

        if (!card.getHolder().getUsername().equals(username)) {
            log.warn("Unauthorized block attempt: user does not own card ID {}", cardId);
            throw new UnauthorizedActionException("You can request block only for your own cards.");
        }
        if (card.getStatus() != CardStatus.ACTIVE) {
            log.warn("Unauthorized block attempt: card ID {} is not active", cardId);
            throw new UnauthorizedActionException("Only active cards can be blocked.");
        }

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
        log.info("Card with ID {} is successfully blocked", cardId);
    }
}
