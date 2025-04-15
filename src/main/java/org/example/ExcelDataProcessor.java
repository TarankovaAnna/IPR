package org.example;

import org.apache.poi.ss.usermodel.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Sheet;

import static org.example.MainClass.*;


public class ExcelDataProcessor {
    public static void processExcelData(Workbook workbook) {

        System.out.print("\nФормируем профиль... ");

        Sheet sheet = workbook.getSheetAt(0); // Получаем первый лист
        // Создаем объект DateTimeFormatter для форматирования дат в формате yyyy-MM-dd
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // Карта для хранения общей суммы запросов по каждому дню недели
        Map<DayOfWeek, Long> totalRequestsMap = new EnumMap<>(DayOfWeek.class);
        // Карта для хранения количества записей (строк) по каждому дню недели
        Map<DayOfWeek, Integer> countMap = new EnumMap<>(DayOfWeek.class);

        for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row != null) {
                Cell dayCell = row.getCell(2); // Предполагаем, что день недели в третьем столбце
                Cell valueCell = row.getCell(3); // Предполагаем, что значение в четвертом столбце
                if (dayCell != null && valueCell != null) {
                    DayOfWeek day = DayOfWeek.valueOf(dayCell.getStringCellValue().toUpperCase());
                    long value = (long) valueCell.getNumericCellValue();
                    totalRequestsMap.put(day, totalRequestsMap.getOrDefault(day, 0L) + value);
                    countMap.put(day, countMap.getOrDefault(day, 0) + 1);
                }
            }
        }
        // Создаем новый лист в Excel-файле с названием "Среднее количество запросов"
        Sheet resultSheet = workbook.createSheet("Среднее количество запросов");

        // Создаем листы с результатами
        DayOfWeek maxRequestsDay = getMaxRequestsDay(totalRequestsMap);
        createAverageRequestsSheet(resultSheet, totalRequestsMap, countMap, maxRequestsDay);
        createMaxRequestsSheet(workbook.createSheet("Запросы в " + maxRequestsDay), sheet, maxRequestsDay, dateFormatter);
        createLoadChartSheet(workbook.createSheet("График нагрузки в " + maxRequestsDay), sheet, maxRequestsDay, dateFormatter);
        // Создаем сводную таблицу
        List<String> validDates = createSummarySheet(workbook.createSheet("Распр-е интенсивности по часам"), sheet, maxRequestsDay, dateFormatter);
        // Удаляем старый лист "Отфильтрованные данные", если он существует
        String filteredSheetName = "Отфильтрованные данные";
        int index = workbook.getSheetIndex(filteredSheetName);
        if (index != -1) {
            workbook.removeSheetAt(index);
        }
        // Создаем новый лист с отфильтрованными запросами
        Sheet filteredSheet = workbook.createSheet(filteredSheetName);
        createFilteredRequestsSheet(filteredSheet, sheet, maxRequestsDay, dateFormatter, validDates);

        // Создаем лист со сводной таблицей распределения нагрузки по часам
        Sheet pivotSheet = workbook.createSheet("Среднее по часам");
        // Создаем лист с максимальными значениями для профиля
        double maxSumValue = createPivotTable(pivotSheet, filteredSheet, workbook);
        createProfileSheet(pivotSheet, workbook.createSheet("Максимальные значения"), maxSumValue);

    }

    // Метод для создания и заполнения листа Excel данными о среднем количестве запросов по дням недели.
    private static void createAverageRequestsSheet(Sheet resultSheet, Map<DayOfWeek, Long> totalRequestsMap, Map<DayOfWeek, Integer> countMap, DayOfWeek maxRequestsDay) {
        // Заголовки результата
        int rowNum = 0;
        Row headerRow = resultSheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("День недели");
        headerRow.createCell(1).setCellValue("Среднее количество запросов");

        // Заполнение результатов
        for (DayOfWeek day : DayOfWeek.values()) {
            long totalRequests = totalRequestsMap.getOrDefault(day, 0L);
            int totalCount = countMap.getOrDefault(day, 0);

            double averageRequests = totalCount > 0 ? (double) totalRequests / totalCount : 0;

            Row row = resultSheet.createRow(rowNum++);
            row.createCell(0).setCellValue(day.toString());
            row.createCell(1).setCellValue(averageRequests);
        }
        // Добавление пустой строки перед информацией о максимальной нагрузке
        resultSheet.createRow(rowNum++); // Пустая строка

        // Добавление строки с информацией о максимальной нагрузке с переносом
        Row infoRow = resultSheet.createRow(rowNum++);
        String infoText = "По сводному графику по дням недели видно, что во " + maxRequestsDay + " нагрузка выше, чем в другие дни. \nРассмотрим детальней нагрузку в этот день.";
        Cell cell = infoRow.createCell(0);
        cell.setCellValue(infoText);

        // Добавление пустой строки
        resultSheet.createRow(rowNum++); // Пустая строка

        // Добавление строки с фразой для наглядности графика
        Row graphRow = resultSheet.createRow(rowNum++);
        graphRow.createCell(0).setCellValue("Для наглядности сформируйте график.");
    }

    // Метод для создания и заполнения листа Excel статистикой за пиковый день
    private static void createMaxRequestsSheet(Sheet maxRequestsSheet, Sheet sourceSheet, DayOfWeek maxRequestsDay, DateTimeFormatter dateFormatter) {
        int rowNum = 0;

        // Заголовки результата
        Row headerRow = maxRequestsSheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Дата");
        headerRow.createCell(1).setCellValue("Час");
        headerRow.createCell(2).setCellValue("День недели");
        headerRow.createCell(3).setCellValue("rph");
        headerRow.createCell(4).setCellValue("messageTypeId");
        headerRow.createCell(5).setCellValue(""); // Пустая колонка
        headerRow.createCell(6).setCellValue("На этом листе собрана статистика за " + maxRequestsDay); // Текст в последней ячейке заголовка

        // Заполнение результатов
        for (Row row : sourceSheet) {
            if (row.getRowNum() == 0) continue; // Пропускаем заголовок

            String dateStr = row.getCell(0).getStringCellValue();
            LocalDate date = LocalDate.parse(dateStr, dateFormatter);
            DayOfWeek dayOfWeek = date.getDayOfWeek();

            if (dayOfWeek == maxRequestsDay) {
                Row newRow = maxRequestsSheet.createRow(rowNum++);
                newRow.createCell(0).setCellValue(dateStr);
                newRow.createCell(1).setCellValue((int) row.getCell(1).getNumericCellValue());
                newRow.createCell(2).setCellValue(dayOfWeek.toString());
                newRow.createCell(3).setCellValue(getCellNumericValue(row.getCell(3)));
                newRow.createCell(4).setCellValue(getCellNumericValue(row.getCell(4)));

                // Добавление пустой ячейки в новую строку
                newRow.createCell(5).setCellValue(""); // Пустая колонка
            }
        }
    }

    // Метод для заполнения листа с суммарной статистикой по часам в пиковый день
    private static void createLoadChartSheet(Sheet loadChartSheet, Sheet sourceSheet, DayOfWeek maxRequestsDay, DateTimeFormatter dateFormatter) {
        int rowNum = 0;
        Row headerRow = loadChartSheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Час");
        headerRow.createCell(1).setCellValue("Количество запросов");
        headerRow.createCell(2).setCellValue(""); // Пустая колонка
        headerRow.createCell(3).setCellValue("На этом листе собрана суммарная статистика по часам за " + maxRequestsDay);

        // Словарь для хранения количества запросов по часам
        Map<Integer, Integer> hourRequestMap = new HashMap<>();

        // Заполнение словаря с количеством запросов по часам
        for (Row row : sourceSheet) {
            if (row.getRowNum() == 0) continue; // Пропускаем заголовок

            String dateStr = row.getCell(0).getStringCellValue();
            LocalDate date = LocalDate.parse(dateStr, dateFormatter);
            if (date.getDayOfWeek() == maxRequestsDay) {
                int hour = (int) row.getCell(1).getNumericCellValue();
                int value = getCellNumericValue(row.getCell(3));
                hourRequestMap.merge(hour, value, Integer::sum);
            }
        }

        // Заполнение листа данными по часам
        for (int hour = 0; hour < 24; hour++) {
            Row row = loadChartSheet.createRow(rowNum++);
            row.createCell(0).setCellValue(hour);
            row.createCell(1).setCellValue(hourRequestMap.getOrDefault(hour, 0));

        }

    }

    // Метод для создания и заполнения листа со сводной таблицей
    private static List<String> createSummarySheet(Sheet summarySheet, Sheet sourceSheet, DayOfWeek maxRequestsDay, DateTimeFormatter dateFormatter) {
        int rowNum = 0;
        Row headerRow = summarySheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Час");

        Map<String, Integer> dateRequestMap = new HashMap<>();

        // Инициализация карты дат
        for (Row row : sourceSheet) {
            if (row.getRowNum() == 0) continue; // Пропускаем заголовок
            String dateStr = row.getCell(0).getStringCellValue();
            LocalDate date = LocalDate.parse(dateStr, dateFormatter);
            if (date.getDayOfWeek() == maxRequestsDay) {
                dateRequestMap.put(dateStr, 0); // Инициализируем дату
            }
        }

        // Заполнение заголовков
        int columnNum = 1;
        for (String dateStr : dateRequestMap.keySet()) {
            headerRow.createCell(columnNum++).setCellValue(dateStr);
        }
        headerRow.createCell(columnNum).setCellValue("Среднее");

        // Заполнение данных по часам
        for (int hour = 0; hour < 24; hour++) {
            Row row = summarySheet.createRow(rowNum++);
            row.createCell(0).setCellValue(hour + ":00:00");
            columnNum = 1;
            double totalRequests = 0;
            int dateCount = 0;

            for (String dateStr : dateRequestMap.keySet()) {
                int requestCount = (int) getRequestCountForHour(sourceSheet, maxRequestsDay, LocalDate.parse(dateStr, dateFormatter), hour);
                row.createCell(columnNum).setCellValue(requestCount);
                totalRequests += requestCount;
                if (requestCount > 0) {
                    dateCount++;
                }
                columnNum++;
            }

            double averageRequests = dateCount > 0 ? totalRequests / dateCount : 0;
            row.createCell(columnNum).setCellValue(averageRequests);
        }

        // Заполнение итоговой строки
        Row totalRow = summarySheet.createRow(rowNum);
        totalRow.createCell(0).setCellValue("Среднее");
        columnNum = 1;
        double grandTotal = 0;

        for (String dateStr : dateRequestMap.keySet()) {
            double totalForDate = getRequestTotalForDate(sourceSheet, maxRequestsDay, LocalDate.parse(dateStr, dateFormatter));
            totalRow.createCell(columnNum).setCellValue(totalForDate);
            grandTotal += totalForDate;
            columnNum++;
        }
        totalRow.createCell(columnNum).setCellValue(grandTotal / dateRequestMap.size());

        summarySheet.createRow(rowNum++);
        // Добавление текста через одну строку после таблицы
        rowNum++; // Переход к следующей строке
        Row noteRow = summarySheet.createRow(rowNum++);
        noteRow.createCell(0).setCellValue("Для наглядности сформируйте график.");

        // Добавление пустой строки
        summarySheet.createRow(rowNum++); // Пустая строка

        // Добавление текста
        Row analysisRow = summarySheet.createRow(rowNum++);
        analysisRow.createCell(0).setCellValue("Рассмотрим график нагрузки по часам в разбивке по дням. \nВ некоторые дни нагрузка в отдельные часы не типичная. Исключим эти дни из анализа.");

        // Создание листа со статистикой за пиковый день недели за исключением дней с нетипичной нагрузкой

        Sheet filteredDataSheet = summarySheet.getWorkbook().createSheet("Отфильтрованные данные");
        rowNum = 0;
        Row filteredHeaderRow = filteredDataSheet.createRow(rowNum++);
        filteredHeaderRow.createCell(0).setCellValue("Час");
        columnNum = 1;

        for (String dateStr : dateRequestMap.keySet()) {
            filteredHeaderRow.createCell(columnNum++).setCellValue(dateStr);
        }
        filteredHeaderRow.createCell(columnNum).setCellValue("Среднее");

        // Заполнение отфильтрованных данных
        for (int hour = 0; hour < 24; hour++) {
            Row row = filteredDataSheet.createRow(rowNum++);
            row.createCell(0).setCellValue(hour + ":00:00");
            columnNum = 1;
            double totalRequests = 0;
            int dateCount = 0;

            for (String dateStr : dateRequestMap.keySet()) {
                int requestCount = (int) getRequestCountForHour(sourceSheet, maxRequestsDay, LocalDate.parse(dateStr, dateFormatter), hour);
                row.createCell(columnNum).setCellValue(requestCount);
                totalRequests += requestCount;
                if (requestCount > 0) {
                    dateCount++;
                }
                columnNum++;
            }

            double averageRequests = dateCount > 0 ? totalRequests / dateCount : 0;
            row.createCell(columnNum).setCellValue(averageRequests);

            // Удаление значений, которые значительно отличаются от среднего
            for (int i = 1; i < columnNum; i++) {
                double value = row.getCell(i).getNumericCellValue();
                if (Math.abs(value - averageRequests) / averageRequests >= DEVIATION) {
                    row.getCell(i).setBlank();
                }
            }
        }

        // Сбор валидных дат
        List<String> validDates = new ArrayList<>();
        for (String dateStr : dateRequestMap.keySet()) {
            boolean hasEmptyCell = false;
            for (int hour = 0; hour < 24; hour++) {
                Row row = filteredDataSheet.getRow(hour + 1);
                if (row != null) {
                    Cell cell = row.getCell(getColumnIndexForDate(filteredHeaderRow, dateStr));
                    if (cell == null || cell.getCellType() == CellType.BLANK) {
                        hasEmptyCell = true;
                        break;
                    }
                }
            }
            if (!hasEmptyCell) {
                validDates.add(dateStr);
            }
        }

        System.out.println("\nДаты с полными данными: " + validDates);
        return validDates;
    }

    // Метод для расчета количества типов запросов
    private static int getMessageTypeCount(Sheet sheet) {
        Set<String> messageTypeIds = new HashSet<>(); // Используем HashSet для уникальных значений
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Пропускаем заголовок
            String messageTypeId = row.getCell(4).getStringCellValue(); // Предполагаем, что massageTypeId находится в пятом столбце (индекс 4)
            messageTypeIds.add(messageTypeId); // Добавляем в Set, дубликаты будут игнорироваться
        }
        return messageTypeIds.size(); // Возвращаем количество уникальных типов
    }

    // Метод для удаления из статистики дней с нетипичной нагрузкой
    private static void createFilteredRequestsSheet(Sheet filteredRequestsSheet, Sheet sourceSheet, DayOfWeek maxRequestsDay, DateTimeFormatter dateFormatter, List<String> validDates) {
        if (validDates == null || validDates.isEmpty()) {
            System.out.println("Список validDates пуст или не определен!");
            return;
        }

        int rowNum = 0;

        // Заголовки результата
        Row headerRow = filteredRequestsSheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Дата");
        headerRow.createCell(1).setCellValue("Час");
        headerRow.createCell(2).setCellValue("День недели");
        headerRow.createCell(3).setCellValue("rph");
        headerRow.createCell(4).setCellValue("messageTypeId");
        headerRow.createCell(5).setCellValue(""); // Пустая колонка
        headerRow.createCell(6).setCellValue("На этом листе представлена статистика за " + maxRequestsDay + " за исключением дней с нетипичной нагрузкой в отдельные часы");


        // Начинаем с 1, чтобы пропустить заголовок
        int lastRowNum = sourceSheet.getLastRowNum();
        for (String dateStr : validDates) {
            for (int i = 1; i <= lastRowNum; i++) { // Изменено на 1, чтобы пропустить заголовок
                Row row = sourceSheet.getRow(i);
                if (row != null) {
                    String dateString = row.getCell(0).getStringCellValue();
                    try {
                        LocalDate date = LocalDate.parse(dateString, dateFormatter);
                        DayOfWeek dayOfWeek = date.getDayOfWeek();

                        if (dayOfWeek == maxRequestsDay && dateString.equals(dateStr)) {
                            //   System.out.println("Обработка строки для даты: " + dateString);

                            Row newRow = filteredRequestsSheet.createRow(rowNum++);
                            newRow.createCell(0).setCellValue(dateString);
                            newRow.createCell(1).setCellValue((int) row.getCell(1).getNumericCellValue());
                            newRow.createCell(2).setCellValue(dayOfWeek.toString());
                            newRow.createCell(3).setCellValue(getCellNumericValue(row.getCell(3)));
                            newRow.createCell(4).setCellValue(getCellNumericValue(row.getCell(4)));

                            //System.out.println("Строка обработана успешно: " + dateString);
                        }
                    } catch (DateTimeParseException e) {
                        System.err.println("Ошибка при парсинге даты: " + dateString);
                    }
                }
            }
        }


    }

    // Метод для поиска индекс столбца в строке заголовка, используется при фильтрации дней с нетипичной нагрузкой
    private static int getColumnIndexForDate(Row headerRow, String dateStr) {
        for (int i = 1; i < headerRow.getPhysicalNumberOfCells(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null && cell.getCellType() == CellType.STRING) {
                if (cell.getStringCellValue().equals(dateStr)) {
                    return i;
                }
            }
        }
        return -1; // Если дата не найдена
    }

    // Метод для вычисления пикового дня недели
    private static DayOfWeek getMaxRequestsDay(Map<DayOfWeek, Long> totalRequestsMap) {
        Long maxRequests = 0L;
        DayOfWeek maxRequestsDay = null;

        for (Map.Entry<DayOfWeek, Long> entry : totalRequestsMap.entrySet()) {
            if (entry.getValue() > maxRequests) {
                maxRequests = entry.getValue();
                maxRequestsDay = entry.getKey();

            }
        }

        return maxRequestsDay;
    }


    // Создание сводной таблицы со средними значениями интенсивностей операций по часам
    private static double createPivotTable(Sheet pivotSheet, Sheet filteredRequestsSheet, Workbook workbook) {
        // Заголовки сводной таблицы
        Row headerRow = pivotSheet.createRow(0);
        headerRow.createCell(0).setCellValue("Час");

        // Словарь для хранения значений
        Map<Integer, Map<Integer, List<Double>>> pivotData = new HashMap<>();

        // Сбор данных из отфильтрованных запросов
        int lastRowNum = filteredRequestsSheet.getLastRowNum();
        for (int i = 1; i <= lastRowNum; i++) { // Пропускаем заголовок
            Row row = filteredRequestsSheet.getRow(i);
            if (row != null) {
                int hour = (int) row.getCell(1).getNumericCellValue();
                int messageTypeId = (int) row.getCell(4).getNumericCellValue();
                double value = getCellNumericValue(row.getCell(3));

                // Инициализация структуры данных
                pivotData.computeIfAbsent(hour, k -> new HashMap<>())
                        .computeIfAbsent(messageTypeId, k -> new ArrayList<>())
                        .add(value);
            }
        }

        // Заполнение заголовков для messageTypeId
        int columnNum = 1;
        Set<Integer> messageTypeIds = pivotData.values().stream()
                .flatMap(map -> map.keySet().stream())
                .collect(Collectors.toSet());

        for (Integer messageTypeId : messageTypeIds) {
            headerRow.createCell(columnNum++).setCellValue("" + messageTypeId);
        }

        // Заполнение сводной таблицы
        int rowNum = 1;
        double maxSumValue = Double.NEGATIVE_INFINITY;
        double maxAverage = Double.NEGATIVE_INFINITY; // Переменная для хранения максимального среднего
        int hourWithMaxAverage = -1; // Переменная для хранения часа с максимальным средним

        for (Integer hour : pivotData.keySet()) {
            Row row = pivotSheet.createRow(rowNum++);
            row.createCell(0).setCellValue(hour); // Час

            columnNum = 1;
            double rowSum = 0;
            int count = 0; // Счетчик для среднего

            for (Integer messageTypeId : pivotData.get(hour).keySet()) {
                List<Double> values = pivotData.get(hour).get(messageTypeId);
                double average = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                row.createCell(columnNum).setCellValue(average);
                rowSum += average;
                count++;
                columnNum++;

                // Проверка на максимальное среднее
                if (average > maxAverage) {
                    maxAverage = average;
                    hourWithMaxAverage = hour; // Сохраняем час с максимальным средним
                }
            }

            // Добавляем сумму в последний столбец
            Cell sumCell = row.createCell(columnNum);
            sumCell.setCellValue(rowSum);
            maxSumValue = Math.max(maxSumValue, rowSum); // Обновляем максимальную сумму
        }

        // Добавляем заголовок для колонки "Сумма"
        headerRow.createCell(columnNum).setCellValue("Сумма");
        columnNum++;
        headerRow.createCell(columnNum + 1).setCellValue("Для наглядности сформируйте график. Рассмотрим среднее значение интенсивностей операций по часам. По графику видно, что максимальная интенсивность достигается в " + hourWithMaxAverage + " часов.");

        // Выделение ячеек с максимальным значением и значениями в пределах ±10%
        highlightMaxSumRows(pivotSheet, workbook, rowNum, maxSumValue);

        System.out.println("Сводная таблица успешно создана.");
        return maxSumValue; // Возвращаем максимальную сумму
    }

    //Метод для выделения ячеек с максимальным значением в пределах 10% и выделение строки с пик часом
    private static void highlightMaxSumRows(Sheet pivotSheet, Workbook workbook, int rowNum, double maxSumValue) {
        double maxAverageValue = Double.MIN_VALUE; // Инициализируем максимальное среднее значение
        int maxAverageRowNum = -1; // Переменная для хранения номера строки с максимальным средним значением

        // Первый проход для нахождения максимального среднего значения
        for (int i = 1; i < rowNum; i++) {
            Row row = pivotSheet.getRow(i);
            if (row != null) {
                Cell sumCell = row.getCell(row.getPhysicalNumberOfCells() - 1); // Ячейка "Сумма"
                double sumValue = sumCell.getNumericCellValue();
                int count = row.getPhysicalNumberOfCells() - 1; // Количество значений для среднего

                if (count > 0) {
                    double averageValue = sumValue / count; // Вычисляем среднее значение
                    if (averageValue > maxAverageValue) {
                        maxAverageValue = averageValue; // Обновляем максимальное среднее значение
                        maxAverageRowNum = i; // Сохраняем номер строки
                    }
                }

                // Проверка на максимальное значение и ±10%
                if (sumValue >= maxSumValue * 0.9 && sumValue <= maxSumValue * 1.1) {
                    CellStyle style = workbook.createCellStyle();
                    style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
                    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    sumCell.setCellStyle(style); // Выделяем ячейку "Сумма"

                    // Выделяем все ячейки в строке
                    for (int j = 0; j < row.getPhysicalNumberOfCells(); j++) {
                        Cell cell = row.getCell(j);
                        cell.setCellStyle(style);
                    }
                }
            }
        }

        // Второй проход для выделения строки с максимальным средним значением
        if (maxAverageRowNum != -1) {
            CellStyle redStyle = workbook.createCellStyle();
            redStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex()); // Бледно-красный цвет
            redStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row maxAverageRow = pivotSheet.getRow(maxAverageRowNum);
            for (int j = 0; j < maxAverageRow.getPhysicalNumberOfCells(); j++) {
                Cell cell = maxAverageRow.getCell(j);
                cell.setCellStyle(redStyle); // Выделяем всю строку
            }
        }
    }

    //Создание листа с максимальными данными и его заполнение
    private static void createProfileSheet(Sheet pivotSheet, Sheet profileSheet, double maxSumValue) {
        Row profileHeaderRow = profileSheet.createRow(0);
        profileHeaderRow.createCell(0).setCellValue("Час");
        int columnNum = 1;

        // Извлечение заголовков messageTypeId из сводной таблицы
        Row pivotHeaderRow = pivotSheet.getRow(0);
        for (int i = 1; i < pivotHeaderRow.getPhysicalNumberOfCells() - 1; i++) { // Пропускаем "Час" и "Сумма"
            Cell headerCell = pivotHeaderRow.getCell(i);
            profileHeaderRow.createCell(columnNum++).setCellValue(headerCell.getStringCellValue());
        }
      //  profileHeaderRow.createCell(columnNum).setCellValue("Сумма"); // Заголовок для суммы

        int profileRowNum = 1; // Индекс строки для нового листа
        double lowerBound = maxSumValue * 0.9; // Нижняя граница
        double upperBound = maxSumValue * 1.1; // Верхняя граница

        // Заполнение профильного листа
        for (int i = 1; i <= pivotSheet.getLastRowNum(); i++) { // Пропускаем заголовок
            Row row = pivotSheet.getRow(i);
            if (row != null) {
                Cell sumCell = row.getCell(row.getPhysicalNumberOfCells() - 1); // Ячейка "Сумма"
                double sumValue = sumCell.getNumericCellValue();

                // Проверка на 10% отклонение
                if (sumValue >= lowerBound && sumValue <= upperBound) {
                    Row profileRow = profileSheet.createRow(profileRowNum++);
                    profileRow.createCell(0).setCellValue((int) row.getCell(0).getNumericCellValue()); // Копируем час

                    columnNum = 1; // Сброс индекса столбца
                    for (int j = 1; j < row.getPhysicalNumberOfCells() - 1; j++) { // Пропускаем "Сумма"
                        profileRow.createCell(columnNum++).setCellValue(row.getCell(j).getNumericCellValue()); // Копируем значения
                    }
                    profileRow.createCell(columnNum).setCellValue(sumValue); // Копируем сумму
                }
            }
        }

        // Найти максимальные значения для каждого messageTypeId на листе "Максимальные значения"
        double[] maxValues = new double[columnNum - 1]; // Создаем массив с размером, равным количеству столбцов без "Сумма"
        String[] messageTypeNames = new String[columnNum - 1]; // Массив для хранения названий Message Type ID
        Arrays.fill(maxValues, Double.NEGATIVE_INFINITY); // Инициализация максимальных значений

        for (int j = 1; j < columnNum; j++) { // Сохраняем названия из заголовков
            messageTypeNames[j - 1] = pivotHeaderRow.getCell(j).getStringCellValue();
        }

        for (int i = 1; i < profileRowNum; i++) { // Проходим по всем строкам на листе "Максимальные значения"
            Row row = profileSheet.getRow(i);
            if (row != null) {
                for (int j = 1; j < row.getPhysicalNumberOfCells() - 1; j++) { // Пропускаем "Час" и "Сумма"
                    double value = row.getCell(j).getNumericCellValue();
                    if (value > maxValues[j - 1]) {
                        maxValues[j - 1] = value;
                    }
                }
            }
        }

        // Добавление строки "Макс" в конец листа "Максимальные значения"
        Row maxRow = profileSheet.createRow(profileRowNum);
        maxRow.createCell(0).setCellValue("Макс");
        for (int j = 0; j < maxValues.length; j++) {
            maxRow.createCell(j + 1).setCellValue(maxValues[j]); // Заполнение максимальными значениями
        }
        // Добавляем комментарий к таблице
        profileRowNum++;
        Row text1Row = profileSheet.createRow(profileRowNum + 1);
        text1Row.createCell(0).setCellValue("На этом листе представлены данные для рассчета профиля. Диапазон времени, который будет покрыт профилем – 10% от пик-часа. ");

        // Проверка, были ли добавлены строки
        if (profileRowNum == 1) { // Проверяем, была ли добавлена только строка "Макс"
            System.out.println("Нет строк, соответствующих критериям для заполнения листа 'Максимальные значения'.");
        } else {
            System.out.println("Лист 'Максимальные значения' успешно заполнен.");
        }

        // Создание нового листа для Профиля
        createMaxValuesSheet(profileSheet, messageTypeNames, maxValues);
    }

    // Метод для создание листа с Профилем и заполнение его данными
    private static void createMaxValuesSheet(Sheet profileSheet, String[] messageTypeNames, double[] maxValues) {
        // Создаем новый лист для Профиля
        Workbook workbook = profileSheet.getWorkbook();
        Sheet maxValuesSheet = workbook.createSheet("Профиль");

        // Заголовки
        Row headerRow = maxValuesSheet.createRow(0);
        headerRow.createCell(0).setCellValue("MassageTypeId");
        headerRow.createCell(1).setCellValue("rph"); // Изменено название колонки на rph
        headerRow.createCell(2).setCellValue("rps"); // Добавлена колонка rps
        headerRow.createCell(3).setCellValue("Нарастающий итог (%)"); // Добавлен новый столбец

        // Список для хранения значений и их названий
        List<Map.Entry<String, Double>> maxValueEntries = new ArrayList<>();

        // Заполнение списка значениями и их названиями
        for (int j = 0; j < maxValues.length; j++) { // Проходим по всем максимальным значениям
            if (maxValues[j] != Double.NEGATIVE_INFINITY) {
                maxValueEntries.add(new AbstractMap.SimpleEntry<>(messageTypeNames[j], maxValues[j])); // Сохраняем название и значение
            }
        }

        // Сортировка по убыванию
        maxValueEntries.sort((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue()));

        // Заполнение нового листа
        int rowNum = 1;
        double totalRph = 0; // Переменная для хранения суммы rph
        double cumulativeTotal = 0; // Переменная для хранения нарастающего итога
        double totalSum = 0; // Общая сумма rph для расчета процентов

        // Сначала вычисляем общую сумму rph
        for (double value : maxValues) {
            totalSum += value;
        }

        for (Map.Entry<String, Double> entry : maxValueEntries) {
            Row row = maxValuesSheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey()); // MassageTypeId
            row.createCell(1).setCellValue(entry.getValue()); // rph
            double rps = entry.getValue() / 3600; // rps = rph / 3600
            row.createCell(2).setCellValue(rps); // rps
            cumulativeTotal += entry.getValue(); // Обновляем нарастающий итог
            double cumulativePercentage = (cumulativeTotal / totalSum) * 100; // Нарастающий итог в процентах
            row.createCell(3).setCellValue(cumulativePercentage); // Нарастающий итог в процентах
            totalRph += entry.getValue(); // Суммируем rph
        }

        // Добавление строки "Сумма"
        Row sumRow = maxValuesSheet.createRow(rowNum);
        sumRow.createCell(0).setCellValue("Сумма");
        sumRow.createCell(1).setCellValue(totalRph); // Сумма rph
        sumRow.createCell(2).setCellValue(totalRph / 3600); // Сумма rps
        sumRow.createCell(3).setCellValue(100); // Нарастающий итог для суммы (100%)

        System.out.println("Лист 'Профиль' успешно заполнен.");
        // Создание листа "Профиль Итоговый"
        createFinalProfileSheet(workbook, maxValuesSheet);
    }

    // Метод для формирования итогового профиля с попаданием 99% и с добавлением критичных запросов
    private static void createFinalProfileSheet(Workbook workbook, Sheet maxValuesSheet) {
        Sheet finalProfileSheet = workbook.createSheet("Профиль Итоговый");
        Row headerRow = finalProfileSheet.createRow(0);
        headerRow.createCell(0).setCellValue("MassageTypeId");
        headerRow.createCell(1).setCellValue("rph");
        headerRow.createCell(2).setCellValue("rps");
        headerRow.createCell(3).setCellValue("Процент от суммы, %");

        // Перенос данных из листа "Профиль", исключая строки с нарастающим итогом > 99%
        int finalRowNum = 1;
        Set<String> addedMessageTypes = new HashSet<>(); // Множество для отслеживания добавленных идентификаторов
        double totalRph = 0; // Переменная для хранения суммы rph
        double totalRps = 0; // Переменная для хранения суммы rps

        for (int i = 1; i <= maxValuesSheet.getLastRowNum(); i++) { // Пропускаем заголовок
            Row row = maxValuesSheet.getRow(i);
            if (row != null) {
                double cumulativePercentage = row.getCell(3).getNumericCellValue(); // Получаем нарастающий итог
                if (cumulativePercentage <= CUMULATIVE_PERCENTAGE) {
                    String messageTypeId = row.getCell(0).getStringCellValue();
                    if (!addedMessageTypes.contains(messageTypeId)) {
                        Row newRow = finalProfileSheet.createRow(finalRowNum++);
                        newRow.createCell(0).setCellValue(messageTypeId); // MassageTypeId
                        double rph = row.getCell(1).getNumericCellValue(); // rph
                        double rps = row.getCell(2).getNumericCellValue(); // rps
                        newRow.createCell(1).setCellValue(rph); // rph
                        newRow.createCell(2).setCellValue(rps); // rps
                        addedMessageTypes.add(messageTypeId); // Добавляем идентификатор в множество
                    }
                }
            }
        }

        // Добавление критичных запросов
        int[] specificMessageTypeIds = SPECIFIC_MESSAGE_TYPE_IDS; // Массив идентификаторов
        for (int messageTypeId : specificMessageTypeIds) {
            finalRowNum = addSpecificMessageType(finalProfileSheet, maxValuesSheet, messageTypeId, finalRowNum, addedMessageTypes);
        }

        //System.out.println("finalRowNum = " + finalRowNum);

        // Обновление сумм после добавления критичных запросов
        for (int j = 1; j < finalRowNum; j++) {
            Row row = finalProfileSheet.getRow(j);
            if (row != null) {
                totalRph += row.getCell(1).getNumericCellValue(); // Суммируем rph
                totalRps += row.getCell(2).getNumericCellValue(); // Суммируем rps
            }
        }

        // Добавление строки с суммой
        Row totalRow = finalProfileSheet.createRow(finalRowNum);
        totalRow.createCell(0).setCellValue("Сумма");
        totalRow.createCell(1).setCellValue(totalRph); // Записываем сумму rph
        totalRow.createCell(2).setCellValue(totalRps); // Записываем сумму rps

        // Обновление процентов
        for (int j = 1; j < finalRowNum; j++) {
            Row row = finalProfileSheet.getRow(j);
            if (row != null) {
                double rphValue = row.getCell(1).getNumericCellValue();
                double rphPercentage = (totalRph > 0) ? (rphValue / totalRph) * 100 : 0;
                row.createCell(3).setCellValue(rphPercentage); // Процент от суммы rph
            }
        }

        System.out.println("Лист 'Профиль Итоговый' успешно заполнен.");
    }

    // Метод для добавление в профиль низкоинтенсивных критическиважных запросов
    private static int addSpecificMessageType(Sheet finalProfileSheet, Sheet maxValuesSheet, int messageTypeId, int startRowNum, Set<String> addedMessageTypes) {
        for (int i = 1; i <= maxValuesSheet.getLastRowNum(); i++) { // Start from the second row (skipping header)
            Row row = maxValuesSheet.getRow(i);
            if (row != null) {
                String currentMessageTypeId = row.getCell(0).getStringCellValue(); // Get Message Type ID
                String numericId = currentMessageTypeId.replaceAll("[^0-9]", "");
                if (!numericId.isEmpty() && Integer.parseInt(numericId) == messageTypeId && !addedMessageTypes.contains(currentMessageTypeId)) {
                    Row newRow = finalProfileSheet.createRow(startRowNum++); // Create a new row at the current startRowNum
                    newRow.createCell(0).setCellValue(currentMessageTypeId); // MessageTypeId
                    newRow.createCell(1).setCellValue(row.getCell(1).getNumericCellValue());
                    newRow.createCell(2).setCellValue(row.getCell(2).getNumericCellValue());
                    addedMessageTypes.add(currentMessageTypeId); // Add identifier to set
                    System.out.println("В профиль обавлено критически важное сообщение:\n " + currentMessageTypeId);
                }
            }
        }
        return startRowNum; // Возвращаем обновленный номер строки
    }

    // Метод для получения числового значения из ячейки
    private static int getCellNumericValue(Cell cell) {
        if (cell == null) {
            return 0; // Или любое другое значение по умолчанию
        }
        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                try {
                    return Integer.parseInt(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return 0; // Или любое другое значение по умолчанию
                }
            default:
                return 0; // Или любое другое значение по умолчанию
        }
    }

    private static double getRequestCountForHour(Sheet sheet, DayOfWeek maxRequestsDay, LocalDate date, int hour) {
        double requestCount = 0; // Изменяем тип на double для точности
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Пропускаем заголовок
            String dateStr = row.getCell(0).getStringCellValue();
            LocalDate rowDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            DayOfWeek dayOfWeek = rowDate.getDayOfWeek();
            int rowHour = (int) row.getCell(1).getNumericCellValue();
            if (dayOfWeek == maxRequestsDay && rowDate.equals(date) && rowHour == hour) {
                requestCount += getCellNumericValue(row.getCell(3)); // Суммируем значения
            }
        }
        int messageTypeCount = getMessageTypeCount(sheet); // Получаем количество уникальных типов сообщений
        return requestCount / messageTypeCount; // Делим на количество типов запросов после суммирования
    }

    // Метод для подсчета общее количество запросов за определенный час в заданный день недели и дату
    private static double getRequestTotalForDate(Sheet sheet, DayOfWeek maxRequestsDay, LocalDate date) {
        double totalRequests = 0;
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Пропускаем заголовок
            String dateStr = row.getCell(0).getStringCellValue();
            LocalDate rowDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            DayOfWeek dayOfWeek = rowDate.getDayOfWeek();
            if (dayOfWeek == maxRequestsDay && rowDate.equals(date)) {
                totalRequests += getCellNumericValue(row.getCell(3));
            }
        }
        return totalRequests;
    }
}