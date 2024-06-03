package org.example;

public class Email {
    private static final String EMAIL_DOMAIN = "example.com";

    public static String generateRandomEmail(String resultName, String resultLastName) {
        String email = resultName.toLowerCase() + "." + resultLastName.toLowerCase() + "@" + EMAIL_DOMAIN;
        return email;
    }
}
