package com.example.bankcards.exception;

public class DuplicateUsernameException extends RuntimeException {
    public DuplicateUsernameException(String username) {
        super("Username '" + username + "' already exists");
    }
}
