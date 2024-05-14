package org.lanit.modelsJson;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.intellij.lang.annotations.Pattern;

public class RequestJson {
    // добавляем аннотацию, указывающую на то, что поле snils должно соответствовать свойству "snils" в JSON
    @JsonProperty("snils")

    // создаем поле для хранения значения СНИЛС
    private String snils;

    // создаем геттеры и сеттеры
    public String getSnils() {
        return snils;
    }


    public void setSnils(String snils) {
        this.snils = snils;
    }
}

