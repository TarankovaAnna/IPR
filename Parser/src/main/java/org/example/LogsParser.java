package org.example;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;

public class LogsParser {
    public static void main(String[] args) {
        try {
            // Открытие и чтение файла "logs.xlsx"
            File file = new File("C:\\projects\\parser\\Parser\\src\\data\\logs_nginx.xlsx");
            FileInputStream fis = new FileInputStream(file);

            // Создание объекта Workbook для представления Excel-файла
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);

            // Создание нового файла Excel
            Workbook newWorkbook = new XSSFWorkbook();
            Sheet newSheet = newWorkbook.createSheet("ParserData");

            int rowNum = 0;
            // Обработка каждой строки в исходном файле
            for (Row row : sheet) {
                Row newRow = newSheet.createRow(rowNum);
                Cell cell = row.getCell(0); // Получение только первой ячейки в строке
                if (cell != null) {
                    // Разделение содержимого ячейки на значения по пробелу
                    String[] values = cell.getStringCellValue().split(" ");
                    // Проверка на наличие необходимых значений
                    if (values.length > 7) {
                        String date = values[3].replace("[", "");
                        String time = values[4];
                        String request = values[7].replace("\"", ""); // Сборка запроса из оставшихся значений

                        int cellNum = 0;
                        // Запись данных в новый файл Excel
                        Cell newCell1 = newRow.createCell(cellNum++);
                        newCell1.setCellValue(date);
                        Cell newCell2 = newRow.createCell(cellNum++);
                        newCell2.setCellValue(time);
                        Cell newCell3 = newRow.createCell(cellNum);
                        newCell3.setCellValue(request);
                    }
                }
                rowNum++;
            }

            // Запись данных в файл parser.xlsx
            FileOutputStream outputStream = new FileOutputStream("C:\\projects\\first\\untitled1\\src\\data\\parser.xlsx");
            newWorkbook.write(outputStream);
            newWorkbook.close();
            outputStream.close();

            workbook.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
