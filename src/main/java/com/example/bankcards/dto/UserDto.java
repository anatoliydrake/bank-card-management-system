package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Role;
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
    private String userName;
    private String password;
    private Set<Role> roles;
    private List<Card> cards;
}
