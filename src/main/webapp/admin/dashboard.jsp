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
    
    int totalUsers = (int) request.getAttribute("totalUsers");
    int activeUsers = (int) request.getAttribute("activeUsers");
    int adminCount = (int) request.getAttribute("adminCount");
    List<ActivityLog> recentLogs = (List<ActivityLog>) request.getAttribute("recentLogs");
    
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard - Video Converter</title>
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
        .stat-card {
            border-left: 4px solid;
            transition: transform 0.3s;
        }
        .stat-card:hover {
            transform: translateY(-5px);
        }
        .activity-item {
            border-left: 3px solid #e2e8f0;
            padding-left: 1rem;
            margin-bottom: 1rem;
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
                       class="admin-nav-link active">
                        <i class="bi bi-speedometer2"></i> Dashboard
                    </a>
                    <a href="<%= request.getContextPath() %>/admin?action=users" 
                       class="admin-nav-link">
                        <i class="bi bi-people"></i> Manage Users
                    </a>
                    <a href="<%= request.getContextPath() %>/admin?action=logs" 
                       class="admin-nav-link">
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
                <h2 class="mb-4">Dashboard Overview</h2>

                <!-- Statistics Cards -->
                <div class="row g-4 mb-4">
                    <div class="col-md-4">
                        <div class="card stat-card border-0 shadow-sm" style="border-left-color: #3b82f6;">
                            <div class="card-body">
                                <div class="d-flex justify-content-between align-items-center">
                                    <div>
                                        <p class="text-muted mb-1">Total Users</p>
                                        <h3 class="mb-0"><%= totalUsers %></h3>
                                    </div>
                                    <div class="fs-1 text-primary">
                                        <i class="bi bi-people-fill"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="col-md-4">
                        <div class="card stat-card border-0 shadow-sm" style="border-left-color: #10b981;">
                            <div class="card-body">
                                <div class="d-flex justify-content-between align-items-center">
                                    <div>
                                        <p class="text-muted mb-1">Active Users</p>
                                        <h3 class="mb-0"><%= activeUsers %></h3>
                                    </div>
                                    <div class="fs-1 text-success">
                                        <i class="bi bi-person-check-fill"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="col-md-4">
                        <div class="card stat-card border-0 shadow-sm" style="border-left-color: #f59e0b;">
                            <div class="card-body">
                                <div class="d-flex justify-content-between align-items-center">
                                    <div>
                                        <p class="text-muted mb-1">Administrators</p>
                                        <h3 class="mb-0"><%= adminCount %></h3>
                                    </div>
                                    <div class="fs-1 text-warning">
                                        <i class="bi bi-shield-fill-check"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Recent Activity -->
                <div class="card border-0 shadow-sm">
                    <div class="card-header bg-white border-0 py-3">
                        <h5 class="mb-0">
                            <i class="bi bi-activity text-primary"></i> Recent Activity (Last 24h)
                        </h5>
                    </div>
                    <div class="card-body">
                        <% if (recentLogs != null && !recentLogs.isEmpty()) { %>
                            <% for (ActivityLog log : recentLogs) { 
                                String actionColor = "";
                                String actionIcon = "";
                                switch (log.getAction()) {
                                    case "LOGIN":
                                        actionColor = "success";
                                        actionIcon = "box-arrow-in-right";
                                        break;
                                    case "LOGOUT":
                                        actionColor = "secondary";
                                        actionIcon = "box-arrow-right";
                                        break;
                                    case "UPLOAD":
                                        actionColor = "primary";
                                        actionIcon = "cloud-upload";
                                        break;
                                    case "DOWNLOAD":
                                        actionColor = "info";
                                        actionIcon = "cloud-download";
                                        break;
                                    case "DELETE":
                                        actionColor = "danger";
                                        actionIcon = "trash";
                                        break;
                                    case "ADMIN_ACTION":
                                        actionColor = "warning";
                                        actionIcon = "shield-check";
                                        break;
                                    default:
                                        actionColor = "dark";
                                        actionIcon = "circle-fill";
                                }
                            %>
                            <div class="activity-item">
                                <div class="d-flex justify-content-between">
                                    <div>
                                        <span class="badge bg-<%= actionColor %> mb-2">
                                            <i class="bi bi-<%= actionIcon %>"></i> <%= log.getAction() %>
                                        </span>
                                        <p class="mb-1">
                                            <strong><%= log.getUsername() %></strong>
                                            <span class="text-muted">(<%= log.getUserEmail() %>)</span>
                                        </p>
                                        <p class="text-muted small mb-0"><%= log.getDescription() %></p>
                                        <small class="text-muted">
                                            <i class="bi bi-geo-alt"></i> <%= log.getIpAddress() %>
                                        </small>
                                    </div>
                                    <div class="text-end text-muted small">
                                        <%= dateFormat.format(log.getCreatedAt()) %>
                                    </div>
                                </div>
                            </div>
                            <% } %>
                        <% } else { %>
                            <div class="text-center text-muted py-4">
                                <i class="bi bi-inbox fs-1"></i>
                                <p>No recent activity</p>
                            </div>
                        <% } %>
                        
                        <div class="text-center mt-3">
                            <a href="<%= request.getContextPath() %>/admin?action=logs" class="btn btn-outline-primary">
                                View All Logs <i class="bi bi-arrow-right"></i>
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
