package com.videoconverter.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * AuthenticationFilter - Intercepts requests to protected resources
 * Redirects to login page if user is not authenticated
 */
@WebFilter(filterName = "AuthenticationFilter", urlPatterns = {"/*"})
public class AuthenticationFilter implements Filter {
    
    // List of protected URL patterns that require authentication
    private static final List<String> PROTECTED_URLS = Arrays.asList(
        "/upload",
        "/status",
        "/profile",
        "/download",
        "/deleteJob"
    );
    
    // List of public URL patterns that don't require authentication
    private static final List<String> PUBLIC_URLS = Arrays.asList(
        "/login",
        "/register",
        "/logout"
    );
    
    // List of public resource patterns (CSS, JS, images, etc.)
    private static final List<String> PUBLIC_RESOURCES = Arrays.asList(
        "/css/",
        "/js/",
        "/images/",
        "/fonts/",
        "/favicon.ico"
    );
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("AuthenticationFilter initialized");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        
        // Remove context path to get the actual URI path
        String path = requestURI.substring(contextPath.length());
        
        // Normalize path (remove trailing slash)
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        
        // Check if request is for a public resource (CSS, JS, images, etc.)
        if (isPublicResource(path)) {
            chain.doFilter(request, response);
            return;
        }
        
        // Check if request is for a public URL (login, register, etc.)
        if (isPublicURL(path)) {
            chain.doFilter(request, response);
            return;
        }
        
        // Check if request is for a protected URL
        if (isProtectedURL(path)) {
            // Check if user is authenticated
            HttpSession session = httpRequest.getSession(false);
            
            if (session == null || session.getAttribute("user") == null) {
                // User is not authenticated
                System.out.println("Unauthorized access attempt to: " + path);
                
                // Save the requested URL to redirect after login
                HttpSession newSession = httpRequest.getSession(true);
                newSession.setAttribute("redirectAfterLogin", requestURI);
                
                // Redirect to login page
                httpResponse.sendRedirect(contextPath + "/login");
                return;
            }
            
            // User is authenticated, allow request to proceed
            chain.doFilter(request, response);
            return;
        }
        
        // For root path or index
        if (path.isEmpty() || path.equals("/") || path.equals("/index.jsp")) {
            // Check if user is logged in
            HttpSession session = httpRequest.getSession(false);
            if (session != null && session.getAttribute("user") != null) {
                // Redirect logged-in users to upload page
                httpResponse.sendRedirect(contextPath + "/upload");
                return;
            } else {
                // Redirect non-logged-in users to login page
                httpResponse.sendRedirect(contextPath + "/login");
                return;
            }
        }
        
        // Allow all other requests to proceed
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {
        System.out.println("AuthenticationFilter destroyed");
    }
    
    /**
     * Check if the path is a protected URL requiring authentication
     * 
     * @param path Request path
     * @return true if protected, false otherwise
     */
    private boolean isProtectedURL(String path) {
        for (String protectedUrl : PROTECTED_URLS) {
            if (path.equals(protectedUrl) || path.startsWith(protectedUrl + "/")) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if the path is a public URL not requiring authentication
     * 
     * @param path Request path
     * @return true if public, false otherwise
     */
    private boolean isPublicURL(String path) {
        for (String publicUrl : PUBLIC_URLS) {
            if (path.equals(publicUrl) || path.startsWith(publicUrl + "/")) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if the path is a public resource (CSS, JS, images, etc.)
     * 
     * @param path Request path
     * @return true if public resource, false otherwise
     */
    private boolean isPublicResource(String path) {
        // Check for specific resource patterns
        for (String resourcePattern : PUBLIC_RESOURCES) {
            if (path.startsWith(resourcePattern)) {
                return true;
            }
        }
        
        // Check for common file extensions
        String lowerPath = path.toLowerCase();
        if (lowerPath.endsWith(".css") || 
            lowerPath.endsWith(".js") || 
            lowerPath.endsWith(".png") || 
            lowerPath.endsWith(".jpg") || 
            lowerPath.endsWith(".jpeg") || 
            lowerPath.endsWith(".gif") || 
            lowerPath.endsWith(".svg") || 
            lowerPath.endsWith(".ico") || 
            lowerPath.endsWith(".woff") || 
            lowerPath.endsWith(".woff2") || 
            lowerPath.endsWith(".ttf") || 
            lowerPath.endsWith(".eot")) {
            return true;
        }
        
        return false;
    }
}
