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

@WebServlet("/upload")
@MultipartConfig(
    maxFileSize = 3221225472L,
    maxRequestSize = 3221225472L,
    fileSizeThreshold = 268435456
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
        // Check authentication
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("login");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect("login");
            return;
        }

        try {
            Part filePart = request.getPart("videoFile");
            String outputFormat = request.getParameter("outputFormat");

            if (filePart == null || filePart.getSize() == 0) {
                showError(request, response, "Please select a video file");
                return;
            }

            if (!isValidFormat(outputFormat)) {
                showError(request, response, "Invalid output format");
                return;
            }

            String filename = getFileName(filePart);
            if (filename == null || filename.isEmpty()) {
                showError(request, response, "Invalid file");
                return;
            }

            String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIR;
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                if (!created) {
                    showError(request, response, "Failed to create upload directory");
                    return;
                }
            }

            String uniqueFilename = System.currentTimeMillis() + "_" + filename;
            String filePath = uploadPath + File.separator + uniqueFilename;
            filePart.write(filePath);

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
                File uploadedFile = new File(filePath);
                if (uploadedFile.exists()) {
                    uploadedFile.delete();
                }
                showError(request, response, "Failed to create conversion job. Queue may be full.");
            }

        } catch (Exception e) {
            showError(request, response, "Upload failed: " + e.getMessage());
        }
    }

    private void showError(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("error", message);
        request.getRequestDispatcher("upload.jsp").forward(request, response);
    }

    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        if (contentDisp == null) return null;

        for (String token : contentDisp.split(";")) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2, token.length() - 1);
            }
        }
        return null;
    }

    private boolean isValidFormat(String format) {
        if (format == null) return false;

        for (String allowed : ALLOWED_FORMATS) {
            if (allowed.equalsIgnoreCase(format)) {
                return true;
            }
        }
        return false;
    }
}

