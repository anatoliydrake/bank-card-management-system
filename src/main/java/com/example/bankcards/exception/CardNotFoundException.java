package com.example.bankcards.exception;

public class CardNotFoundException extends RuntimeException {
    public CardNotFoundException(Long id) {
        super("Card with ID " + id + " not found");
    }
}
