package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;import com.example.bankcards.exception.CardNotActiveException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;

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

    public Collection<CardDto> getCardsByHolderId(Long id) {
        log.info("Get all cards of user {}", id);
        if (userRepository.findById(id).isEmpty()) {
            throw new UserNotFoundException(id);
        }
        return cardRepository.findByHolderId(id).stream()
                .map(cardMapper::mapToDto)
                .toList();
    }

    public CardDto create(Long id) {
        log.info("Create new card for User");
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        String cardNumber;
        do {
            cardNumber = CardNumberGenerator.generateCardNumber();
        } while (cardRepository.existsByNumber(cardNumber));

        Card card = new Card();
        card.setNumber(cardNumber);
        card.setHolder(user);
        card.setExpirationDate(LocalDate.now().plusYears(5));
        card.setStatus(CardStatus.ACTIVE);
        int randomAmount = ThreadLocalRandom.current().nextInt(20, 101);
        card.setBalance(BigDecimal.valueOf(randomAmount * 1_000L));

        return cardMapper.mapToDto(cardRepository.save(card));
    }

    public CardDto changeCardStatus(Long cardId, CardStatus newStatus) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new CardNotFoundException(cardId));
        if (card.getStatus().equals(newStatus)) {
            return cardMapper.mapToDto(card);
        }
        card.setStatus(newStatus);
        log.info("Card ID: {} changed status to {}", card.getId(), newStatus);
        return cardMapper.mapToDto(cardRepository.save(card));
    }

    public void deleteById(Long id) {
        if (cardRepository.findById(id).isEmpty()) {
            throw new CardNotFoundException(id);
        }
        cardRepository.deleteById(id);
        log.info("Card ID: {} deleted", id);
    }

    public BigDecimal getBalance(Long cardId) {
        log.info("Get balance for card ID: {}", cardId);
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new CardNotFoundException(cardId));
        return card.getBalance();
    }

    @Transactional
    public void transfer(TransferDto transferDto) {
        String fromCardNumber = transferDto.getFromCardNumber();
        Card fromCard = cardRepository.findByNumber(fromCardNumber)
                .orElseThrow(() -> new CardNotFoundException(fromCardNumber));

        String toCardNumber = transferDto.getToCardNumber();
        Card toCard = cardRepository.findByNumber(toCardNumber)
                .orElseThrow(() -> new CardNotFoundException(toCardNumber));

        if (fromCard.getStatus() != CardStatus.ACTIVE) {
            throw new CardNotActiveException(fromCardNumber);
        }
        if (toCard.getStatus() != CardStatus.ACTIVE) {
            throw new CardNotActiveException(toCardNumber);
        }

        if (fromCard.getBalance().compareTo(transferDto.getAmount()) < 0) {
            throw new InsufficientFundsException(fromCardNumber);
        }

        fromCard.setBalance(fromCard.getBalance().subtract(transferDto.getAmount()));
        toCard.setBalance(toCard.getBalance().add(transferDto.getAmount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        log.info("Transferred {} from {} to {}", transferDto.getAmount(), fromCard.getNumber(), toCard.getNumber());
    }
}
