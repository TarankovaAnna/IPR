package org.example;

import java.util.Random;
import java.util.Set;

public class PhoneNumber {
    private static final String PHONE_NUMBER_PREFIX = "8999";
    private static final int PHONE_NUMBER_LENGTH = 7;

    public static String generateUniquePhoneNumber(Set<String> phoneNumbers, Random random) {
        String phoneNumber;
        do {
            phoneNumber = PHONE_NUMBER_PREFIX + String.format("%0" + PHONE_NUMBER_LENGTH + "d", random.nextInt((int) Math.pow(10, PHONE_NUMBER_LENGTH)));
        } while (phoneNumbers.contains(phoneNumber));
        return phoneNumber;
    }
}
