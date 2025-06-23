package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CardDto {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "**** **** **** 1234")
    private String number;
    @Schema(example = "username")
    private String holderName;
    @Schema(example = "2025-06-23")
    private LocalDate expirationDate;
    @Schema(example = "ACTIVE")
    private CardStatus status;
    @Schema(example = "10500.51")
    private BigDecimal balance;
}
