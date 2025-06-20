package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService implements CRUDService<CardDto>{
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    @Override
    public CardDto getById(Long id) {
        log.info("Get card by id: " + id);
        Card card = cardRepository.findById(id).orElse(null);
        if (card == null) {
            return null;
        }
        return mapToDto(card);
    }

    @Override
    public Collection<CardDto> getAll() {
        log.info("Get all cards");
        return cardRepository.findAll().stream()
                .map(CardService::mapToDto)
                .toList();
    }

    public Collection<CardDto> getCardsByHolderId(Long id) {
        log.info("Get all cards of user " + id);
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return null;
        }
        return cardRepository.findByHolderId(id).stream()
                .map(CardService::mapToDto)
                .toList();
    }

    @Override
    public CardDto create(Long holderId) {
        log.info("Create new card for User " + holderId);
        User user = userRepository.findById(holderId).orElse(null);
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
        card.setBalance(BigDecimal.ZERO);

        return mapToDto(cardRepository.save(card));
    }

    @Override
    public CardDto update(CardDto cardDto) {
        Card card = cardRepository.findByNumber(cardDto.getNumber()).orElse(null);
        if (card == null) {
            return null;
        }
        if (cardDto.getStatus() != null && card.getStatus() != cardDto.getStatus()) {
            card.setStatus(cardDto.getStatus());
        }
        log.info("Card " + cardDto.getNumber() + " changed status to " + cardDto.getStatus());
        return mapToDto(cardRepository.save(card));
    }

    @Override
    public void deleteById(Long id) {
        cardRepository.deleteById(id);
    }

    public static CardDto mapToDto (Card card) {
        CardDto cardDto = new CardDto();
        cardDto.setNumber(card.getNumber());
        cardDto.setHolderName(card.getHolder().getUsername());
        cardDto.setExpirationDate(card.getExpirationDate());
        cardDto.setStatus(card.getStatus());
        cardDto.setBalance(card.getBalance());
        return cardDto;
    }
}
