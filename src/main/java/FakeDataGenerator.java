import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;


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
        List<String> data = new ArrayList<>();  // Объявление списка для хранения данных
        // Создание экземпляра генератора случайных чисел
        Random random = new Random();
        Scanner scanner = new Scanner(System.in);  // Создание экземпляра Scanner для ввода данных
        System.out.print("Введите количество строк: ");  // Приглашение для ввода числа строк
        int numLines = scanner.nextInt();  // Считывание введенного числа строк
        scanner.nextLine();  // Очистка буфера после считывания числа

        // Запись заголовка в файл
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(CSV_FILE_PATH), StandardCharsets.UTF_8))) {
            writer.write("ФИО;Дата рождения;Номер телефона;Электронная почта;СНИЛС\n");

            for (int i = 0; i < numLines; i++) {


                String fullName = Name();


                // генерация рандомной даты рождения
              //  Date birthdate = BDate();
              //  String formattedBirthdate = new SimpleDateFormat("dd.MM.yyyy").format(birthdate);

                // генерация рандомного номера телефона
                String phoneNumber = Phone(phoneNumbers, random);
                phoneNumbers.add(phoneNumber);

                String firstName = fullName.split(" ")[0];
                String lastName = fullName.split(" ")[1];
                // транслиттерация имени и фамилии для email
                String newName = firstName.toLowerCase();
                newName = transliterate(newName);
                // String newLastName = lastName.toLowerCase();
              //  newLastName = transliterate(newLastName);

                //String email = Email(newName, newLastName);
               // String snils = Snils(random);

                data.add(String.format("%s;%s;%s;%s;%s", fullName, BDate(), Phone(phoneNumbers, random), Email(newName), Snils(random)));
                for (String dataItem : data) {
                    writer.write(dataItem + "\n");  // Запись каждого элемента списка в файл
                }
                // Запись в CSV-файл
              //  String csvLine = String.format("%s;%s;%s;%s;%s\n", fullName, formattedBirthdate, Phone(phoneNumbers, random), Email(newName, newLastName), Snils(random));
               // writer.write(csvLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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


    private static final String PHONE_NUMBER_PREFIX = "+7999";
    private static final int PHONE_NUMBER_LENGTH = 7;

    public static String Phone(Set<String> phoneNumbers, Random random) {
        String phoneNumber;
        do {
            phoneNumber = PHONE_NUMBER_PREFIX + String.format("%0" + PHONE_NUMBER_LENGTH + "d", random.nextInt((int) Math.pow(10, PHONE_NUMBER_LENGTH)));
        } while (phoneNumbers.contains(phoneNumber));
        return phoneNumber;
    }

    private static final int MIN_AGE = 18;
    private static final int MAX_AGE = 99;

    public static String BDate() {
        long minTimestamp = System.currentTimeMillis() - (MAX_AGE * 365L * 24 * 60 * 60 * 1000);
        long maxTimestamp = System.currentTimeMillis() - (MIN_AGE * 365L * 24 * 60 * 60 * 1000);
        long randomTimestamp = minTimestamp + (long) (Math.random() * (maxTimestamp - minTimestamp));
        String formattedBirthdate = new SimpleDateFormat("dd.MM.yyyy").format(randomTimestamp);
        return formattedBirthdate;
    }

    private static final String EMAIL_DOMAIN = "example.ru";

    public static String Email(String resultName) {
        String email = resultName.toLowerCase()  + "@" + EMAIL_DOMAIN;
        return email;
    }

    public static String Snils(Random random) {
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
        if (controlNumber == 100 || controlNumber == 101) {
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
