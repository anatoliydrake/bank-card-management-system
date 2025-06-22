package com.example.bankcards.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransferDto {
    @NotNull(message = "Source card ID must be provided")
    private Long fromCardId;

    @NotNull(message = "Destination card ID must be provided")
    private Long toCardId;

    @NotNull(message = "Amount must be provided")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private BigDecimal amount;
}
