package com.example.bankcards.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UserUpdateDto {
    @Size(min = 4, message = "Username must be at least 4 characters")
    private String username;

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @Pattern(regexp = "\\d{10}", message = "Passport number must be 10 digits")
    private String passportNumber;

    @Size(min = 1, message = "At least one role must be provided")
    private Set<String> roles;
}
