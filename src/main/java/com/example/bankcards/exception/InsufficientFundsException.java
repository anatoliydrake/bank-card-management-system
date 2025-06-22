package com.example.bankcards.exception;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(Long id) {
        super("Card ID: " + id + " has insufficient funds for the operation");
    }
}
