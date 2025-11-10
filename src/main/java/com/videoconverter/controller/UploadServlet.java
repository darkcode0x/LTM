package com.videoconverter.controller;

import com.videoconverter.dao.ConversionJobDAO;
import com.videoconverter.dao.VideoDAO;
import com.videoconverter.model.ConversionJob;
import com.videoconverter.model.ConversionSettings;
import com.videoconverter.model.User;
import com.videoconverter.model.Video;
import com.videoconverter.service.ConversionService;
import com.videoconverter.util.FFmpegWrapper;

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

/**
 * UploadServlet - Handles video file upload and conversion job creation
 */
@WebServlet(name = "UploadServlet", urlPatterns = {"/upload"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 500,       // 500MB
    maxRequestSize = 1024 * 1024 * 500     // 500MB
)
public class UploadServlet extends HttpServlet {
    
    private VideoDAO videoDAO;
    private ConversionJobDAO jobDAO;
    
    // Allowed video formats
    private static final String[] ALLOWED_FORMATS = {
        "video/mp4", "video/avi", "video/x-msvideo", "video/quicktime",
        "video/x-matroska", "video/webm", "video/x-flv", "video/mpeg"
    };
    
    @Override
    public void init() throws ServletException {
        super.init();
        videoDAO = new VideoDAO();
        jobDAO = new ConversionJobDAO();
        System.out.println("UploadServlet initialized");
    }
    
    /**
     * Display upload form (GET)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check if user is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            session = request.getSession(true);
            session.setAttribute("redirectAfterLogin", request.getContextPath() + "/upload");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        // Forward to upload page
        request.getRequestDispatcher("/upload.jsp").forward(request, response);
    }
    
    /**
     * Process video upload and conversion request (POST)
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
        System.out.println("Upload request from user: " + user.getUsername());
        
        try {
            // Get the uploaded file
            Part filePart = request.getPart("videoFile");
            
            if (filePart == null || filePart.getSize() == 0) {
                request.setAttribute("error", "Please select a video file to upload.");
                request.getRequestDispatcher("/upload.jsp").forward(request, response);
                return;
            }
            
            // Validate file type
            String contentType = filePart.getContentType();
            if (!isValidVideoFormat(contentType)) {
                request.setAttribute("error", 
                    "Invalid file format. Please upload a valid video file (MP4, AVI, MKV, WebM, etc.).");
                request.getRequestDispatcher("/upload.jsp").forward(request, response);
                return;
            }
            
            // Get original filename
            String originalFilename = getSubmittedFileName(filePart);
            long fileSize = filePart.getSize();
            
            System.out.println("Uploading file: " + originalFilename + " (" + 
                             formatFileSize(fileSize) + ")");
            
            // Check file size (max 500MB)
            if (fileSize > 500 * 1024 * 1024) {
                request.setAttribute("error", 
                    "File size exceeds maximum limit of 500MB.");
                request.getRequestDispatcher("/upload.jsp").forward(request, response);
                return;
            }
            
            // Create uploads directory if not exists
            String uploadsPath = getServletContext().getRealPath("/uploads");
            File uploadsDir = new File(uploadsPath);
            if (!uploadsDir.exists()) {
                uploadsDir.mkdirs();
            }
            
            // Generate unique filename
            String uniqueFilename = generateUniqueFilename(originalFilename);
            String filePath = uploadsPath + File.separator + uniqueFilename;
            
            // Save uploaded file
            File uploadedFile = new File(filePath);
            try (InputStream input = filePart.getInputStream()) {
                Files.copy(input, uploadedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            
            System.out.println("File saved to: " + filePath);
            
            // Extract video metadata using FFmpeg
            FFmpegWrapper.VideoInfo videoInfo = FFmpegWrapper.getVideoInfo(filePath);
            
            // Create Video record in database
            Video video = new Video();
            video.setUserId(user.getUserId());
            video.setOriginalFilename(originalFilename);
            video.setFilePath(filePath);
            video.setFileSize(fileSize);
            
            if (videoInfo != null) {
                video.setDuration(videoInfo.getDuration());
                video.setResolution(videoInfo.getResolution());
                video.setFormat(videoInfo.getFormat());
            }
            
            int videoId = videoDAO.insert(video);
            
            if (videoId <= 0) {
                // Failed to save video record
                uploadedFile.delete(); // Clean up uploaded file
                request.setAttribute("error", 
                    "Failed to save video information. Please try again.");
                request.getRequestDispatcher("/upload.jsp").forward(request, response);
                return;
            }
            
            video.setVideoId(videoId);
            System.out.println("Video record created with ID: " + videoId);
            
            // Get conversion settings from form
            String outputFormat = request.getParameter("outputFormat");
            String outputResolution = request.getParameter("outputResolution");
            String quality = request.getParameter("quality");
            
            // Validate conversion settings
            if (outputFormat == null || outputFormat.trim().isEmpty()) {
                outputFormat = "mp4"; // Default format
            }
            
            if (quality == null || quality.trim().isEmpty()) {
                quality = "medium"; // Default quality
            }
            
            // Create conversion settings
            ConversionSettings settings = new ConversionSettings();
            settings.setOutputFormat(outputFormat);
            settings.setOutputResolution(outputResolution);
            settings.setQuality(quality);
            settings.applyQualityPreset(); // Set bitrate based on quality
            
            // Optional: Trimming settings
            String startTimeStr = request.getParameter("startTime");
            String endTimeStr = request.getParameter("endTime");
            
            if (startTimeStr != null && !startTimeStr.trim().isEmpty()) {
                try {
                    int startTime = Integer.parseInt(startTimeStr);
                    settings.setStartTime(startTime);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid start time: " + startTimeStr);
                }
            }
            
            if (endTimeStr != null && !endTimeStr.trim().isEmpty()) {
                try {
                    int endTime = Integer.parseInt(endTimeStr);
                    settings.setEndTime(endTime);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid end time: " + endTimeStr);
                }
            }
            
            // Create conversion job
            ConversionJob job = new ConversionJob(videoId, user.getUserId(), settings);
            
            // Calculate estimated time (rough estimate: 1 second per second of video)
            if (videoInfo != null && videoInfo.getDuration() > 0) {
                job.setEstimatedTime(videoInfo.getDuration());
            }
            
            // Insert job into database
            int jobId = jobDAO.insert(job);
            
            if (jobId <= 0) {
                request.setAttribute("error", 
                    "Failed to create conversion job. Please try again.");
                request.getRequestDispatcher("/upload.jsp").forward(request, response);
                return;
            }
            
            job.setJobId(jobId);
            System.out.println("Conversion job created with ID: " + jobId);
            
            // Submit job to ConversionService
            ConversionService conversionService = ConversionService.getInstance();
            
            if (!conversionService.isRunning()) {
                conversionService.start();
            }
            
            boolean submitted = conversionService.submitJob(job);
            
            if (submitted) {
                System.out.println("Job submitted to conversion service: Job ID " + jobId);
                
                // Set success message
                session.setAttribute("successMessage", 
                    "Video uploaded successfully! Your conversion job has been queued.");
                
                // Redirect to status page
                response.sendRedirect(request.getContextPath() + "/status");
            } else {
                request.setAttribute("error", 
                    "Failed to submit conversion job to processing queue. Please try again later.");
                request.getRequestDispatcher("/upload.jsp").forward(request, response);
            }
            
        } catch (Exception e) {
            System.err.println("Error during upload: " + e.getMessage());
            e.printStackTrace();
            
            request.setAttribute("error", 
                "An unexpected error occurred during upload: " + e.getMessage());
            request.getRequestDispatcher("/upload.jsp").forward(request, response);
        }
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
     * Generate unique filename with timestamp
     */
    private String generateUniqueFilename(String originalFilename) {
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
            originalFilename = originalFilename.substring(0, dotIndex);
        }
        
        // Remove special characters and spaces
        originalFilename = originalFilename.replaceAll("[^a-zA-Z0-9_-]", "_");
        
        long timestamp = System.currentTimeMillis();
        return originalFilename + "_" + timestamp + extension;
    }
    
    /**
     * Validate video format
     */
    private boolean isValidVideoFormat(String contentType) {
        if (contentType == null) {
            return false;
        }
        
        for (String format : ALLOWED_FORMATS) {
            if (contentType.toLowerCase().contains(format.toLowerCase())) {
                return true;
            }
        }
        
        return false;
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
        return "UploadServlet - Handles video file upload and conversion job creation";
    }
}
