package com.videoconverter.controller;

import com.videoconverter.dao.ActivityLogDAO;
import com.videoconverter.model.ActivityLog;
import com.videoconverter.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * LogoutServlet - Handles user logout and session termination
 */
@WebServlet(name = "LogoutServlet", urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {
    
    private ActivityLogDAO activityLogDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        activityLogDAO = new ActivityLogDAO();
        System.out.println("LogoutServlet initialized");
    }
    
    /**
     * Handle logout for both GET and POST requests
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        handleLogout(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        handleLogout(request, response);
    }
    
    /**
     * Process logout - invalidate session and redirect to login
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Get current session (don't create new one)
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            // Get user info for logging
            User user = (User) session.getAttribute("user");
            String username = (String) session.getAttribute("username");
            
            // Log logout activity before session invalidation
            if (user != null) {
                ActivityLog log = new ActivityLog(
                        user.getUserId(),
                        "LOGOUT",
                        "User logged out",
                        request.getRemoteAddr()
                );
                activityLogDAO.createLog(log);
                System.out.println("User logged out: " + username);
            } else {
                System.out.println("Anonymous user logged out");
            }
            
            // Invalidate session (removes all attributes and destroys session)
            try {
                session.invalidate();
                System.out.println("Session invalidated successfully");
            } catch (IllegalStateException e) {
                // Session was already invalidated
                System.out.println("Session was already invalidated");
            }
        } else {
            System.out.println("No active session to logout");
        }
        
        // Create new session for success message
        HttpSession newSession = request.getSession(true);
        newSession.setAttribute("successMessage", "You have been logged out successfully.");
        
        // Redirect to login page
        response.sendRedirect(request.getContextPath() + "/login");
    }
    
    @Override
    public String getServletInfo() {
        return "LogoutServlet - Handles user logout and session termination";
    }
}
