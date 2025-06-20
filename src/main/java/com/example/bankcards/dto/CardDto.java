package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CardDto {
    private String number;
    private String holderName;
    private LocalDate expirationDate;
    private CardStatus status;
    private BigDecimal balance;
}
