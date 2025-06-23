package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransferDto {
    @NotNull(message = "Source card ID must be provided")
    @Schema(example = "1")
    private Long fromCardId;

    @NotNull(message = "Destination card ID must be provided")
    @Schema(example = "2")
    private Long toCardId;

    @NotNull(message = "Amount must be provided")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    @Schema(example = "900.00")
    private BigDecimal amount;
}
