<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.videoconverter.model.User" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null || !currentUser.isAdmin()) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    
    List<User> users = (List<User>) request.getAttribute("users");
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
    
    String message = (String) session.getAttribute("message");
    String error = (String) session.getAttribute("error");
    session.removeAttribute("message");
    session.removeAttribute("error");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Users - Admin Panel</title>
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
        .user-avatar {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            object-fit: cover;
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
                       class="admin-nav-link active">
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
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h2 class="mb-0">
                        <i class="bi bi-people text-primary"></i> User Management
                    </h2>
                    <div class="text-muted">
                        Total: <%= users != null ? users.size() : 0 %> users
                    </div>
                </div>

                <!-- Alert Messages -->
                <% if (message != null) { %>
                    <div class="alert alert-success alert-dismissible fade show" role="alert">
                        <i class="bi bi-check-circle"></i> <%= message %>
                        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                    </div>
                <% } %>
                <% if (error != null) { %>
                    <div class="alert alert-danger alert-dismissible fade show" role="alert">
                        <i class="bi bi-exclamation-triangle"></i> <%= error %>
                        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                    </div>
                <% } %>

                <!-- Users Table -->
                <div class="card border-0 shadow-sm">
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table table-hover align-middle">
                                <thead class="table-light">
                                    <tr>
                                        <th>ID</th>
                                        <th>User</th>
                                        <th>Email</th>
                                        <th>Role</th>
                                        <th>Status</th>
                                        <th>Daily Quota</th>
                                        <th>Total Conversions</th>
                                        <th>Joined</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% if (users != null && !users.isEmpty()) {
                                        for (User user : users) { %>
                                    <tr>
                                        <td><strong>#<%= user.getUserId() %></strong></td>
                                        <td>
                                            <div class="d-flex align-items-center">
                                                <img src="<%= request.getContextPath() %>/<%= user.getAvatar() != null ? user.getAvatar() : "images/default-avatar.png" %>" 
                                                     alt="Avatar" class="user-avatar me-2"
                                                     onerror="this.src='<%= request.getContextPath() %>/images/default-avatar.png'">
                                                <div>
                                                    <div class="fw-bold"><%= user.getUsername() %></div>
                                                    <small class="text-muted"><%= user.getFullName() != null ? user.getFullName() : "-" %></small>
                                                </div>
                                            </div>
                                        </td>
                                        <td><%= user.getEmail() %></td>
                                        <td>
                                            <% if (user.isAdmin()) { %>
                                                <span class="badge bg-warning text-dark">
                                                    <i class="bi bi-shield-fill-check"></i> ADMIN
                                                </span>
                                            <% } else { %>
                                                <span class="badge bg-secondary">
                                                    <i class="bi bi-person"></i> USER
                                                </span>
                                            <% } %>
                                        </td>
                                        <td>
                                            <% if (user.isActive()) { %>
                                                <span class="badge bg-success">
                                                    <i class="bi bi-check-circle"></i> Active
                                                </span>
                                            <% } else { %>
                                                <span class="badge bg-danger">
                                                    <i class="bi bi-x-circle"></i> Inactive
                                                </span>
                                            <% } %>
                                        </td>
                                        <td>
                                            <div class="input-group input-group-sm" style="width: 120px;">
                                                <input type="number" class="form-control" 
                                                       value="<%= user.getDailyQuota() %>" 
                                                       min="0" max="100"
                                                       id="quota-<%= user.getUserId() %>"
                                                       data-user-id="<%= user.getUserId() %>">
                                                <button class="btn btn-outline-primary" type="button"
                                                        data-user-id="<%= user.getUserId() %>"
                                                        class="btn-update-quota">
                                                    <i class="bi bi-check"></i>
                                                </button>
                                            </div>
                                        </td>
                                        <td>
                                            <span class="badge bg-info text-dark">
                                                <%= user.getTotalConversions() %>
                                            </span>
                                        </td>
                                        <td>
                                            <small class="text-muted">
                                                <%= user.getCreatedAt() != null ? dateFormat.format(user.getCreatedAt()) : "-" %>
                                            </small>
                                        </td>
                                        <td>
                                            <% if (user.getUserId() != currentUser.getUserId()) { %>
                                                <button class="btn btn-sm <%= user.isActive() ? "btn-outline-danger" : "btn-outline-success" %> btn-toggle-status"
                                                        data-user-id="<%= user.getUserId() %>"
                                                        data-username="<%= user.getUsername() %>"
                                                        data-is-active="<%= user.isActive() %>">
                                                    <i class="bi bi-<%= user.isActive() ? "x-circle" : "check-circle" %>"></i>
                                                    <%= user.isActive() ? "Disable" : "Enable" %>
                                                </button>
                                            <% } else { %>
                                                <span class="text-muted small">Current User</span>
                                            <% } %>
                                        </td>
                                    </tr>
                                    <% }
                                    } else { %>
                                    <tr>
                                        <td colspan="9" class="text-center text-muted py-4">
                                            <i class="bi bi-inbox fs-1"></i>
                                            <p>No users found</p>
                                        </td>
                                    </tr>
                                    <% } %>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        const contextPath = '<%= request.getContextPath() %>';
        
        // Toggle user status
        document.querySelectorAll('.btn-toggle-status').forEach(btn => {
            btn.addEventListener('click', function() {
                const userId = this.getAttribute('data-user-id');
                const username = this.getAttribute('data-username');
                const isActive = this.getAttribute('data-is-active') === 'true';
                const action = isActive ? 'deactivate' : 'activate';
                
                if (confirm(`Are you sure you want to ${action} user "${username}"?`)) {
                    window.location.href = contextPath + '/admin?action=toggleUserStatus&userId=' + userId;
                }
            });
        });
        
        // Update quota
        document.querySelectorAll('.btn-update-quota').forEach(btn => {
            btn.addEventListener('click', function() {
                const userId = this.getAttribute('data-user-id');
                const quota = document.getElementById('quota-' + userId).value;
                
                if (confirm('Update daily quota to ' + quota + '?')) {
                    window.location.href = contextPath + '/admin?action=updateUserQuota&userId=' + userId + '&quota=' + quota;
                }
            });
        });
    </script>
</body>
</html>
