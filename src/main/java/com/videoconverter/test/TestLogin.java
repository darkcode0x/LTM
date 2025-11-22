package com.videoconverter.test;

import com.videoconverter.model.bean.User;
import com.videoconverter.model.dao.UserDAO;
import com.videoconverter.util.PasswordUtil;

/**
 * Test Login Functionality
 * Kiểm tra xem có thể đăng nhập được với tài khoản admin và testuser không
 */
public class TestLogin {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║          TEST LOGIN FUNCTIONALITY                        ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();

        UserDAO userDAO = new UserDAO();

        // Test 1: Login với admin
        System.out.println("Test 1: Đăng nhập với ADMIN");
        System.out.println("─────────────────────────────────────");
        testLogin(userDAO, "admin", "admin123", "ADMIN");

        System.out.println();

        // Test 2: Login với testuser
        System.out.println("Test 2: Đăng nhập với TEST USER");
        System.out.println("─────────────────────────────────────");
        testLogin(userDAO, "testuser", "user123", "USER");

        System.out.println();

        // Test 3: Login với password sai
        System.out.println("Test 3: Đăng nhập với PASSWORD SAI (phải fail)");
        System.out.println("─────────────────────────────────────");
        testLogin(userDAO, "admin", "wrongpassword", null);

        System.out.println();

        // Test 4: Login với username không tồn tại
        System.out.println("Test 4: Đăng nhập với USERNAME KHÔNG TỒN TẠI (phải fail)");
        System.out.println("─────────────────────────────────────");
        testLogin(userDAO, "nonexistent", "password123", null);

        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║          TEST COMPLETED                                  ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
    }

    private static void testLogin(UserDAO userDAO, String username, String password, String expectedRole) {
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);

        try {
            // Step 1: Lấy user từ database
            User user = userDAO.getUserByUsername(username);

            if (user == null) {
                if (expectedRole == null) {
                    System.out.println("✓ PASS: User không tồn tại (như mong đợi)");
                } else {
                    System.out.println("✗ FAIL: User không tìm thấy trong database!");
                }
                return;
            }

            System.out.println("User found: ID=" + user.getUserId() + ", Role=" + user.getRole());
            System.out.println("Password hash in DB: " + user.getPassword().substring(0, 20) + "...");

            // Step 2: Verify password
            boolean isPasswordCorrect = PasswordUtil.checkPassword(password, user.getPassword());

            System.out.println("Password verification: " + (isPasswordCorrect ? "SUCCESS" : "FAILED"));

            // Step 3: Check role
            if (isPasswordCorrect && expectedRole != null) {
                if (user.getRole().equals(expectedRole)) {
                    System.out.println("✓ PASS: Login thành công! Role đúng: " + expectedRole);
                } else {
                    System.out.println("✗ FAIL: Role không đúng. Expected: " + expectedRole + ", Got: " + user.getRole());
                }
            } else if (isPasswordCorrect && expectedRole == null) {
                System.out.println("✗ FAIL: Password đúng nhưng không nên đúng!");
            } else if (!isPasswordCorrect && expectedRole == null) {
                System.out.println("✓ PASS: Password sai (như mong đợi)");
            } else {
                System.out.println("✗ FAIL: Password không đúng!");
            }

        } catch (Exception e) {
            System.out.println("✗ ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

