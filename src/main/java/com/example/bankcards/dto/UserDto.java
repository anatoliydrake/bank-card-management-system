package com.example.bankcards.dto;

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
    private Long id;

    @NotBlank(message = "Username must not be blank")
    @Size(min = 4, message = "Username must be at least 4 characters")
    private String username;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 4, message = "Password must be at least 4 characters")
    private String password;

    @NotEmpty(message = "User must have at least one role")
    private Set<String> roles;

    private List<CardDto> cards;
}
