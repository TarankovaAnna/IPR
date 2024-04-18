package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class LogsParser {
    public static void parseLogs(String inputFilePath, String outputFilePath) {
        try {
            // Открытие и чтение файла "logs_nginx.xlsx"
            FileInputStream fis = new FileInputStream(new File(inputFilePath));
            // Создание экземпляра книги Excel из потока ввода.
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);

            // Создание карты для хранения данных в формате "ключ-значение"
            Map<String, Integer> countMap = new HashMap<>();

            //Обход строк листа Excel
            for (Row row : sheet) {
                // Получение ячейки в первом столбце текущей строки
                Cell cell = row.getCell(0);
                if (cell != null) {
                    String[] values = cell.getStringCellValue().split(" "); // Разделение содержимого ячейки на отдельные значения по пробелу
                    if (values.length > 7) {
                        // Извлечение нужных данных
                        String date = values[3].replace("[", ""); // Извлечение даты
                        String time = values[4]; //Извлечение времени
                        String request = values[7].replace("\"", ""); // Извлечение запроса
                        String hours = time.substring(0, 2); // Извлечение часа из времени
                        // Обработка запросов по шаблону
                        if (request.matches("/tickets/\\d+/")) {
                            request = "/tickets/_tick";
                        } else if (request.matches("/tickets/\\d+/update/")) {
                            request = "/tickets/_tick_/update/";
                        } else if (request.startsWith("/datatables_ticket_list/")) {
                            request = "/datatables_ticket_list/";
                        }

                        // Формирование ключа для карты данных.
                        String key = date + " " + hours + " " + request;
                        //Суммирование количества запросов по дням и часам
                        countMap.put(key, countMap.getOrDefault(key, 0) + 1);
                    }
                }
            }
            // Создание нового файла Excel
            Workbook newWorkbook = new XSSFWorkbook();
            //Создание нового листа в новой книге Excel.
            Sheet newSheet = newWorkbook.createSheet("ParserData");
            int rowNum = 0;

            // Извлечение данных из countMap и запись в файл
            for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
                String[] values = entry.getKey().split(" ");
                String date = values[0];
                String hours = values[1];
                String request = values[2];
                SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy"); // Формат даты
                Date parsedDate = format.parse(date); // Преобразование строки в дату
                SimpleDateFormat newFormat = new SimpleDateFormat("EEEE"); // Формат для получения дня недели
                String dayOfWeek = newFormat.format(parsedDate); // Получение дня недели из даты
                int count = entry.getValue();

                // Создание первой строки с названиями колонок
                Row headerRow = newSheet.createRow(0);
                headerRow.createCell(0).setCellValue("Дата");
                headerRow.createCell(1).setCellValue("Час");
                headerRow.createCell(2).setCellValue("Запрос");
                headerRow.createCell(3).setCellValue("Количество");
                headerRow.createCell(4).setCellValue("День");

                //Создание новой строки для записи данных.
                Row newRow = newSheet.createRow(rowNum);
                Cell newCell1 = newRow.createCell(0);
                newCell1.setCellValue(date);
                Cell newCell2 = newRow.createCell(1);
                newCell2.setCellValue(hours);
                Cell newCell3 = newRow.createCell(2);
                newCell3.setCellValue(request);
                Cell newCell4 = newRow.createCell(3);
                newCell4.setCellValue(count);
                Cell newCell5 = newRow.createCell(4);
                newCell5.setCellValue(dayOfWeek);


                rowNum++;
            }

            FileOutputStream outputStream = new FileOutputStream(outputFilePath);
            newWorkbook.write(outputStream);
            newWorkbook.close();
            outputStream.close();

            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
