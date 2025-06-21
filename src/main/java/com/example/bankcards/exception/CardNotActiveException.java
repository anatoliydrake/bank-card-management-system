package com.example.bankcards.exception;

public class CardNotActiveException extends RuntimeException {
    public CardNotActiveException(String cardNumber) {
        super("Card " + cardNumber + " is not active.");
    }
}
