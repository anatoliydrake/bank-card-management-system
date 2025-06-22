package com.example.bankcards.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("User with ID " + id + " not found");
    }
    public UserNotFoundException(String username) {
        super("User " + username + " not found");
    }
}
