package com.example;

import java.security.SecureRandom;

public class DataGenerator {
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();

    public static String generateData(int size) {
        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
