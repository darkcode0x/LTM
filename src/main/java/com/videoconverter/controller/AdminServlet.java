package com.videoconverter.controller;

import com.videoconverter.dao.ActivityLogDAO;
import com.videoconverter.dao.UserDAO;
import com.videoconverter.model.ActivityLog;
import com.videoconverter.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * AdminServlet - Handle admin operations (user management, activity logs)
 */
@WebServlet("/admin")
public class AdminServlet extends HttpServlet {

    private UserDAO userDAO;
    private ActivityLogDAO activityLogDAO;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
        activityLogDAO = new ActivityLogDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        User currentUser = (User) session.getAttribute("user");
        
        // Check if user is admin
        if (currentUser == null || !currentUser.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "dashboard";

        switch (action) {
            case "dashboard":
                showDashboard(request, response);
                break;
            case "users":
                listUsers(request, response);
                break;
            case "logs":
                viewLogs(request, response);
                break;
            case "toggleUserStatus":
                toggleUserStatus(request, response);
                break;
            case "updateUserQuota":
                updateUserQuota(request, response);
                break;
            default:
                showDashboard(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    /**
     * Show admin dashboard
     */
    private void showDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Get statistics
        List<User> allUsers = userDAO.getAllUsers();
        int totalUsers = allUsers.size();
        int activeUsers = (int) allUsers.stream().filter(User::isActive).count();
        int adminCount = (int) allUsers.stream().filter(User::isAdmin).count();
        
        // Get recent activity logs
        List<ActivityLog> recentLogs = activityLogDAO.getRecentLogs(24, 10);
        
        request.setAttribute("totalUsers", totalUsers);
        request.setAttribute("activeUsers", activeUsers);
        request.setAttribute("adminCount", adminCount);
        request.setAttribute("recentLogs", recentLogs);
        
        request.getRequestDispatcher("/admin/dashboard.jsp").forward(request, response);
    }

    /**
     * List all users
     */
    private void listUsers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        List<User> users = userDAO.getAllUsers();
        request.setAttribute("users", users);
        
        request.getRequestDispatcher("/admin/users.jsp").forward(request, response);
    }

    /**
     * View activity logs
     */
    private void viewLogs(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String actionFilter = request.getParameter("filter");
        List<ActivityLog> logs;
        
        if (actionFilter != null && !actionFilter.isEmpty()) {
            logs = activityLogDAO.getLogsByAction(actionFilter, 100);
        } else {
            logs = activityLogDAO.getAllLogsWithUserInfo(100);
        }
        
        request.setAttribute("logs", logs);
        request.setAttribute("currentFilter", actionFilter);
        
        request.getRequestDispatcher("/admin/logs.jsp").forward(request, response);
    }

    /**
     * Toggle user active status
     */
    private void toggleUserStatus(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        User adminUser = (User) session.getAttribute("user");
        
        try {
            int userId = Integer.parseInt(request.getParameter("userId"));
            User user = userDAO.findById(userId);
            
            if (user != null) {
                // Don't allow disabling yourself
                if (user.getUserId() == adminUser.getUserId()) {
                    session.setAttribute("error", "Cannot disable your own account!");
                } else {
                    boolean newStatus = !user.isActive();
                    
                    // Use specific method to update status
                    if (userDAO.updateUserStatus(userId, newStatus)) {
                        // Log activity
                        String description = String.format("Admin %s %s user %s (ID: %d)",
                                adminUser.getUsername(),
                                newStatus ? "activated" : "deactivated",
                                user.getUsername(),
                                userId);
                        
                        ActivityLog log = new ActivityLog(
                                adminUser.getUserId(),
                                "ADMIN_ACTION",
                                description,
                                request.getRemoteAddr()
                        );
                        activityLogDAO.createLog(log);
                        
                        session.setAttribute("message", 
                                "User " + user.getUsername() + " has been " + 
                                (newStatus ? "activated" : "deactivated"));
                    } else {
                        session.setAttribute("error", "Failed to update user status");
                    }
                }
            } else {
                session.setAttribute("error", "User not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("error", "Error: " + e.getMessage());
        }
        
        response.sendRedirect(request.getContextPath() + "/admin?action=users");
    }

    /**
     * Update user quota
     */
    private void updateUserQuota(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        User adminUser = (User) session.getAttribute("user");
        
        try {
            int userId = Integer.parseInt(request.getParameter("userId"));
            int newQuota = Integer.parseInt(request.getParameter("quota"));
            
            if (newQuota < 0 || newQuota > 100) {
                session.setAttribute("error", "Invalid quota value (0-100)");
            } else {
                User user = userDAO.findById(userId);
                if (user != null) {
                    // Use specific method to update quota
                    if (userDAO.updateUserQuota(userId, newQuota)) {
                        // Log activity
                        String description = String.format("Admin %s updated quota for user %s (ID: %d) to %d",
                                adminUser.getUsername(),
                                user.getUsername(),
                                userId,
                                newQuota);
                        
                        ActivityLog log = new ActivityLog(
                                adminUser.getUserId(),
                                "ADMIN_ACTION",
                                description,
                                request.getRemoteAddr()
                        );
                        activityLogDAO.createLog(log);
                        
                        session.setAttribute("message", 
                                "Updated daily quota for " + user.getUsername() + " to " + newQuota);
                    } else {
                        session.setAttribute("error", "Failed to update quota");
                    }
                } else {
                    session.setAttribute("error", "User not found");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("error", "Error: " + e.getMessage());
        }
        
        response.sendRedirect(request.getContextPath() + "/admin?action=users");
    }
}
