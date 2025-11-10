package com.videoconverter.controller;

import com.videoconverter.dao.ConversionJobDAO;
import com.videoconverter.model.ConversionJob;
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
 * StatusServlet - Displays user's conversion job status
 */
@WebServlet(name = "StatusServlet", urlPatterns = {"/status"})
public class StatusServlet extends HttpServlet {
    
    private ConversionJobDAO jobDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        jobDAO = new ConversionJobDAO();
        System.out.println("StatusServlet initialized");
    }
    
    /**
     * Display job status page (GET)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check if user is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            session = request.getSession(true);
            session.setAttribute("redirectAfterLogin", request.getContextPath() + "/status");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        
        try {
            // Get all conversion jobs for this user
            List<ConversionJob> jobs = jobDAO.findByUserId(user.getUserId());
            
            System.out.println("Retrieved " + jobs.size() + " jobs for user: " + user.getUsername());
            
            // Get filter parameter (optional)
            String statusFilter = request.getParameter("status");
            
            if (statusFilter != null && !statusFilter.trim().isEmpty()) {
                // Filter jobs by status
                jobs.removeIf(job -> !job.getStatusString().equalsIgnoreCase(statusFilter));
                System.out.println("Filtered to " + jobs.size() + " jobs with status: " + statusFilter);
            }
            
            // Count jobs by status
            long pendingCount = jobs.stream()
                .filter(job -> job.getStatus() == ConversionJob.JobStatus.PENDING)
                .count();
            
            long processingCount = jobs.stream()
                .filter(job -> job.getStatus() == ConversionJob.JobStatus.PROCESSING)
                .count();
            
            long completedCount = jobs.stream()
                .filter(job -> job.getStatus() == ConversionJob.JobStatus.COMPLETED)
                .count();
            
            long failedCount = jobs.stream()
                .filter(job -> job.getStatus() == ConversionJob.JobStatus.FAILED)
                .count();
            
            // Set attributes for JSP
            request.setAttribute("jobs", jobs);
            request.setAttribute("totalJobs", jobs.size());
            request.setAttribute("pendingCount", pendingCount);
            request.setAttribute("processingCount", processingCount);
            request.setAttribute("completedCount", completedCount);
            request.setAttribute("failedCount", failedCount);
            request.setAttribute("statusFilter", statusFilter);
            
            // Forward to status page
            request.getRequestDispatcher("/status.jsp").forward(request, response);
            
        } catch (Exception e) {
            System.err.println("Error retrieving job status: " + e.getMessage());
            e.printStackTrace();
            
            request.setAttribute("error", 
                "An error occurred while retrieving job status. Please try again later.");
            request.getRequestDispatcher("/status.jsp").forward(request, response);
        }
    }
    
    /**
     * Handle POST requests (same as GET)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
    
    @Override
    public String getServletInfo() {
        return "StatusServlet - Displays user's conversion job status";
    }
}
