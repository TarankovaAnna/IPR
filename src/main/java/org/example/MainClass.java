package org.example;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.example.GrafanaDataFetcher.*;

public class MainClass {
    //Типы сообщений, по которым будет собираться статистика
    protected static final String[] MESSAGE_TYPE_IDS = {"10", "11", "12", "13", "15", "16", "22", "23", "24", "25", "29", "30", "32", "33", "34", "37", "39", "40", "41", "45", "46", "50", "51", "52", "55", "60", "61", "62", "63", "70", "73", "111", "91", "92", "94", "95", "96", "97", "98", "99", "89", "102", "103", "104", "105", "106", "107", "108", "109", "110", "112", "113", "114", "115", "116", "117", "118", "119", "120", "121", "122", "123", "124", "125", "126", "127"};
//    protected static final String[] MESSAGE_TYPE_IDS = {"60", "61", "70", "80", "90", "91", "92", "93", "94", "95", "96", "97", "100", "101", "102", "103", "108"};

    // Статическое поле для хранения идентификаторов критически важных сообщений
    protected static final int[] SPECIFIC_MESSAGE_TYPE_IDS = {111, 62, 52, 73};
    // Процент попадания в профиль
    protected static final int CUMULATIVE_PERCENTAGE = 99;
    //отклонение от среднего значения для исключения дней с нетипичной нагрузкой
    protected static final double DEVIATION = 0.5; //0.5 - 20% отклонение от среднего значения, 0.4 ~15%
    //URL Grafana
    protected static final String BASE_URL = "https://grafana.psi.tcsbank.ru";
    //Путь к файлу с профилем
    protected static final String FILE_PATH = "U:\\Profile_creator\\data.xlsx";
    //api для получения данных по запросам
    protected static final String DATA_API = "/api/ds/query";
    protected static final String TAG_BANK = "BANK";
    //Период сбора статистики
    protected static final String DATE_FROM = "2024-12-01T21:00:00.000Z";
    protected static final String DATE_TO = "2024-12-28T23:59:59.000Z";
    //timestamp периода сбора статистики
    protected static final String TIMESTAMP_FROM = "1733000400000";
    protected static final String TIMESTAMP_TO = "1735419599000";
    //grafana_session, получаем из запроса и подставляем вручную
    private static final String GRAFANA_SESSION = "b372d57e6e88fa2fbb35fa6cc5b8562c";


    public static void main(String[] args) throws IOException {
        //Создаем HTTP-клиент с использованием библиотеки Apache HttpClient
        CloseableHttpClient client = HttpClients.createDefault();

        try {
        //Формируем строку cookie для авторизации на сервере Grafana
            String sessionCookie = "grafana_session=\"" + GRAFANA_SESSION + "\"";
        //Создаем новый Excel-файл (Workbook) с использованием библиотеки Apache POI
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Данные");

            // Создаем заголовки
            createHeaderRow(sheet);

            int rowNum = 1;
            //Цикл по массиву MESSAGE_TYPE_IDS
            for (int i = 0; i < MESSAGE_TYPE_IDS.length; i++) {
                String messageTypeID = MESSAGE_TYPE_IDS[i];

                // Получаем данные
                String rawData = getData(client, sessionCookie, messageTypeID, TAG_BANK);

                // Парсим и добавляем данные в Excel
                parseAndAddData(workbook, sheet, rowNum, rawData, messageTypeID);
                rowNum += countRows(rawData);

                // Обновляем прогресс
                printProgress(i + 1, MESSAGE_TYPE_IDS.length); // Передаем текущий индекс + 1 и общее количество
            }

            // Обрабатываем данные в Excel
            ExcelDataProcessor.processExcelData(workbook);

            // Сохраняем файл
            saveWorkbook(workbook, FILE_PATH);

            System.out.println("Данные успешно сохранены в Excel-файл");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } finally {
            client.close();
        }
    }

    //Медод для вывода строки прогресса
    private static void printProgress(int current, int total) {
        int percent = (int) ((double) current / total * 100);
        StringBuilder progressBar = new StringBuilder("[");
        int progressWidth = 50; // Ширина прогресс-бара
        int progress = (int) ((double) current / total * progressWidth);

        for (int i = 0; i < progressWidth; i++) {
            if (i < progress) {
                progressBar.append("=");
            } else {
                progressBar.append(" ");
            }
        }
        progressBar.append("] ").append(percent).append("%");

        // Перемещаем курсор в начало строки для обновления
        System.out.print("\r" + "Выгружаем статистику по запросам... " + progressBar);

    }

    //Метод для сохранения Excel-файла
    private static void saveWorkbook(Workbook workbook, String filePath) {
        try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
            workbook.write(outputStream);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при сохранении Excel-файла", e);
        }
    }

    //Медод для создания заголовков
    private static void createHeaderRow(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Дата");
        headerRow.createCell(1).setCellValue("Час");
        headerRow.createCell(2).setCellValue("День недели");
        headerRow.createCell(3).setCellValue("rph");
        headerRow.createCell(4).setCellValue("messageTypeId");
    }
}