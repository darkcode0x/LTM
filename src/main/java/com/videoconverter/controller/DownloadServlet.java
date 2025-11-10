package com.videoconverter.controller;

import com.videoconverter.dao.ConversionJobDAO;
import com.videoconverter.model.ConversionJob;
import com.videoconverter.model.ConversionJob.JobStatus;
import com.videoconverter.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;

/**
 * DownloadServlet - Handles downloading of converted video files
 */
@WebServlet(name = "DownloadServlet", urlPatterns = {"/download"})
public class DownloadServlet extends HttpServlet {
    
    private ConversionJobDAO jobDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        jobDAO = new ConversionJobDAO();
        System.out.println("DownloadServlet initialized");
    }
    
    /**
     * Handle download request (GET)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check if user is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, 
                "You must be logged in to download files.");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        
        // Get job ID parameter
        String jobIdStr = request.getParameter("jobId");
        
        if (jobIdStr == null || jobIdStr.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
                "Job ID is required.");
            return;
        }
        
        try {
            int jobId = Integer.parseInt(jobIdStr);
            
            // Find the conversion job
            ConversionJob job = jobDAO.findById(jobId);
            
            if (job == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, 
                    "Conversion job not found.");
                return;
            }
            
            // Security check: Verify that this job belongs to the current user
            if (job.getUserId() != user.getUserId()) {
                System.err.println("Unauthorized download attempt by user " + 
                                 user.getUsername() + " for job " + jobId);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, 
                    "You do not have permission to download this file.");
                return;
            }
            
            // Check if job is completed
            if (job.getStatus() != JobStatus.COMPLETED) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
                    "This conversion job is not completed yet. Current status: " + 
                    job.getStatusString());
                return;
            }
            
            // Check if output file exists
            String outputPath = job.getOutputPath();
            if (outputPath == null || outputPath.trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, 
                    "Output file path not found.");
                return;
            }
            
            File outputFile = new File(outputPath);
            
            if (!outputFile.exists()) {
                System.err.println("Output file not found: " + outputPath);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, 
                    "Output file does not exist on server.");
                return;
            }
            
            if (!outputFile.isFile()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
                    "Invalid file.");
                return;
            }
            
            System.out.println("Download request for job " + jobId + 
                             " by user " + user.getUsername() + 
                             " - File: " + outputFile.getName());
            
            // Set response headers for file download
            String mimeType = getServletContext().getMimeType(outputFile.getName());
            if (mimeType == null) {
                // Default to binary stream
                mimeType = "application/octet-stream";
            }
            
            response.setContentType(mimeType);
            response.setContentLengthLong(outputFile.length());
            
            // Set filename for download (encode for special characters)
            String filename = job.getOutputFilename();
            if (filename == null) {
                filename = outputFile.getName();
            }
            
            // Encode filename for Content-Disposition header
            String encodedFilename = URLEncoder.encode(filename, "UTF-8")
                                               .replace("+", "%20");
            
            response.setHeader("Content-Disposition", 
                "attachment; filename=\"" + filename + "\"; " +
                "filename*=UTF-8''" + encodedFilename);
            
            // Stream file to response
            try (FileInputStream fis = new FileInputStream(outputFile);
                 OutputStream out = response.getOutputStream()) {
                
                byte[] buffer = new byte[8192]; // 8KB buffer
                int bytesRead;
                
                while ((bytesRead = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                
                out.flush();
            }
            
            System.out.println("Download completed for job " + jobId + 
                             " - File: " + filename + 
                             " (" + formatFileSize(outputFile.length()) + ")");
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
                "Invalid job ID format.");
        } catch (IOException e) {
            System.err.println("Error during download: " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "An error occurred while downloading the file.");
        } catch (Exception e) {
            System.err.println("Unexpected error during download: " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "An unexpected error occurred.");
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
        return "DownloadServlet - Handles downloading of converted video files";
    }
}
