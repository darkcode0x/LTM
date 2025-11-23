package com.videoconverter.controller;

import com.videoconverter.model.bean.User;
import com.videoconverter.model.bo.UserBO;
import com.videoconverter.model.dao.ConversionJobDAO;
import com.videoconverter.model.dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

@WebServlet("/admin")
public class AdminServlet extends HttpServlet {
    private UserBO userBO;
    private ConversionJobDAO jobDAO;

    @Override
    public void init() {
        userBO = new UserBO();
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

        if (user == null || !user.isAdmin()) {
            response.sendRedirect("login");
            return;
        }

        // Get statistics
        int totalUsers = userBO.getTotalUsers();
        Map<Integer, Integer> conversionCounts = jobDAO.getConversionCountByUser();

        request.setAttribute("totalUsers", totalUsers);
        request.setAttribute("conversionCounts", conversionCounts);

        request.getRequestDispatcher("admin/dashboard.jsp").forward(request, response);
    }
}

