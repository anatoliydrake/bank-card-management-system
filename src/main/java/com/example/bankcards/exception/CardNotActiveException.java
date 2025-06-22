package com.example.bankcards.exception;

public class CardNotActiveException extends RuntimeException {
    public CardNotActiveException(Long id) {
        super("Card with ID: " + id + " is not active.");
    }
}
