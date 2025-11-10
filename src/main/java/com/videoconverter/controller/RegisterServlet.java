package com.videoconverter.controller;

import com.videoconverter.dao.UserDAO;
import com.videoconverter.model.User;
import com.videoconverter.util.PasswordUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * RegisterServlet - Handles user registration
 */
@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {
    
    private UserDAO userDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        userDAO = new UserDAO();
        System.out.println("RegisterServlet initialized");
    }
    
    /**
     * Display registration form (GET)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Forward to registration page
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }
    
    /**
     * Process registration form (POST)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Set character encoding
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        // Get form parameters
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");
        
        System.out.println("Registration attempt for username: " + username);
        
        // Validate input
        StringBuilder errors = new StringBuilder();
        
        // Check required fields
        if (username == null || username.trim().isEmpty()) {
            errors.append("Username is required.<br>");
        } else if (username.length() < 3 || username.length() > 50) {
            errors.append("Username must be between 3 and 50 characters.<br>");
        } else if (!username.matches("^[a-zA-Z0-9_]+$")) {
            errors.append("Username can only contain letters, numbers, and underscores.<br>");
        }
        
        if (email == null || email.trim().isEmpty()) {
            errors.append("Email is required.<br>");
        } else if (!isValidEmail(email)) {
            errors.append("Invalid email format.<br>");
        }
        
        if (password == null || password.isEmpty()) {
            errors.append("Password is required.<br>");
        } else if (password.length() < 6) {
            errors.append("Password must be at least 6 characters long.<br>");
        } else if (!PasswordUtil.isPasswordStrong(password)) {
            errors.append("Password must contain at least one letter and one number.<br>");
        }
        
        if (confirmPassword == null || !confirmPassword.equals(password)) {
            errors.append("Passwords do not match.<br>");
        }
        
        if (fullName == null || fullName.trim().isEmpty()) {
            errors.append("Full name is required.<br>");
        }
        
        // Check if username already exists
        if (errors.length() == 0 && userDAO.existsByUsername(username)) {
            errors.append("Username already exists. Please choose another one.<br>");
        }
        
        // Check if email already exists
        if (errors.length() == 0 && userDAO.existsByEmail(email)) {
            errors.append("Email already exists. Please use another email.<br>");
        }
        
        // If there are validation errors, return to registration page
        if (errors.length() > 0) {
            request.setAttribute("error", errors.toString());
            request.setAttribute("username", username);
            request.setAttribute("email", email);
            request.setAttribute("fullName", fullName);
            request.setAttribute("phone", phone);
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }
        
        try {
            // Hash password using PasswordUtil
            String hashedPassword = PasswordUtil.hashPassword(password);
            
            // Create new user object
            User newUser = new User();
            newUser.setUsername(username.trim());
            newUser.setEmail(email.trim().toLowerCase());
            newUser.setPassword(hashedPassword);
            newUser.setFullName(fullName.trim());
            newUser.setPhone(phone != null ? phone.trim() : null);
            newUser.setActive(true);
            newUser.setDailyQuota(5); // Default quota
            newUser.setTotalConversions(0);
            
            // Insert user into database
            int userId = userDAO.insert(newUser);
            
            if (userId > 0) {
                System.out.println("User registered successfully: " + username + " (ID: " + userId + ")");
                
                // Set success message
                HttpSession session = request.getSession();
                session.setAttribute("successMessage", 
                    "Registration successful! Please login with your credentials.");
                
                // Redirect to login page
                response.sendRedirect(request.getContextPath() + "/login");
            } else {
                // Registration failed
                request.setAttribute("error", 
                    "Registration failed due to a server error. Please try again later.");
                request.setAttribute("username", username);
                request.setAttribute("email", email);
                request.setAttribute("fullName", fullName);
                request.setAttribute("phone", phone);
                request.getRequestDispatcher("/register.jsp").forward(request, response);
            }
            
        } catch (Exception e) {
            System.err.println("Error during registration: " + e.getMessage());
            e.printStackTrace();
            
            request.setAttribute("error", 
                "An unexpected error occurred. Please try again later.");
            request.setAttribute("username", username);
            request.setAttribute("email", email);
            request.setAttribute("fullName", fullName);
            request.setAttribute("phone", phone);
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        }
    }
    
    /**
     * Validate email format
     * 
     * @param email Email to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        // Simple email regex pattern
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
    
    @Override
    public String getServletInfo() {
        return "RegisterServlet - Handles user registration";
    }
}
