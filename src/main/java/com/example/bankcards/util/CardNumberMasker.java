package com.example.bankcards.util;

public class CardNumberMasker {
    private CardNumberMasker() {
    }

    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() != 16) {
            throw new IllegalArgumentException("Invalid card number");
        }
        return "**** **** **** ".concat(cardNumber.substring(cardNumber.length() - 4));
    }
}
