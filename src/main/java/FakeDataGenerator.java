import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.example.Transliteration.transliterate;


public class FakeDataGenerator {

    // Путь к файлу CSV
    private static final String CSV_FILE_PATH = "data.csv";

    // Размер пула для генерации данных
    private static final int POOL_SIZE = 10000;

    // Формат даты
    private static final String DATE_FORMAT = "dd.MM.yyyy";

    // Метод для записи в CSV
    public static void main(String[] args) {
        // Создание множества для хранения уникальных номеров телефонов
        Set<String> phoneNumbers = new HashSet<>();
        // Создание экземпляра генератора случайных чисел
        Random random = new Random();

        // Запись заголовка в файл
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(CSV_FILE_PATH), StandardCharsets.UTF_8))) {
            writer.write("ФИО;Дата рождения;Номер телефона;Электронная почта;СНИЛС\n");

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
                Date birthdate = dDate();
                String formattedBirthdate = new SimpleDateFormat(DATE_FORMAT).format(birthdate);

                // генерация рандомного номера телефона
                String phoneNumber = phone(phoneNumbers, random);
                phoneNumbers.add(phoneNumber);


                // транслиттерация имени и фамилии для email
                String newName = firstName.toLowerCase();
                newName = transliterate(newName);
                String newLastName = lastName.toLowerCase();
                newLastName = transliterate(newLastName);

                String email = email(newName, newLastName);
                String snils = snils(random);

                String csvLine = String.format("%s;%s;%s;%s;%s\n", fullName, formattedBirthdate, phoneNumber, email, snils);
                writer.write(csvLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


        private static final String PHONE_NUMBER_PREFIX = "8999";
        private static final int PHONE_NUMBER_LENGTH = 7;

        public static String phone(Set<String> phoneNumbers, Random random) {
            String phoneNumber;
            do {
                phoneNumber = PHONE_NUMBER_PREFIX + String.format("%0" + PHONE_NUMBER_LENGTH + "d", random.nextInt((int) Math.pow(10, PHONE_NUMBER_LENGTH)));
            } while (phoneNumbers.contains(phoneNumber));
            return phoneNumber;
        }

    private static final int MIN_AGE = 18;
    private static final int MAX_AGE = 99;

    public static Date dDate() {
        long minTimestamp = System.currentTimeMillis() - (MAX_AGE * 365L * 24 * 60 * 60 * 1000);
        long maxTimestamp = System.currentTimeMillis() - (MIN_AGE * 365L * 24 * 60 * 60 * 1000);
        long randomTimestamp = minTimestamp + (long) (Math.random() * (maxTimestamp - minTimestamp));
        return new Date(randomTimestamp);
    }

    private static final String EMAIL_DOMAIN = "example.com";

    public static String email(String resultName, String resultLastName) {
        String email = resultName.toLowerCase() + "." + resultLastName.toLowerCase() + "@" + EMAIL_DOMAIN;
        return email;
    }

    public static String snils(Random random) {
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
