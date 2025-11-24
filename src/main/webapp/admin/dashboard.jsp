<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.videoconverter.model.bean.User" %>
<%@ page import="java.util.Map" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null || !user.isAdmin()) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }

    int totalUsers = (int) request.getAttribute("totalUsers");
    Map<Integer, Integer> conversionCounts = (Map<Integer, Integer>) request.getAttribute("conversionCounts");
    int totalConversions = conversionCounts.values().stream().mapToInt(Integer::intValue).sum();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard - Video Converter</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/css/style.css" rel="stylesheet">
</head>
<body>
    <nav class="navbar navbar-dark bg-dark">
        <div class="container">
            <span class="navbar-brand">Video Converter - Admin</span>
            <div>
                <span class="text-white me-3">Admin: <%= user.getUsername() %></span>
                <a href="<%= request.getContextPath() %>/logout" class="btn btn-outline-light btn-sm">Logout</a>
            </div>
        </div>
    </nav>

    <div class="container mt-5">
        <h2 class="mb-4">Admin Dashboard</h2>

        <div class="row mb-4">
            <div class="col-md-6">
                <div class="card border-primary">
                    <div class="card-body text-center">
                        <h5 class="card-title text-muted">Total Users</h5>
                        <h2 class="text-primary"><%= totalUsers %></h2>
                    </div>
                </div>
            </div>
            <div class="col-md-6">
                <div class="card border-success">
                    <div class="card-body text-center">
                        <h5 class="card-title text-muted">Total Conversions</h5>
                        <h2 class="text-success"><%= totalConversions %></h2>
                    </div>
                </div>
            </div>
        </div>

        <div class="card">
            <div class="card-header">
                <h5 class="mb-0">Conversion Statistics by User</h5>
            </div>
            <div class="card-body">
                <% if (conversionCounts.isEmpty()) { %>
                    <p class="text-muted">No conversions yet.</p>
                <% } else { %>
                    <table class="table table-striped">
                        <thead>
                            <tr>
                                <th>User ID</th>
                                <th>Completed Conversions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (Map.Entry<Integer, Integer> entry : conversionCounts.entrySet()) { %>
                                <tr>
                                    <td>User #<%= entry.getKey() %></td>
                                    <td><span class="badge bg-success"><%= entry.getValue() %></span></td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                <% } %>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

