package com.videoconverter.controller;

import com.videoconverter.dao.ConversionJobDAO;
import com.videoconverter.dao.VideoDAO;
import com.videoconverter.model.ConversionJob;
import com.videoconverter.model.User;
import com.videoconverter.model.Video;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.io.IOException;

/**
 * DeleteJobServlet - Handles deletion of conversion jobs and related files
 */
@WebServlet(name = "DeleteJobServlet", urlPatterns = {"/deleteJob"})
public class DeleteJobServlet extends HttpServlet {
    
    private ConversionJobDAO jobDAO;
    private VideoDAO videoDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        jobDAO = new ConversionJobDAO();
        videoDAO = new VideoDAO();
        System.out.println("DeleteJobServlet initialized");
    }
    
    /**
     * Handle delete request (GET)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        handleDelete(request, response);
    }
    
    /**
     * Handle delete request (POST)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        handleDelete(request, response);
    }
    
    /**
     * Process deletion request
     */
    private void handleDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check if user is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, 
                "You must be logged in to delete jobs.");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        
        // Get job ID parameter
        String jobIdStr = request.getParameter("jobId");
        
        if (jobIdStr == null || jobIdStr.trim().isEmpty()) {
            session.setAttribute("errorMessage", "Job ID is required.");
            response.sendRedirect(request.getContextPath() + "/status");
            return;
        }
        
        try {
            int jobId = Integer.parseInt(jobIdStr);
            
            // Find the conversion job
            ConversionJob job = jobDAO.findById(jobId);
            
            if (job == null) {
                session.setAttribute("errorMessage", "Conversion job not found.");
                response.sendRedirect(request.getContextPath() + "/status");
                return;
            }
            
            // Security check: Verify that this job belongs to the current user
            if (job.getUserId() != user.getUserId()) {
                System.err.println("Unauthorized delete attempt by user " + 
                                 user.getUsername() + " for job " + jobId);
                session.setAttribute("errorMessage", 
                    "You do not have permission to delete this job.");
                response.sendRedirect(request.getContextPath() + "/status");
                return;
            }
            
            System.out.println("Delete request for job " + jobId + 
                             " by user " + user.getUsername());
            
            // Get associated video
            Video video = videoDAO.findById(job.getVideoId());
            
            // Delete output file if exists
            if (job.getOutputPath() != null && !job.getOutputPath().trim().isEmpty()) {
                File outputFile = new File(job.getOutputPath());
                if (outputFile.exists()) {
                    if (outputFile.delete()) {
                        System.out.println("Deleted output file: " + outputFile.getName());
                    } else {
                        System.err.println("Failed to delete output file: " + outputFile.getName());
                    }
                }
            }
            
            // Delete job from database
            boolean jobDeleted = jobDAO.delete(jobId);
            
            if (!jobDeleted) {
                session.setAttribute("errorMessage", 
                    "Failed to delete conversion job from database.");
                response.sendRedirect(request.getContextPath() + "/status");
                return;
            }
            
            System.out.println("Job " + jobId + " deleted from database");
            
            // Check if there are any other jobs for this video
            var relatedJobs = jobDAO.findByUserId(user.getUserId());
            boolean hasOtherJobs = relatedJobs.stream()
                .anyMatch(j -> j.getVideoId() == job.getVideoId() && j.getJobId() != jobId);
            
            // If no other jobs reference this video, delete the video and its file
            if (!hasOtherJobs && video != null) {
                System.out.println("No other jobs found for video " + video.getVideoId() + 
                                 ", deleting video and file...");
                
                // Delete original video file
                if (video.getFilePath() != null && !video.getFilePath().trim().isEmpty()) {
                    File videoFile = new File(video.getFilePath());
                    if (videoFile.exists()) {
                        if (videoFile.delete()) {
                            System.out.println("Deleted video file: " + videoFile.getName());
                        } else {
                            System.err.println("Failed to delete video file: " + videoFile.getName());
                        }
                    }
                }
                
                // Delete video from database
                boolean videoDeleted = videoDAO.delete(video.getVideoId());
                if (videoDeleted) {
                    System.out.println("Video " + video.getVideoId() + " deleted from database");
                }
            }
            
            // Set success message
            session.setAttribute("successMessage", "Conversion job deleted successfully.");
            
            // Redirect back to status page
            response.sendRedirect(request.getContextPath() + "/status");
            
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "Invalid job ID format.");
            response.sendRedirect(request.getContextPath() + "/status");
        } catch (Exception e) {
            System.err.println("Error during job deletion: " + e.getMessage());
            e.printStackTrace();
            
            session.setAttribute("errorMessage", 
                "An unexpected error occurred while deleting the job: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/status");
        }
    }
    
    @Override
    public String getServletInfo() {
        return "DeleteJobServlet - Handles deletion of conversion jobs and related files";
    }
}
