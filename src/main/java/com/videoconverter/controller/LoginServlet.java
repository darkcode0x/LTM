package com.videoconverter.controller;

import com.videoconverter.dao.ActivityLogDAO;
import com.videoconverter.dao.UserDAO;
import com.videoconverter.model.ActivityLog;
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
 * LoginServlet - Handles user authentication and login
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {
    
    private UserDAO userDAO;
    private ActivityLogDAO activityLogDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        userDAO = new UserDAO();
        activityLogDAO = new ActivityLogDAO();
        System.out.println("LoginServlet initialized");
    }
    
    /**
     * Display login form (GET)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check if user is already logged in
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            // Already logged in, redirect to upload page
            response.sendRedirect(request.getContextPath() + "/upload");
            return;
        }
        
        // Forward to login page
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }
    
    /**
     * Process login form (POST)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Set character encoding
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        // Get form parameters
        String usernameOrEmail = request.getParameter("username");
        String password = request.getParameter("password");
        String rememberMe = request.getParameter("rememberMe");
        
        System.out.println("Login attempt for: " + usernameOrEmail);
        
        // Validate input
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            request.setAttribute("error", "Username or email is required.");
            request.setAttribute("username", usernameOrEmail);
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }
        
        if (password == null || password.isEmpty()) {
            request.setAttribute("error", "Password is required.");
            request.setAttribute("username", usernameOrEmail);
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }
        
        try {
            // Find user by username or email
            User user = userDAO.findByUsernameOrEmail(usernameOrEmail.trim());
            
            if (user == null) {
                // User not found
                System.out.println("Login failed: User not found - " + usernameOrEmail);
                request.setAttribute("error", "Invalid username/email or password.");
                request.setAttribute("username", usernameOrEmail);
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                return;
            }
            
            // Check if user is active
            if (!user.isActive()) {
                System.out.println("Login failed: Account is inactive - " + usernameOrEmail);
                request.setAttribute("error", 
                    "Your account has been deactivated. Please contact administrator.");
                request.setAttribute("username", usernameOrEmail);
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                return;
            }
            
            // Verify password using PasswordUtil
            boolean passwordMatches = PasswordUtil.checkPassword(password, user.getPassword());
            
            if (!passwordMatches) {
                // Invalid password
                System.out.println("Login failed: Invalid password - " + usernameOrEmail);
                request.setAttribute("error", "Invalid username/email or password.");
                request.setAttribute("username", usernameOrEmail);
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                return;
            }
            
            // Login successful
            System.out.println("Login successful: " + user.getUsername() + " (ID: " + user.getUserId() + ")");
            
            // Update last login timestamp
            userDAO.updateLastLogin(user.getUserId());
            
            // Log login activity
            ActivityLog log = new ActivityLog(
                    user.getUserId(),
                    "LOGIN",
                    "User logged in successfully",
                    request.getRemoteAddr()
            );
            activityLogDAO.createLog(log);
            
            // Create session for user
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("fullName", user.getFullName());
            
            // Set session timeout (30 minutes)
            session.setMaxInactiveInterval(30 * 60);
            
            // Handle "Remember Me" functionality
            if ("on".equals(rememberMe) || "true".equals(rememberMe)) {
                // Extend session timeout to 7 days
                session.setMaxInactiveInterval(7 * 24 * 60 * 60);
                System.out.println("Remember me enabled for user: " + user.getUsername());
            }
            
            // Get the page user was trying to access (if any)
            String redirectUrl = (String) session.getAttribute("redirectAfterLogin");
            if (redirectUrl != null) {
                session.removeAttribute("redirectAfterLogin");
                response.sendRedirect(redirectUrl);
            } else {
                // Redirect based on user role
                if (user.isAdmin()) {
                    response.sendRedirect(request.getContextPath() + "/admin");
                } else {
                    response.sendRedirect(request.getContextPath() + "/upload");
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error during login: " + e.getMessage());
            e.printStackTrace();
            
            request.setAttribute("error", 
                "An unexpected error occurred. Please try again later.");
            request.setAttribute("username", usernameOrEmail);
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
    
    @Override
    public String getServletInfo() {
        return "LoginServlet - Handles user authentication and login";
    }
}
