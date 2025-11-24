<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.videoconverter.model.bean.User" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("login");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Upload Video - Video Converter</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="css/style.css" rel="stylesheet">
</head>
<body>
    <nav class="navbar navbar-dark bg-primary">
        <div class="container">
            <span class="navbar-brand">Video Converter</span>
            <div>
                <span class="text-white me-3">Welcome, <%= user.getUsername() %></span>
                <a href="status" class="btn btn-light btn-sm me-2">My Jobs</a>
                <a href="logout" class="btn btn-outline-light btn-sm">Logout</a>
            </div>
        </div>
    </nav>

    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-8">
                <div class="card shadow">
                    <div class="card-body p-4">
                        <h3 class="mb-4">Upload & Convert Video</h3>

                        <% if (request.getAttribute("error") != null) { %>
                            <div class="alert alert-danger">
                                <%= request.getAttribute("error") %>
                            </div>
                        <% } %>

                        <form method="post" action="upload" enctype="multipart/form-data">
                            <div class="mb-4">
                                <label class="form-label">Select Video File</label>
                                <input type="file" class="form-control" name="videoFile"
                                       accept="video/*" required>
                                <small class="text-muted">Max file size: 500 MB</small>
                            </div>

                            <div class="mb-4">
                                <label class="form-label">Output Format</label>
                                <select class="form-select" name="outputFormat" required>
                                    <option value="">-- Select Format --</option>
                                    <option value="mp4">MP4 (H.264)</option>
                                    <option value="avi">AVI</option>
                                    <option value="mkv">MKV</option>
                                    <option value="mov">MOV (QuickTime)</option>
                                    <option value="webm">WebM</option>
                                </select>
                            </div>

                            <button type="submit" class="btn btn-primary btn-lg w-100">
                                Upload & Convert
                            </button>
                        </form>
                    </div>
                </div>

                <div class="mt-4 text-center">
                    <a href="status" class="text-decoration-none">View your conversion jobs →</a>
                </div>
            </div>
        </div>
    </div>
</body>
</html>

