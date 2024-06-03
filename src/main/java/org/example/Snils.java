package org.example;

import java.util.Random;

public class Snils {
    public static String generateRandomSnils(Random random) {
        StringBuilder snils = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            snils.append(random.nextInt(10));
        }

        int controlNumber = calculateControlNumber(snils.toString());
        snils.append(String.format("%02d", controlNumber));

        return formatSnils(snils.toString());
    }

    // метод для проверки контрольной суммы
    public static int calculateControlNumber(String snils) {
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += Character.getNumericValue(snils.charAt(i)) * (9 - i);
        }

        int controlNumber = sum % 101;
        if (controlNumber == 100) {
            controlNumber = 0;
        }

        return controlNumber;
    }

    // метод для формирования номера снилс
    public static String formatSnils(String snils) {
        StringBuilder formattedSnils = new StringBuilder();
        formattedSnils.append(snils.substring(0, 3));
        formattedSnils.append("-");
        formattedSnils.append(snils.substring(3, 6));
        formattedSnils.append("-");
        formattedSnils.append(snils.substring(6, 9));
        formattedSnils.append(" ");
        formattedSnils.append(snils.substring(9, 11));
        return formattedSnils.toString();
    }
}
