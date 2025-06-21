package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
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
        log.info("Get card by ID: " + id);
        Card card = cardRepository.findById(id).orElse(null);
        if (card == null) {
            return null;
        }
        return cardMapper.mapToDto(card);
    }

    public Collection<CardDto> getAll() {
        log.info("Get all cards");
        return cardRepository.findAll().stream()
                .map(cardMapper::mapToDto)
                .toList();
    }

    public Collection<CardDto> getCardsByHolderId(Long id) {
        log.info("Get all cards of user " + id);
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return null;
        }
        return cardRepository.findByHolderId(id).stream()
                .map(cardMapper::mapToDto)
                .toList();
    }

    public CardDto create(Long id) {
        log.info("Create new card for User");
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return null;
        }

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
        Card card = cardRepository.findById(cardId).orElse(null);
        if (card == null) {
            return null;
        }
        if (!card.getStatus().equals(newStatus)) {
            card.setStatus(newStatus);
        }
        log.info("Card ID: " + card.getId() + " changed status to " + newStatus);
        return cardMapper.mapToDto(cardRepository.save(card));
    }

    public void deleteById(Long id) {
        cardRepository.deleteById(id);
        log.info("Card ID: " + id + " deleted");
    }

    public BigDecimal getBalance(Long cardId) {
        log.info("Get balance for card ID: " + cardId);
        Card card = cardRepository.findById(cardId).orElse(null);
        if (card == null) {
            return null;
        }
        return card.getBalance();
    }

    @Transactional
    public String transfer(TransferDto transferDto) {
        Card fromCard = cardRepository.findByNumber(transferDto.getFromCardNumber()).orElse(null);
        Card toCard = cardRepository.findByNumber(transferDto.getToCardNumber()).orElse(null);

        if (fromCard == null || toCard == null) {
            return "One or both cards not found";
        }

        if (fromCard.getStatus() != CardStatus.ACTIVE) {
            return "Source card is not active";
        }

        if (toCard.getStatus() != CardStatus.ACTIVE) {
            return "Destination card is not active";
        }

        if (fromCard.getBalance().compareTo(transferDto.getAmount()) < 0) {
            return "Insufficient funds";
        }

        fromCard.setBalance(fromCard.getBalance().subtract(transferDto.getAmount()));
        toCard.setBalance(toCard.getBalance().add(transferDto.getAmount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        log.info("Transferred {} from {} to {}", transferDto.getAmount(), fromCard.getNumber(), toCard.getNumber());

        return "Transfer successful";
    }
}
