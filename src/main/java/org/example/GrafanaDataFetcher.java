package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.poi.ss.usermodel.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import static org.example.MainClass.*;

public class GrafanaDataFetcher {

    //Получаем данные из указанного API
    protected static String getData(CloseableHttpClient client, String sessionCookie, String messageTypeID, String tag) throws IOException, URISyntaxException {
        // Конструируем URI для API-запроса
        URI uri = new URI(BASE_URL + DATA_API);
        HttpPost post = new HttpPost(uri);
        // Устанавливаем необходимые заголовки для HTTP POST запроса
        post.setHeader("Content-Type", "application/json");
        post.setHeader("Cookie", sessionCookie);
        // Определяем значение job в зависимости от переданного тега
        String jobValue = tag.equals(TAG_BANK) ? "garmr-iris" : "iris";
        // Конструируем JSON-тело для запроса
        String jsonBody = "{"
                + "\"queries\": ["
                + "{"
                + "\"datasource\": {\"uid\": \"victoriametrics_cluster\", \"type\": \"prometheus\"},"
                + "\"editorMode\": \"code\","
                + "\"expr\": \"" +
                (Arrays.asList("50", "71").contains(messageTypeID) ?
                        "sum(increase(iris_nohup_total{message=\\\"ModelRequest\\\", messageTypeId=\\\"" + messageTypeID + "\\\"}))" :
                        "sum(increase(iris_nohup_total{message=\\\"ModelRequest\\\", messageTypeId=\\\"" + messageTypeID + "\\\", job=\\\"" + jobValue + "\\\", tag=\\\"" + tag + "\\\"}))") +
                "\","
                + "\"legendFormat\": \"req\","
                + "\"range\": true,"
                + "\"refId\": \"A\","
                + "\"queryType\": \"timeSeriesQuery\","
                + "\"exemplar\": false,"
                + "\"utcOffsetSec\": 10800,"
                + "\"intervalMs\": 3600000,"
                + "\"maxDataPoints\": 3000"
                + "}"
                + "],"
                + "\"range\": {"
                + "\"from\": \"" + DATE_FROM + "\","
                + "\"to\": \"" + DATE_TO + "\","
                + "\"raw\": {"
                + "\"from\": \"now-90d\","
                + "\"to\": \"now\""
                + "}"
                + "},"
                + "\"from\": \"" + TIMESTAMP_FROM + "\","
                + "\"to\": \"" + TIMESTAMP_TO + "\""
                + "}";

        // Устанавливаем JSON-тело
        // System.out.println(jsonBody);
        post.setEntity(new StringEntity(jsonBody));
        / Выполняем HTTP POST запрос и обрабатываем ответ
        try (CloseableHttpResponse response = client.execute(post)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity());

//            System.out.println("Статус код: " + statusCode);
//            System.out.println("Ответ сервера:");
//            System.out.println(responseBody);
            // Проверяем, успешен ли ответ по коду статуса
            if (statusCode == 200) {
                return responseBody;
            } else {
                throw new IOException("Ошибка при запросе данных: " + statusCode + " - " + responseBody);
            }
        }
    }

    // Метод для парсинга полученных данных и добавления их в Excel-таблицу
    protected static void parseAndAddData(Workbook workbook, Sheet sheet, int startRow, String rawData, String messageTypeId) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        // Парсим сырые данные JSON
        JsonNode root = mapper.readTree(rawData);
        // Извлекаем results из распарсенного JSON
        JsonNode results = root.get("results");
        if (results != null && results.isObject()) {
            JsonNode frames = results.get("A").get("frames");
            if (frames != null && frames.isArray() && !frames.isEmpty()) {
                JsonNode frame = frames.get(0);
                JsonNode data = frame.get("data");
                JsonNode values = data.get("values");
                // Проверяем, содержат ли значения временные метки и фактические данные
                if (values != null && values.isArray()) {
                    JsonNode timestamps = values.get(0); // Получаем массив временных меток
                    JsonNode valuesArray = values.get(1); // Получаем массив фактических данных
                    // Убеждаемся, что оба массива одинакового размера
                    if (timestamps.isArray() && valuesArray.isArray() && timestamps.size() == valuesArray.size()) {
                        int currentRow = startRow;
                        for (int i = 0; i < timestamps.size(); i++) {
                            long timestamp = timestamps.get(i).asLong();
                            int value = valuesArray.get(i).asInt();
                            // Форматируем временную метку в дату и время
                            String[] formattedDateTime = formatTimestamp(timestamp);
                            String shortDayOfWeek = mapDayOfWeek(getDayOfWeek(formattedDateTime[0]));
                            // Создаем новую строку в Excel и заполняем ее данными
                            Row dataRow = sheet.createRow(currentRow++);
                            dataRow.createCell(0).setCellValue(formattedDateTime[0]);
                            dataRow.createCell(1).setCellValue(Integer.parseInt(formattedDateTime[1].split(":")[0]));
                            dataRow.createCell(2).setCellValue(shortDayOfWeek);
                            dataRow.createCell(3).setCellValue(value);
                            dataRow.createCell(4).setCellValue(messageTypeId);


//                            System.out.println("Добавлена строка данных: Дата=" + formattedDateTime[0] + ", Час=" + Integer.parseInt(formattedDateTime[1].split(":")[0]) +
//                                    ", День недели =" + shortDayOfWeek + ", Значение=" + value + ", messageTypeId=" + messageTypeId);
                        }
                    } else {
                        System.err.println("Несоответствие количества временных меток и значений");
                    }
                }
            }
        }
    }

    // Метод для подсчета количества строк в полученных данных
    protected static int countRows(String rawData) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(rawData);
        JsonNode results = root.get("results");
        if (results != null && results.isObject()) {
            JsonNode frames = results.get("A").get("frames");
            if (frames != null && frames.isArray() && !frames.isEmpty()) {
                JsonNode frame = frames.get(0);
                JsonNode data = frame.get("data");
                JsonNode values = data.get("values");
                if (values != null && values.isArray()) {
                    JsonNode timestamps = values.get(0);
                    return timestamps.size();
                }
            }
        }
        return 0;
    }

    // Метод для форматирования временной метки в строки даты и времени
    private static String[] formatTimestamp(long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        Date date = new Date(timestamp);
        return new String[]{dateFormat.format(date), timeFormat.format(date)};
    }

    // Метод для получения дня недели из строки даты
    private static String getDayOfWeek(String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = dateFormat.parse(dateString);
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", new java.util.Locale("en"));
            return dayFormat.format(date);
        } catch (ParseException e) {
            return "";
        }
    }

    // Метод для переформатирования дня недели
    private static String mapDayOfWeek(String dayOfWeek) {
        switch (dayOfWeek) {
            case "Monday":
                return "MONDAY";
            case "Tuesday":
                return "TUESDAY";
            case "Wednesday":
                return "WEDNESDAY";
            case "Thursday":
                return "THURSDAY";
            case "Friday":
                return "FRIDAY";
            case "Saturday":
                return "SATURDAY";
            case "Sunday":
                return "SUNDAY";
            default:
                return dayOfWeek;
        }
    }
}
