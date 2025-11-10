<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.videoconverter.model.User" %>
<%@ page import="com.videoconverter.model.ActivityLog" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null || !currentUser.isAdmin()) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    
    List<ActivityLog> logs = (List<ActivityLog>) request.getAttribute("logs");
    String currentFilter = (String) request.getAttribute("currentFilter");
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Activity Logs - Admin Panel</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/css/style.css" rel="stylesheet">
    <style>
        .admin-sidebar {
            min-height: 100vh;
            background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
        }
        .admin-nav-link {
            color: #cbd5e1;
            padding: 0.75rem 1.5rem;
            transition: all 0.3s;
            border-left: 3px solid transparent;
        }
        .admin-nav-link:hover, .admin-nav-link.active {
            background: rgba(59, 130, 246, 0.1);
            color: #3b82f6;
            border-left-color: #3b82f6;
        }
        .log-row {
            border-left: 4px solid #e2e8f0;
            transition: all 0.3s;
        }
        .log-row:hover {
            background: #f8fafc;
        }
    </style>
</head>
<body>
    <div class="container-fluid">
        <div class="row">
            <!-- Sidebar -->
            <div class="col-md-2 admin-sidebar p-0">
                <div class="p-4">
                    <h4 class="text-white mb-4">
                        <i class="bi bi-shield-check"></i> Admin Panel
                    </h4>
                    <div class="text-white-50 small mb-4">
                        Welcome, <strong><%= currentUser.getUsername() %></strong>
                    </div>
                </div>
                <nav class="nav flex-column">
                    <a href="<%= request.getContextPath() %>/admin?action=dashboard" 
                       class="admin-nav-link">
                        <i class="bi bi-speedometer2"></i> Dashboard
                    </a>
                    <a href="<%= request.getContextPath() %>/admin?action=users" 
                       class="admin-nav-link">
                        <i class="bi bi-people"></i> Manage Users
                    </a>
                    <a href="<%= request.getContextPath() %>/admin?action=logs" 
                       class="admin-nav-link active">
                        <i class="bi bi-clock-history"></i> Activity Logs
                    </a>
                    <hr class="bg-secondary mx-3">
                    <a href="<%= request.getContextPath() %>/upload" 
                       class="admin-nav-link">
                        <i class="bi bi-arrow-left-circle"></i> Back to App
                    </a>
                    <a href="<%= request.getContextPath() %>/logout" 
                       class="admin-nav-link">
                        <i class="bi bi-box-arrow-right"></i> Logout
                    </a>
                </nav>
            </div>

            <!-- Main Content -->
            <div class="col-md-10 p-4">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h2 class="mb-0">
                        <i class="bi bi-clock-history text-primary"></i> Activity Logs
                    </h2>
                    <div class="text-muted">
                        Showing last <%= logs != null ? logs.size() : 0 %> logs
                    </div>
                </div>

                <!-- Filter Buttons -->
                <div class="card border-0 shadow-sm mb-4">
                    <div class="card-body">
                        <div class="btn-group" role="group">
                            <a href="<%= request.getContextPath() %>/admin?action=logs" 
                               class="btn <%= currentFilter == null ? "btn-primary" : "btn-outline-primary" %>">
                                <i class="bi bi-list-ul"></i> All
                            </a>
                            <a href="<%= request.getContextPath() %>/admin?action=logs&filter=LOGIN" 
                               class="btn <%= "LOGIN".equals(currentFilter) ? "btn-success" : "btn-outline-success" %>">
                                <i class="bi bi-box-arrow-in-right"></i> Login
                            </a>
                            <a href="<%= request.getContextPath() %>/admin?action=logs&filter=LOGOUT" 
                               class="btn <%= "LOGOUT".equals(currentFilter) ? "btn-secondary" : "btn-outline-secondary" %>">
                                <i class="bi bi-box-arrow-right"></i> Logout
                            </a>
                            <a href="<%= request.getContextPath() %>/admin?action=logs&filter=UPLOAD" 
                               class="btn <%= "UPLOAD".equals(currentFilter) ? "btn-info" : "btn-outline-info" %>">
                                <i class="bi bi-cloud-upload"></i> Upload
                            </a>
                            <a href="<%= request.getContextPath() %>/admin?action=logs&filter=DOWNLOAD" 
                               class="btn <%= "DOWNLOAD".equals(currentFilter) ? "btn-primary" : "btn-outline-primary" %>">
                                <i class="bi bi-cloud-download"></i> Download
                            </a>
                            <a href="<%= request.getContextPath() %>/admin?action=logs&filter=DELETE" 
                               class="btn <%= "DELETE".equals(currentFilter) ? "btn-danger" : "btn-outline-danger" %>">
                                <i class="bi bi-trash"></i> Delete
                            </a>
                            <a href="<%= request.getContextPath() %>/admin?action=logs&filter=ADMIN_ACTION" 
                               class="btn <%= "ADMIN_ACTION".equals(currentFilter) ? "btn-warning" : "btn-outline-warning" %>">
                                <i class="bi bi-shield-check"></i> Admin
                            </a>
                        </div>
                    </div>
                </div>

                <!-- Logs Table -->
                <div class="card border-0 shadow-sm">
                    <div class="card-body p-0">
                        <% if (logs != null && !logs.isEmpty()) { 
                            for (ActivityLog log : logs) {
                                String actionColor = "";
                                String actionIcon = "";
                                String borderColor = "";
                                switch (log.getAction()) {
                                    case "LOGIN":
                                        actionColor = "success";
                                        actionIcon = "box-arrow-in-right";
                                        borderColor = "#10b981";
                                        break;
                                    case "LOGOUT":
                                        actionColor = "secondary";
                                        actionIcon = "box-arrow-right";
                                        borderColor = "#6b7280";
                                        break;
                                    case "UPLOAD":
                                        actionColor = "primary";
                                        actionIcon = "cloud-upload";
                                        borderColor = "#3b82f6";
                                        break;
                                    case "DOWNLOAD":
                                        actionColor = "info";
                                        actionIcon = "cloud-download";
                                        borderColor = "#06b6d4";
                                        break;
                                    case "DELETE":
                                        actionColor = "danger";
                                        actionIcon = "trash";
                                        borderColor = "#ef4444";
                                        break;
                                    case "ADMIN_ACTION":
                                        actionColor = "warning";
                                        actionIcon = "shield-check";
                                        borderColor = "#f59e0b";
                                        break;
                                    default:
                                        actionColor = "dark";
                                        actionIcon = "circle-fill";
                                        borderColor = "#374151";
                                }
                        %>
                        <div class="log-row p-3 border-bottom" data-border-color="<%= borderColor %>">
                            <div class="row align-items-center">
                                <div class="col-md-2">
                                    <div class="fw-bold">
                                        <%= log.getUsername() %>
                                    </div>
                                    <small class="text-muted"><%= log.getUserEmail() %></small>
                                </div>
                                <div class="col-md-2">
                                    <span class="badge bg-<%= actionColor %>" data-action="<%= log.getAction() %>">
                                        <i class="bi bi-<%= actionIcon %>"></i> <%= log.getAction() %>
                                    </span>
                                </div>
                                <div class="col-md-4">
                                    <div class="text-muted small">
                                        <%= log.getDescription() %>
                                    </div>
                                </div>
                                <div class="col-md-2">
                                    <small class="text-muted">
                                        <i class="bi bi-geo-alt"></i> <%= log.getIpAddress() %>
                                    </small>
                                </div>
                                <div class="col-md-2 text-end">
                                    <small class="text-muted">
                                        <i class="bi bi-clock"></i>
                                        <%= dateFormat.format(log.getCreatedAt()) %>
                                    </small>
                                </div>
                            </div>
                        </div>
                        <% }
                        } else { %>
                        <div class="text-center text-muted py-5">
                            <i class="bi bi-inbox fs-1"></i>
                            <p class="mt-3">No activity logs found</p>
                        </div>
                        <% } %>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Apply border colors from data attributes
        document.querySelectorAll('.log-row[data-border-color]').forEach(row => {
            const borderColor = row.getAttribute('data-border-color');
            row.style.borderLeftColor = borderColor;
        });
    </script>
</body>
</html>
