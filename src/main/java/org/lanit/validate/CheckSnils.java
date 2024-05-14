package org.lanit.validate;

import org.springframework.stereotype.Component;

import java.io.IOException;

@Component // Аннотация, указывающая, что этот класс является компонентом, управляемым контейнером Spring
public class CheckSnils {
    public boolean isValidSnils(String snils) throws IOException {

        // Удаляем все символы, кроме цифр
        snils = snils.replaceAll("\\D", "");

        // Проверяем, что длина СНИЛС равна 11
        if (snils.length() != 11) {
            return false;
        }

        // Вычисляем контрольное число
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += Character.getNumericValue(snils.charAt(i)) * (9 - i);
        }
        int controlNumber = sum % 101;
        if (controlNumber == 100) {
            controlNumber = 0;
        }

        // Проверяем контрольное число
        int expectedControlNumber = Integer.parseInt(snils.substring(9));
        if (controlNumber == expectedControlNumber) {
            return true;
        } else {
            return false;
        }
    }
}