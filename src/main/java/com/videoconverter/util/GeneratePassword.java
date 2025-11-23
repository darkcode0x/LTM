package com.videoconverter.util;

// sử dụng để tạo user/admin thủ công -> sau đó tự viết script insert vào database.
// không dùng trong code chính.

public class GeneratePassword {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║        BCrypt Password Hash Generator                    ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();

        // If argument provided, hash it
        if (args.length > 0) {
            for (String password : args) {
                generateHash(password);
            }
        } else {
            // Default passwords to hash
            String[] passwords = {
                "admin123",
                "password123",
                "test123",
                "demo123",
                "alice2025",
                "bob2025"
            };

            System.out.println("Generating hashes for default passwords...");
            System.out.println("(Run with arguments to hash custom passwords)");
            System.out.println();

            for (String password : passwords) {
                generateHash(password);
            }
        }

        System.out.println();
        System.out.println("Note: BCrypt generates different hashes each time,");
        System.out.println("but all hashes will verify correctly against the original password.");
    }

    private static void generateHash(String password) {
        try {
            String hash = PasswordUtil.hashPassword(password);
            System.out.println("────────────────────────────────────────────────────────");
            System.out.println("Plain text: " + password);
            System.out.println("BCrypt hash: " + hash);
            System.out.println();

            boolean verified = PasswordUtil.checkPassword(password, hash);
            if (verified) {
                System.out.println("✓ Verification: SUCCESS");
            } else {
                System.out.println("✗ Verification: FAILED (This should not happen!)");
            }
            System.out.println();
        } catch (Exception e) {
            System.err.println("Error hashing password '" + password + "': " + e.getMessage());
        }
    }
}

