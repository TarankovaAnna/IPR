package org.example;

import java.util.Date;

public class Birthdate {
    private static final int MIN_AGE = 18;
    private static final int MAX_AGE = 99;

    public static Date generateRandomBirthdate() {
        long minTimestamp = System.currentTimeMillis() - (MAX_AGE * 365L * 24 * 60 * 60 * 1000);
        long maxTimestamp = System.currentTimeMillis() - (MIN_AGE * 365L * 24 * 60 * 60 * 1000);
        long randomTimestamp = minTimestamp + (long) (Math.random() * (maxTimestamp - minTimestamp));
        return new Date(randomTimestamp);
    }
}
