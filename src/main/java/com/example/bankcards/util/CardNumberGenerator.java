package com.example.bankcards.util;

import java.security.SecureRandom;

public class CardNumberGenerator {
    private static final SecureRandom random = new SecureRandom();
    private static final String IIN = "220220";
    private static final int NUMBER_LENGTH = 16;

    private CardNumberGenerator() {
    }

    public static String generateCardNumber() {
        StringBuilder cardNumber = new StringBuilder(IIN);
        while (cardNumber.length() < NUMBER_LENGTH - 1) {
            cardNumber.append(random.nextInt(10));
        }
        int checkDigit = getLuhnCheckDigit(cardNumber.toString());
        cardNumber.append(checkDigit);
        return cardNumber.toString();
    }

    private static int getLuhnCheckDigit(String number) {
        int sum = 0;
        boolean alternate = true;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = number.charAt(i) - '0';
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (10 - (sum % 10)) % 10;
    }
}
