package com.videoconverter.controller;

import com.videoconverter.model.bean.ConversionJob;
import com.videoconverter.model.bean.User;
import com.videoconverter.model.bo.ConversionBO;
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
import java.nio.file.Files;

@WebServlet("/upload")
@MultipartConfig(
    maxFileSize = 524288000,      // 500 MB
    maxRequestSize = 524288000
)
public class UploadServlet extends HttpServlet {
    private ConversionBO conversionBO;
    private static final String UPLOAD_DIR = "uploads";
    private static final String[] ALLOWED_FORMATS = {"mp4", "avi", "mkv", "mov", "webm"};

    @Override
    public void init() {
        conversionBO = ConversionBO.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("upload.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect("login");
            return;
        }

        try {
            Part filePart = request.getPart("videoFile");
            String outputFormat = request.getParameter("outputFormat");

            if (filePart == null || filePart.getSize() == 0) {
                request.setAttribute("error", "Please select a video file");
                request.getRequestDispatcher("upload.jsp").forward(request, response);
                return;
            }

            // Validate output format
            if (outputFormat == null || !isValidFormat(outputFormat)) {
                request.setAttribute("error", "Invalid output format");
                request.getRequestDispatcher("upload.jsp").forward(request, response);
                return;
            }

            // Get filename
            String filename = getFileName(filePart);
            if (filename == null || filename.isEmpty()) {
                request.setAttribute("error", "Invalid file");
                request.getRequestDispatcher("upload.jsp").forward(request, response);
                return;
            }

            // Create upload directory
            String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIR;
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Save file
            String uniqueFilename = System.currentTimeMillis() + "_" + filename;
            String filePath = uploadPath + File.separator + uniqueFilename;
            filePart.write(filePath);

            // Submit conversion job
            ConversionJob job = conversionBO.submitJob(
                user.getUserId(),
                filename,
                filePath,
                filePart.getSize(),
                outputFormat
            );

            if (job != null) {
                response.sendRedirect("status?success=true");
            } else {
                // Delete uploaded file on failure
                new File(filePath).delete();
                request.setAttribute("error", "Failed to create conversion job");
                request.getRequestDispatcher("upload.jsp").forward(request, response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Upload failed: " + e.getMessage());
            request.getRequestDispatcher("upload.jsp").forward(request, response);
        }
    }

    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] tokens = contentDisp.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2, token.length() - 1);
            }
        }
        return null;
    }

    private boolean isValidFormat(String format) {
        for (String allowed : ALLOWED_FORMATS) {
            if (allowed.equalsIgnoreCase(format)) {
                return true;
            }
        }
        return false;
    }
}

