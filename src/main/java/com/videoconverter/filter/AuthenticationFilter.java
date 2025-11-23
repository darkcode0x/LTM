package com.videoconverter.filter;

import com.videoconverter.model.bean.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Filter kiểm tra authentication và authorization
 * - Cho phép access public resources (login, register, static files)
 * - Yêu cầu login cho các trang khác
 * - Kiểm tra quyền admin cho /admin/*
 */
@WebFilter("/*")
public class AuthenticationFilter implements Filter {

    private static final String[] PUBLIC_URLS = {
        "/login", "/register", "/css/", "/js/", "/images/"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Lấy path từ URI
        String path = req.getRequestURI().substring(req.getContextPath().length());

        // Cho phép public resources
        if (isPublicResource(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Kiểm tra đã login chưa
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Kiểm tra quyền admin
        if (path.startsWith("/admin") && !user.isAdmin()) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Cho phép tiếp tục
        chain.doFilter(request, response);
    }

    /**
     * Kiểm tra có phải public resource không
     */
    private boolean isPublicResource(String path) {
        // Root page, login page, register page
        if (path.equals("/") || path.equals("/login.jsp") || path.equals("/register.jsp")) {
            return true;
        }

        // Static resources và public endpoints
        for (String publicUrl : PUBLIC_URLS) {
            if (path.startsWith(publicUrl)) {
                return true;
            }
        }

        return false;
    }
}

