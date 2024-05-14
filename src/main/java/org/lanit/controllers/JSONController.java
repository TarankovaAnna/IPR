package org.lanit.controllers;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.lanit.modelsJson.RequestJson;
import org.lanit.validate.CheckSnils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Controller // Аннотация, указывающая, что этот класс является контроллером Spring MVC
public class JSONController {

    // создаем поле для хранения экземпляра CheckSnils
    private final CheckSnils checkSnils;

    public JSONController(CheckSnils checkSnils) {
        this.checkSnils = checkSnils;
    }

    @PostMapping(path = "snils", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE) // Аннотация, указывающая, что метод обрабатывает POST-запросы по адресу /snils

    // создаем метод для проверки валидности СНИЛС
    public ResponseEntity<?> checkSnils(@RequestBody String requestBody) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");
        try {
            // создаем экземпляр ObjectMapper для работы с JSON
            ObjectMapper mapper = new ObjectMapper();
            // преобразовываем JSON-строку в объект RequestJson
            RequestJson requestJson = mapper.readValue(requestBody, RequestJson.class);
            // получаем значение СНИЛС из объекта RequestJson
            String snils = requestJson.getSnils();
            // проверяем валидность СНИЛС с помощью метода из CheckSnils
            if (checkSnils.isValidSnils(snils)) {
                // возвращаем успешный ответ
                return ResponseEntity.ok("\"message\": \" success\",\n\"snils\": " + "\"" + snils + "\"");
            } else {
                //
                // возвращаем ответ с ошибкой валидации СНИЛС
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("\"message\": \"Error: uncorrected snils\",\n\"snils\": " + "\"" + snils + "\"");
            }
        } catch (JsonParseException | JsonMappingException e) {
            // возвращаем ответ с ошибкой разбора JSON
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("\"message\": \"Error: uncorrected json\",\n\"request\": \n" + requestBody);
        } catch (IOException e) {
            // обработываем исключения ввода/вывода, возвращаем ответа с внутренней ошибкой сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("\"message\": \"Error: internal server error\"");
        }
    }
}
