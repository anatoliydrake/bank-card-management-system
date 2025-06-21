package com.example.bankcards.dto;

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
    private String username;
    private String password;
    private String passportNumber;
    private Set<String> roles;
    private List<CardDto> cards;
}
