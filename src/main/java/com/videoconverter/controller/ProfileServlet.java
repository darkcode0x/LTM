package com.videoconverter.controller;

import com.videoconverter.dao.ConversionJobDAO;
import com.videoconverter.dao.UserDAO;
import com.videoconverter.dao.VideoDAO;
import com.videoconverter.model.ConversionJob;
import com.videoconverter.model.ConversionJob.JobStatus;
import com.videoconverter.model.User;
import com.videoconverter.util.PasswordUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * ProfileServlet - Displays and updates user profile information
 */
@WebServlet(name = "ProfileServlet", urlPatterns = {"/profile"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,    // 1MB
    maxFileSize = 1024 * 1024 * 5,      // 5MB
    maxRequestSize = 1024 * 1024 * 5    // 5MB
)
public class ProfileServlet extends HttpServlet {
    
    private UserDAO userDAO;
    private VideoDAO videoDAO;
    private ConversionJobDAO jobDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        userDAO = new UserDAO();
        videoDAO = new VideoDAO();
        jobDAO = new ConversionJobDAO();
        System.out.println("ProfileServlet initialized");
    }
    
    /**
     * Display profile page (GET)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check if user is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            session = request.getSession(true);
            session.setAttribute("redirectAfterLogin", request.getContextPath() + "/profile");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        
        try {
            // Refresh user data from database
            User freshUser = userDAO.findById(user.getUserId());
            if (freshUser != null) {
                user = freshUser;
                session.setAttribute("user", user);
            }
            
            // Get statistics
            int totalVideos = videoDAO.countByUserId(user.getUserId());
            long totalStorage = videoDAO.getTotalStorageByUserId(user.getUserId());
            
            int totalJobs = jobDAO.findByUserId(user.getUserId()).size();
            int completedJobs = jobDAO.countByUserIdAndStatus(user.getUserId(), JobStatus.COMPLETED);
            int pendingJobs = jobDAO.countByUserIdAndStatus(user.getUserId(), JobStatus.PENDING);
            int processingJobs = jobDAO.countByUserIdAndStatus(user.getUserId(), JobStatus.PROCESSING);
            int failedJobs = jobDAO.countByUserIdAndStatus(user.getUserId(), JobStatus.FAILED);
            
            // Get recent jobs (last 5)
            List<ConversionJob> recentJobs = jobDAO.findByUserId(user.getUserId());
            if (recentJobs.size() > 5) {
                recentJobs = recentJobs.subList(0, 5);
            }
            
            // Calculate success rate
            double successRate = totalJobs > 0 ? 
                (completedJobs * 100.0 / totalJobs) : 0.0;
            
            // Set attributes for JSP
            request.setAttribute("user", user);
            request.setAttribute("totalVideos", totalVideos);
            request.setAttribute("totalStorage", totalStorage);
            request.setAttribute("totalStorageFormatted", formatFileSize(totalStorage));
            request.setAttribute("totalJobs", totalJobs);
            request.setAttribute("completedJobs", completedJobs);
            request.setAttribute("pendingJobs", pendingJobs);
            request.setAttribute("processingJobs", processingJobs);
            request.setAttribute("failedJobs", failedJobs);
            request.setAttribute("successRate", String.format("%.1f", successRate));
            request.setAttribute("recentJobs", recentJobs);
            
            System.out.println("Profile loaded for user: " + user.getUsername() + 
                             " (Total jobs: " + totalJobs + ", Completed: " + completedJobs + ")");
            
            // Forward to profile page
            request.getRequestDispatcher("/profile.jsp").forward(request, response);
            
        } catch (Exception e) {
            System.err.println("Error loading profile: " + e.getMessage());
            e.printStackTrace();
            
            request.setAttribute("error", 
                "An error occurred while loading profile. Please try again later.");
            request.getRequestDispatcher("/profile.jsp").forward(request, response);
        }
    }
    
    /**
     * Update profile (POST)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check if user is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        
        try {
            // Determine which form was submitted
            String action = request.getParameter("action");
            
            if ("updateProfile".equals(action)) {
                updateProfileInfo(request, response, user);
            } else if ("changePassword".equals(action)) {
                changePassword(request, response, user);
            } else if ("uploadAvatar".equals(action)) {
                uploadAvatar(request, response, user);
            } else {
                response.sendRedirect(request.getContextPath() + "/profile");
            }
            
        } catch (Exception e) {
            System.err.println("Error updating profile: " + e.getMessage());
            e.printStackTrace();
            
            session.setAttribute("errorMessage", 
                "An error occurred while updating profile: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/profile");
        }
    }
    
    /**
     * Update profile information
     */
    private void updateProfileInfo(HttpServletRequest request, HttpServletResponse response, 
                                   User user) throws IOException {
        HttpSession session = request.getSession();
        
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        
        // Validate input
        if (fullName == null || fullName.trim().isEmpty()) {
            session.setAttribute("errorMessage", "Full name is required.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }
        
        if (email == null || email.trim().isEmpty()) {
            session.setAttribute("errorMessage", "Email is required.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }
        
        // Check if email is already used by another user
        if (!email.equalsIgnoreCase(user.getEmail())) {
            User existingUser = userDAO.findByUsernameOrEmail(email);
            if (existingUser != null && existingUser.getUserId() != user.getUserId()) {
                session.setAttribute("errorMessage", 
                    "Email is already used by another account.");
                response.sendRedirect(request.getContextPath() + "/profile");
                return;
            }
        }
        
        // Update user object
        user.setFullName(fullName.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPhone(phone != null ? phone.trim() : null);
        
        // Update in database
        boolean updated = userDAO.update(user);
        
        if (updated) {
            // Update session
            session.setAttribute("user", user);
            session.setAttribute("fullName", user.getFullName());
            session.setAttribute("successMessage", "Profile updated successfully!");
            System.out.println("Profile updated for user: " + user.getUsername());
        } else {
            session.setAttribute("errorMessage", "Failed to update profile. Please try again.");
        }
        
        response.sendRedirect(request.getContextPath() + "/profile");
    }
    
    /**
     * Change password
     */
    private void changePassword(HttpServletRequest request, HttpServletResponse response, 
                               User user) throws IOException {
        HttpSession session = request.getSession();
        
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");
        
        // Validate input
        if (currentPassword == null || currentPassword.isEmpty()) {
            session.setAttribute("errorMessage", "Current password is required.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }
        
        if (newPassword == null || newPassword.isEmpty()) {
            session.setAttribute("errorMessage", "New password is required.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }
        
        if (newPassword.length() < 6) {
            session.setAttribute("errorMessage", 
                "New password must be at least 6 characters long.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }
        
        if (!PasswordUtil.isPasswordStrong(newPassword)) {
            session.setAttribute("errorMessage", 
                "Password must contain at least one letter and one number.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            session.setAttribute("errorMessage", "New passwords do not match.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }
        
        // Verify current password
        User freshUser = userDAO.findById(user.getUserId());
        if (freshUser == null || !PasswordUtil.checkPassword(currentPassword, freshUser.getPassword())) {
            session.setAttribute("errorMessage", "Current password is incorrect.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }
        
        // Hash new password
        String hashedPassword = PasswordUtil.hashPassword(newPassword);
        
        // Update password in database
        boolean updated = userDAO.updatePassword(user.getUserId(), hashedPassword);
        
        if (updated) {
            session.setAttribute("successMessage", 
                "Password changed successfully! Please login again.");
            System.out.println("Password changed for user: " + user.getUsername());
            
            // Invalidate session and redirect to login
            session.invalidate();
            response.sendRedirect(request.getContextPath() + "/login");
        } else {
            session.setAttribute("errorMessage", 
                "Failed to change password. Please try again.");
            response.sendRedirect(request.getContextPath() + "/profile");
        }
    }
    
    /**
     * Upload avatar
     */
    private void uploadAvatar(HttpServletRequest request, HttpServletResponse response, 
                             User user) throws IOException, ServletException {
        HttpSession session = request.getSession();
        
        Part filePart = request.getPart("avatarFile");
        
        if (filePart == null || filePart.getSize() == 0) {
            session.setAttribute("errorMessage", "Please select an avatar image.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }
        
        // Validate file type
        String contentType = filePart.getContentType();
        if (!contentType.startsWith("image/")) {
            session.setAttribute("errorMessage", "Please upload a valid image file.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }
        
        // Get file extension
        String originalFilename = getSubmittedFileName(filePart);
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        
        // Generate unique filename
        String avatarFilename = "avatar_" + user.getUserId() + "_" + 
                               System.currentTimeMillis() + extension;
        
        // Create avatars directory
        String avatarsPath = getServletContext().getRealPath("/uploads/avatars");
        File avatarsDir = new File(avatarsPath);
        if (!avatarsDir.exists()) {
            avatarsDir.mkdirs();
        }
        
        // Save file
        String avatarPath = avatarsPath + File.separator + avatarFilename;
        File avatarFile = new File(avatarPath);
        
        try (InputStream input = filePart.getInputStream()) {
            Files.copy(input, avatarFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        
        // Update user avatar in database
        String avatarUrl = "uploads/avatars/" + avatarFilename;
        user.setAvatar(avatarUrl);
        
        boolean updated = userDAO.update(user);
        
        if (updated) {
            session.setAttribute("user", user);
            session.setAttribute("successMessage", "Avatar updated successfully!");
            System.out.println("Avatar updated for user: " + user.getUsername());
        } else {
            session.setAttribute("errorMessage", "Failed to update avatar. Please try again.");
        }
        
        response.sendRedirect(request.getContextPath() + "/profile");
    }
    
    /**
     * Get submitted filename from Part
     */
    private String getSubmittedFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        for (String content : contentDisposition.split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf('=') + 1).trim()
                              .replace("\"", "");
            }
        }
        return "unknown";
    }
    
    /**
     * Format file size for display
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    @Override
    public String getServletInfo() {
        return "ProfileServlet - Displays and updates user profile information";
    }
}
