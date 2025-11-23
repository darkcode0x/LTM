package com.videoconverter.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    private static final int WORK_FACTOR = 10;
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }

        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        int score = 0;
        if (password.matches(".*[A-Z].*")) score++;  // Has uppercase
        if (password.matches(".*[a-z].*")) score++;  // Has lowercase
        if (password.matches(".*\\d.*")) score++;    // Has digit
        if (password.matches(".*[^A-Za-z0-9].*")) score++;  // Has special char

        return score >= 3;
    }
}
