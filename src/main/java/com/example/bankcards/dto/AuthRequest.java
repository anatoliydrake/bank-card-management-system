package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AuthRequest {
    @Schema(example = "admin")
    private String username;
    @Schema(example = "qwerty12345")
    private String password;
}
