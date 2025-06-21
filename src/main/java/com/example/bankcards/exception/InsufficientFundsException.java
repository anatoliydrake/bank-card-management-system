package com.example.bankcards.exception;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String cardNumber) {
        super("Card " + cardNumber + " has insufficient funds for the operation");
    }
}
