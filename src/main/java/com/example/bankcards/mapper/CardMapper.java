package com.example.bankcards.mapper;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import org.springframework.stereotype.Component;

@Component
public class CardMapper {

    public CardDto mapToDto(Card card) {
        CardDto cardDto = new CardDto();
        cardDto.setId(card.getId());
        cardDto.setNumber(card.getNumber());
        cardDto.setHolderName(card.getHolder().getUsername());
        cardDto.setExpirationDate(card.getExpirationDate());
        cardDto.setStatus(card.getStatus());
        cardDto.setBalance(card.getBalance());
        return cardDto;
    }
}
