package com.videoconverter.filter;

import com.videoconverter.model.bean.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

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

        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = uri.substring(contextPath.length());

        // Check if path is public
        if (isPublicResource(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Check authentication
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            res.sendRedirect(contextPath + "/login");
            return;
        }

        // Check admin access
        if (path.startsWith("/admin") && !user.isAdmin()) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isPublicResource(String path) {
        if (path.equals("/") || path.equals("/login.jsp") || path.equals("/register.jsp")) {
            return true;
        }

        for (String publicUrl : PUBLIC_URLS) {
            if (path.startsWith(publicUrl)) {
                return true;
            }
        }

        return false;
    }
}

