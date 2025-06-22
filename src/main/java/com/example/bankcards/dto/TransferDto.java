package com.example.bankcards.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransferDto {
    @NotBlank(message = "Source card number must be provided")
    @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
    private String fromCardNumber;

    @NotBlank(message = "Destination card number must be provided")
    @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
    private String toCardNumber;

    @NotNull(message = "Amount must be provided")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private BigDecimal amount;
}
