package com.example.bankcards.exception;

public class DuplicatePassportException extends RuntimeException {
    public DuplicatePassportException(String passportNumber) {
        super("User with passport number " + passportNumber + " already exists");
    }
}
