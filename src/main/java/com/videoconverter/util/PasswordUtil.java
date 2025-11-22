package com.videoconverter.util;

import org.mindrot.jbcrypt.BCrypt;


public class PasswordUtil {

    private static final int WORK_FACTOR = 10;

    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        try {
            // Generate a salt and hash the password
            String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(WORK_FACTOR));
            System.out.println("Password hashed successfully!");
            return hashedPassword;
        } catch (Exception e) {
            System.err.println("Error hashing password: " + e.getMessage());
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Plain password cannot be null or empty");
        }

        if (hashedPassword == null || hashedPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Hashed password cannot be null or empty");
        }

        try {
            // Check if the password matches the hash
            boolean matches = BCrypt.checkpw(plainPassword, hashedPassword);

            if (matches) {
                System.out.println("Password verification: SUCCESS");
            } else {
                System.out.println("Password verification: FAILED");
            }

            return matches;
        } catch (Exception e) {
            System.err.println("Error verifying password: " + e.getMessage());
            return false;
        }
    }

    public static boolean needsRehash(String hashedPassword) {
        if (hashedPassword == null || hashedPassword.trim().isEmpty()) {
            return true;
        }

        try {
            // Extract the work factor from the hash
            String[] parts = hashedPassword.split("\\$");
            if (parts.length < 4) {
                return true;
            }

            int currentWorkFactor = Integer.parseInt(parts[2]);
            return currentWorkFactor < WORK_FACTOR;
        } catch (Exception e) {
            return true;
        }
    }

    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else {
                hasSpecialChar = true;
            }
        }

        // Require at least 3 out of 4 character types
        int score = 0;
        if (hasUpperCase) score++;
        if (hasLowerCase) score++;
        if (hasDigit) score++;
        if (hasSpecialChar) score++;

        return score >= 3;
    }
}

