package com.videoconverter.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * PasswordUtil Utility Class
 * Provides password hashing and verification using BCrypt
 */
public class PasswordUtil {
    
    // BCrypt default work factor (log rounds)
    // Higher value = more secure but slower
    // Recommended: 10-12 for production
    private static final int WORK_FACTOR = 10;
    
    /**
     * Hash a plain text password using BCrypt
     * 
     * @param plainPassword The plain text password to hash
     * @return The hashed password (60 characters)
     * @throws IllegalArgumentException if plainPassword is null or empty
     */
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
    
    /**
     * Verify a plain text password against a hashed password
     * 
     * @param plainPassword The plain text password to verify
     * @param hashedPassword The hashed password to check against
     * @return true if password matches, false otherwise
     * @throws IllegalArgumentException if any parameter is null or empty
     */
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
    
    /**
     * Check if a password hash needs to be rehashed
     * (useful when updating work factor)
     * 
     * @param hashedPassword The hashed password to check
     * @return true if password needs rehashing, false otherwise
     */
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
    
    /**
     * Validate password strength
     * 
     * @param password The password to validate
     * @return true if password meets minimum requirements, false otherwise
     */
    public static boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        // Check for at least one digit
        boolean hasDigit = password.matches(".*\\d.*");
        
        // Check for at least one letter
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        
        return hasDigit && hasLetter;
    }
    
    /**
     * Get password strength description
     * 
     * @param password The password to evaluate
     * @return Description of password strength
     */
    public static String getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return "Empty";
        }
        
        int score = 0;
        
        // Length
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        if (password.length() >= 16) score++;
        
        // Contains lowercase
        if (password.matches(".*[a-z].*")) score++;
        
        // Contains uppercase
        if (password.matches(".*[A-Z].*")) score++;
        
        // Contains digit
        if (password.matches(".*\\d.*")) score++;
        
        // Contains special character
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) score++;
        
        if (score <= 2) return "Weak";
        if (score <= 4) return "Medium";
        if (score <= 6) return "Strong";
        return "Very Strong";
    }
    
    // Main method for testing
    public static void main(String[] args) {
        System.out.println("Testing PasswordUtil...");
        System.out.println("========================================");
        
        // Test 1: Hash and verify correct password
        String plainPassword = "admin123";
        System.out.println("\n1. Testing password: " + plainPassword);
        String hashedPassword = hashPassword(plainPassword);
        System.out.println("   Hashed: " + hashedPassword);
        System.out.println("   Length: " + hashedPassword.length() + " characters");
        
        boolean isCorrect = checkPassword(plainPassword, hashedPassword);
        System.out.println("   Verification with correct password: " + isCorrect);
        
        // Test 2: Verify wrong password
        System.out.println("\n2. Testing with wrong password");
        boolean isWrong = checkPassword("wrongpassword", hashedPassword);
        System.out.println("   Verification with wrong password: " + isWrong);
        
        // Test 3: Password strength
        System.out.println("\n3. Password strength test");
        String[] testPasswords = {"123", "admin", "admin123", "Admin123!", "MyStr0ng!Pass#2024"};
        for (String pwd : testPasswords) {
            System.out.println("   Password: \"" + pwd + "\" - " + 
                             "Strength: " + getPasswordStrength(pwd) + 
                             " - Strong: " + isPasswordStrong(pwd));
        }
        
        // Test 4: Sample hashed passwords from database
        System.out.println("\n4. Testing sample passwords from database");
        String demoHash = "$2a$10$ObFPVKwziMLrJRD7r4nVAOE1osy18RsUfu7NNxW6Srx2vs3cKvI6.";
        
        System.out.println("   Demo password '@Guest': " + checkPassword("@Guest", demoHash));
        
        System.out.println("\n========================================");
        System.out.println("✓ PasswordUtil testing completed!");
    }
}
