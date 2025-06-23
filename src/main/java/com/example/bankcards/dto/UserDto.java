package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@ToString(exclude = "password")
public class UserDto {
    @Schema(example = "1")
    private Long id;

    @NotBlank(message = "Username must not be blank")
    @Size(min = 4, message = "Username must be at least 4 characters")
    @Schema(example = "username")
    private String username;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 4, message = "Password must be at least 4 characters")
    @Schema(example = "<encrypted_password>")
    private String password;

    @NotEmpty(message = "User must have at least one role")
    @Schema(example = "[\"USER\"]")
    private Set<String> roles;

    @Schema(example = "[]")
    private List<CardDto> cards;
}
