import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;


public class FakeDataGenerator {

    // Путь к файлу CSV
    private static final String CSV_FILE_PATH = "data.csv";
    private static final String PHONE_NUMBER_PREFIX = "8999";
    private static final int PHONE_NUMBER_LENGTH = 7;
    private static final int MIN_AGE = 18;
    private static final int MAX_AGE = 99;
    private static final String EMAIL_DOMAIN = "mail.ru";

    // Метод для записи в CSV
    public static void main(String[] args) {
        // Создание множества для хранения уникальных номеров телефонов
        Set<String> phoneNumbers = new HashSet<>();
        // Объявление списка для хранения данных
        List<String> data = new ArrayList<>();

        // Создание экземпляра Scanner для ввода данных
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите количество строк: ");  // Приглашение для ввода числа строк
        int numLines = scanner.nextInt();  // Считывание введенного числа строк
        scanner.nextLine();  // Очистка буфера после считывания числа

        // Запись заголовка в файл
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(CSV_FILE_PATH), StandardCharsets.UTF_8))) {
            writer.write("ФИО;Дата рождения;Номер телефона;Электронная почта;СНИЛС\n");

            for (int i = 0; i < numLines; i++) {

                String fullName = Name();

                data.add(String.format("%s;%s;%s;%s;%s", fullName, BDate(), Phone(), Email(), Snils()));

            }
            for (String dataItem : data) {
                // Запись каждого элемента списка в файл
                writer.write(dataItem + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // метод для генерации имени
    public static String Name() {
        Random random = new Random();
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
        return firstName + " " + lastName + " " + patronymicName;
    }


    // метод для генерации номера телефона
    public static String Phone() {
        Set<String> phoneNumbers = new HashSet<>();  // Создание множества для хранения уникальных номеров телефонов
        Random random = new Random();  // Создание экземпляра Random

        String phoneNumber;
        do {
            phoneNumber = PHONE_NUMBER_PREFIX + String.format("%0" + PHONE_NUMBER_LENGTH + "d", random.nextInt((int) Math.pow(10, PHONE_NUMBER_LENGTH)));
        } while (phoneNumbers.contains(phoneNumber));
        return phoneNumber;
    }


    // метод для генерации даты рождения
    public static String BDate() {
        long minTimestamp = System.currentTimeMillis() - (MAX_AGE * 365L * 24 * 60 * 60 * 1000);
        long maxTimestamp = System.currentTimeMillis() - (MIN_AGE * 365L * 24 * 60 * 60 * 1000);
        long randomTimestamp = minTimestamp + (long) (Math.random() * (maxTimestamp - minTimestamp));
        String formattedBirthdate = new SimpleDateFormat("dd.MM.yyyy").format(randomTimestamp);
        return formattedBirthdate;
    }


    // метод для генерации email
    public static String Email() {
        String resultName = generateRandomName();  // Генерация случайного имени для почты
        String email = resultName.toLowerCase() + "@" + EMAIL_DOMAIN;
        return email;
    }

    // Метод для генерации случайного названия email
    private static String generateRandomName() {
        // Реализация логики для генерации случайного имени
        return "user" + (new Random().nextInt(1000)) + "example";
    }

    // метод для генерации снилс
    public static String Snils() {
        Random random = new Random();
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
        // Вычисляем контрольное число
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
