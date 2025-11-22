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
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect("login");
            return;
        }

        try {
            int jobId = Integer.parseInt(request.getParameter("jobId"));
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
            if (!file.exists()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
                return;
            }

            // Set response headers
            response.setContentType("application/octet-stream");
            response.setContentLengthLong(file.length());
            response.setHeader("Content-Disposition",
                "attachment; filename=\"" + file.getName() + "\"");

            // Stream file to client
            try (FileInputStream in = new FileInputStream(file);
                 OutputStream out = response.getOutputStream()) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}

