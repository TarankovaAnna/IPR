package org.example;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.example.Birthdate.generateRandomBirthdate;
import static org.example.Email.generateRandomEmail;
import static org.example.PhoneNumber.generateUniquePhoneNumber;
import static org.example.Snils.generateRandomSnils;
import static org.example.Transliteration.transliterate;


public class Generator {

    // Путь к файлу CSV
    private static final String CSV_FILE_PATH = "data.csv";

    // Размер пула для генерации данных
    private static final int POOL_SIZE = 10000;

    // Формат даты
    private static final String DATE_FORMAT = "dd.MM.yyyy";

    // Метод для записи в CSV
    public static void writeToCsv(String[] args) {
        // Создание множества для хранения уникальных номеров телефонов
        Set<String> phoneNumbers = new HashSet<>();
        // Создание экземпляра генератора случайных чисел
        Random random = new Random();

        // Запись заголовка в файл
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(CSV_FILE_PATH), StandardCharsets.UTF_8))) {
            writer.write("ФИО;Дата рождения;Номер телефона;Электронная почта;СНИЛС\n");

            // Цикл для генерации данных
            for (int i = 0; i < POOL_SIZE; i++) {

                String characters = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя";
                StringBuilder randomString = new StringBuilder();

                // формирование рандомного имени
                int nameLength = 4 + random.nextInt(7);  // Генерация случайной длины от 4 до 10
                for (int y = 0; y < nameLength; y++) {
                    char randomChar = characters.charAt(random.nextInt(characters.length()));
                    randomString.append(y == 0 ? Character.toUpperCase(randomChar) : Character.toLowerCase(randomChar));
                }
                String firstName = randomString.toString();

                // формирование рандомной фамилии
                StringBuilder randomStringLastName = new StringBuilder();

                int lastNameLength = 6 + random.nextInt(9);  // Генерация случайной длины от 4 до 10
                for (int y = 0; y < lastNameLength; y++) {
                    char randomChar = characters.charAt(random.nextInt(characters.length()));
                    randomStringLastName.append(y == 0 ? Character.toUpperCase(randomChar) : Character.toLowerCase(randomChar));
                }
                String lastName = randomStringLastName.toString();

                // формирование рандомного отчества
                StringBuilder randomStringPatronymicName = new StringBuilder();

                int patronymicNameLength = 8 + random.nextInt(11);  // Генерация случайной длины от 4 до 10
                for (int y = 0; y < patronymicNameLength; y++) {
                    char randomChar = characters.charAt(random.nextInt(characters.length()));
                    randomStringPatronymicName.append(y == 0 ? Character.toUpperCase(randomChar) : Character.toLowerCase(randomChar));
                }
                String patronymicName = randomStringPatronymicName.toString();

                String fullName = firstName + " " + patronymicName + " " + lastName;


                // генерация рандомной даты рождения
                Date birthdate = generateRandomBirthdate();
                String formattedBirthdate = new SimpleDateFormat(DATE_FORMAT).format(birthdate);

                // генерация рандомного номера телефона
                String phoneNumber = generateUniquePhoneNumber(phoneNumbers, random);
                phoneNumbers.add(phoneNumber);


                // транслиттерация имени и фамилии для email
                String newName = firstName.toLowerCase();
                newName = transliterate(newName);
                String newLastName = lastName.toLowerCase();
                newLastName = transliterate(newLastName);

                String email = generateRandomEmail(newName, newLastName);
                String snils = generateRandomSnils(random);

                String csvLine = String.format("%s;%s;%s;%s;%s\n", fullName, formattedBirthdate, phoneNumber, email, snils);
                writer.write(csvLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
