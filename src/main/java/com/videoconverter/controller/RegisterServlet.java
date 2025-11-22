package com.videoconverter.controller;

import com.videoconverter.model.bo.UserBO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private UserBO userBO;

    @Override
    public void init() {
        userBO = new UserBO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String email = request.getParameter("email");

        // Validation
        if (!userBO.isValidUsername(username)) {
            request.setAttribute("error", "Invalid username format");
            request.getRequestDispatcher("register.jsp").forward(request, response);
            return;
        }

        if (!userBO.isValidEmail(email)) {
            request.setAttribute("error", "Invalid email format");
            request.getRequestDispatcher("register.jsp").forward(request, response);
            return;
        }

        if (!userBO.isValidPassword(password)) {
            request.setAttribute("error", "Password must be at least 6 characters");
            request.getRequestDispatcher("register.jsp").forward(request, response);
            return;
        }

        boolean success = userBO.register(username, password, email);

        if (success) {
            response.sendRedirect("login?registered=true");
        } else {
            request.setAttribute("error", "Username or email already exists");
            request.getRequestDispatcher("register.jsp").forward(request, response);
        }
    }
}

