<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.videoconverter.model.bean.User" %>
<%@ page import="com.videoconverter.model.bean.ConversionJob" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.TimeZone" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("login");
        return;
    }

    List<ConversionJob> jobs = (List<ConversionJob>) request.getAttribute("jobs");
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");
    dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Jobs - Video Converter</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="css/style.css" rel="stylesheet">
    <meta http-equiv="refresh" content="5">
</head>
<body>
    <nav class="navbar navbar-dark bg-primary">
        <div class="container">
            <span class="navbar-brand">Video Converter</span>
            <div>
                <span class="text-white me-3">Welcome, <%= user.getUsername() %></span>
                <a href="upload" class="btn btn-light btn-sm me-2">Upload</a>
                <a href="logout" class="btn btn-outline-light btn-sm">Logout</a>
            </div>
        </div>
    </nav>

    <div class="container mt-4">
        <h3 class="mb-4">My Conversion Jobs</h3>


        <% if (jobs == null || jobs.isEmpty()) { %>
            <div class="alert alert-info">
                No conversion jobs yet. <a href="upload" class="alert-link">Upload a video</a> to get started!
            </div>
        <% } else { %>
            <div class="table-responsive">
                <table class="table table-hover">
                    <thead>
                        <tr>
                            <th>Video</th>
                            <th>Format</th>
                            <th>Status</th>
                            <th>Progress</th>
                            <th>Created</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (ConversionJob job : jobs) { %>
                            <tr>
                                <td><%= job.getVideoFilename() %></td>
                                <td><%= job.getOutputFormat().toUpperCase() %></td>
                                <td>
                                    <span class="badge <%= job.getStatusBadgeClass() %>">
                                        <%= job.getStatus() %>
                                    </span>
                                </td>
                                <td>
                                    <div class="progress" style="width: 100px;">
                                        <div class="progress-bar <%= job.getStatusBadgeClass() %>"
                                             style="width: <%= job.getProgress() %>%">
                                            <%= job.getProgress() %>%
                                        </div>
                                    </div>
                                </td>
                                <td><%= dateFormat.format(job.getCreatedAt()) %></td>
                                <td>
                                    <% if ("COMPLETED".equals(job.getStatus())) { %>
                                        <a href="download?jobId=<%= job.getJobId() %>"
                                           class="btn btn-sm btn-success">Download</a>
                                    <% } %>
                                    <form method="post" action="status" style="display: inline;">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="jobId" value="<%= job.getJobId() %>">
                                        <button type="submit" class="btn btn-sm btn-danger"
                                                onclick="return confirm('Delete this job?')">Delete</button>
                                    </form>
                                </td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
            <small class="text-muted">Page auto-refreshes every 5 seconds</small>
        <% } %>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

