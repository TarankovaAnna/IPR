package org.example;

public class Transliteration {
    //метод для транслитерации кириллицы в латиницу для использования в email
    public static String transliterate(String cyrillic) {
        String[] rus = {"а", "б", "в", "г", "д", "е", "ё", "ж", "з", "и", "й", "к", "л", "м", "н", "о", "п", "р", "с", "т", "у", "ф", "х", "ц", "ч", "ш", "щ", "ъ", "ы", "ь", "э", "ю", "я"};
        String[] eng = {"a", "b", "v", "g", "d", "e", "yo", "zh", "z", "i", "y", "k", "l", "m", "n", "o", "p", "r", "s", "t", "u", "f", "kh", "ts", "ch", "sh", "shch", "", "y", "", "e", "yu", "ya"};

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < cyrillic.length(); i++) {
            char c = cyrillic.charAt(i);
            int index = -1;
            for (int j = 0; j < rus.length; j++) {
                if (rus[j].charAt(0) == c) {
                    index = j;
                    break;
                }
            }
            if (index != -1) {
                result.append(eng[index]);
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
