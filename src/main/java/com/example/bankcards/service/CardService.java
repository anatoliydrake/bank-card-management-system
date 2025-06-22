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
        log.info("Get card by ID: {}", id);
        return cardRepository.findById(id).map(cardMapper::mapToDto).orElseThrow(() -> new CardNotFoundException(id));
    }

    public Collection<CardDto> getAll() {
        log.info("Get all cards");
        return cardRepository.findAll().stream()
                .map(cardMapper::mapToDto)
                .toList();
    }

    public Collection<CardDto> getCardsByUsername(String username) {
        log.info("Get all cards of user {}", username);
        if (userRepository.findByUsername(username).isEmpty()) {
            throw new UserNotFoundException(username);
        }
        return cardRepository.findByHolderUsername(username).stream()
                .map(cardMapper::mapToDto)
                .toList();
    }

    @Transactional
    public CardDto create(Long id) {
        log.info("Create new card for User");
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        String cardNumber;
        do {
            cardNumber = CardNumberGenerator.generateCardNumber();
        } while (cardRepository.existsByNumber(cardNumber));

        Card card = new Card();
        card.setNumber(encryptionUtil.encrypt(cardNumber));
        card.setHolder(user);
        card.setExpirationDate(LocalDate.now().plusYears(5));
        card.setStatus(CardStatus.ACTIVE);
        int randomAmount = ThreadLocalRandom.current().nextInt(20, 101);
        card.setBalance(BigDecimal.valueOf(randomAmount * 1_000L));

        return cardMapper.mapToDto(cardRepository.save(card));
    }

    @Transactional
    public CardDto changeCardStatus(Long cardId, CardStatus newStatus) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new CardNotFoundException(cardId));
        if (card.getStatus().equals(newStatus)) {
            return cardMapper.mapToDto(card);
        }
        card.setStatus(newStatus);
        log.info("Card ID: {} changed status to {}", card.getId(), newStatus);
        return cardMapper.mapToDto(cardRepository.save(card));
    }

    @Transactional
    public void deleteById(Long id) {
        if (cardRepository.findById(id).isEmpty()) {
            throw new CardNotFoundException(id);
        }
        cardRepository.deleteById(id);
        log.info("Card ID: {} deleted", id);
    }

    public BigDecimal getBalanceByUsername(String username) {
        log.info("Get balance for user: {}", username);
        List<Card> cards = cardRepository.findByHolderUsername(username);
        return cards.stream()
                .map(Card::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public void transfer(@Valid TransferDto transferDto, String username) {
        Long fromCardId = transferDto.getFromCardId();
        Card fromCard = cardRepository.findById(fromCardId)
                .orElseThrow(() -> new CardNotFoundException(fromCardId));

        Long toCardId = transferDto.getToCardId();
        Card toCard = cardRepository.findById(toCardId)
                .orElseThrow(() -> new CardNotFoundException(toCardId));

        if (!fromCard.getHolder().getUsername().equals(username) ||
                !toCard.getHolder().getUsername().equals(username)) {
            throw new UnauthorizedActionException("You can transfer only between your own cards.");
        }
        if (fromCard.getStatus() != CardStatus.ACTIVE) {
            throw new CardNotActiveException(fromCardId);
        }
        if (toCard.getStatus() != CardStatus.ACTIVE) {
            throw new CardNotActiveException(toCardId);
        }
        if (fromCard.getBalance().compareTo(transferDto.getAmount()) < 0) {
            throw new InsufficientFundsException(fromCardId);
        }

        fromCard.setBalance(fromCard.getBalance().subtract(transferDto.getAmount()));
        toCard.setBalance(toCard.getBalance().add(transferDto.getAmount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        log.info("User {} transferred {} from {} to {}", username, transferDto.getAmount(),
                fromCard.getNumber(), toCard.getNumber());
    }

    @Transactional
    public void requestBlock(Long cardId, String username) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        if (!card.getHolder().getUsername().equals(username)) {
            throw new UnauthorizedActionException("You can request block only for your own cards.");
        }
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new UnauthorizedActionException("Only active cards can be blocked.");
        }

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
        log.info("User '{}' requested block for card '{}'", username, card.getNumber());
    }
}
