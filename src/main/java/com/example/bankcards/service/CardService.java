package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;

    public CardDto getById(Long id) {
        log.info("Get card by id: " + id);
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
        card.setBalance(BigDecimal.ZERO);

        return cardMapper.mapToDto(cardRepository.save(card));
    }

    public CardDto update(CardDto cardDto) {
        Card card = cardRepository.findById(cardDto.getId()).orElse(null);
        if (card == null) {
            return null;
        }
        if (cardDto.getStatus() != null && !card.getStatus().equals(cardDto.getStatus())) {
            card.setStatus(cardDto.getStatus());
        }
        log.info("Card " + cardDto.getNumber() + " changed status to " + cardDto.getStatus());
        return cardMapper.mapToDto(cardRepository.save(card));
    }

    public void deleteById(Long id) {
        cardRepository.deleteById(id);
        log.info("Card " + id + " deleted");
    }
}
