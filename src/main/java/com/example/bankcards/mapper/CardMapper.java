package com.example.bankcards.mapper;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.util.CardNumberMasker;
import com.example.bankcards.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardMapper {
    private final EncryptionUtil encryptionUtil;

    public CardDto mapToDto(Card card) {
        String decryptedNumber = encryptionUtil.decrypt(card.getNumber());
        String maskedNumber = CardNumberMasker.maskCardNumber(decryptedNumber);

        CardDto cardDto = new CardDto();
        cardDto.setId(card.getId());
        cardDto.setNumber(maskedNumber);
        cardDto.setHolderName(card.getHolder().getUsername());
        cardDto.setExpirationDate(card.getExpirationDate());
        cardDto.setStatus(card.getStatus());
        cardDto.setBalance(card.getBalance());
        return cardDto;
    }
}
