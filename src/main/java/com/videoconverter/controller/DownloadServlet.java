package com.videoconverter.controller;

import com.videoconverter.model.bean.ConversionJob;
import com.videoconverter.model.bean.User;
import com.videoconverter.model.dao.ConversionJobDAO;
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

@WebServlet("/download")
public class DownloadServlet extends HttpServlet {
    private ConversionJobDAO jobDAO;

    @Override
    public void init() {
        jobDAO = new ConversionJobDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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
            String jobIdParam = request.getParameter("jobId");
            if (jobIdParam == null || jobIdParam.trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing jobId");
                return;
            }

            int jobId = Integer.parseInt(jobIdParam);
            ConversionJob job = jobDAO.getJobById(jobId);

            if (job == null || job.getUserId() != user.getUserId()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            if (!"COMPLETED".equals(job.getStatus()) || job.getOutputPath() == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not ready");
                return;
            }

            File file = new File(job.getOutputPath());

            // If file doesn't exist at absolute path, try relative to webapp
            if (!file.exists()) {
                String storedPath = job.getOutputPath().replace("\\", "/");
                int uploadsIndex = storedPath.indexOf("uploads");

                if (uploadsIndex != -1) {
                    String relativePath = storedPath.substring(uploadsIndex).replace("/", File.separator);
                    String webappPath = getServletContext().getRealPath("");
                    File relativeFile = new File(webappPath, relativePath);

                    if (relativeFile.exists()) {
                        file = relativeFile;
                    }
                }
            }

            // Security: Prevent path traversal
            String uploadPath = getServletContext().getRealPath("") + File.separator + "uploads";
            String canonicalPath = file.getCanonicalPath();
            String normalizedUploadPath = new File(uploadPath).getCanonicalPath();

            if (!canonicalPath.startsWith(normalizedUploadPath)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid path");
                return;
            }

            if (!file.exists()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
                return;
            }

            // Sanitize filename
            String safeFilename = file.getName().replaceAll("[^a-zA-Z0-9._-]", "_");

            // Set response headers
            response.setContentType("application/octet-stream");
            response.setContentLengthLong(file.length());
            response.setHeader("Content-Disposition",
                "attachment; filename=\"" + safeFilename + "\"");

            // Stream file to client
            try (FileInputStream in = new FileInputStream(file);
                 OutputStream out = response.getOutputStream()) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
            }

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid jobId");
        } catch (Exception e) {
            getServletContext().log("Download error", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}

