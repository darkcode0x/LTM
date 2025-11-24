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
    <style>
        #uploadingOverlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0, 0, 0, 0.85);
            z-index: 9999;
            display: flex;
            justify-content: center;
            align-items: center;
        }

        .uploading-content {
            text-align: center;
            background: white;
            padding: 40px;
            border-radius: 15px;
            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
        }

        .uploading-content h4 {
            font-weight: 600;
        }

        .progress {
            margin: 0 auto;
        }
    </style>
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

                        <form method="post" action="upload" enctype="multipart/form-data" id="uploadForm">
                            <div class="mb-4">
                                <label class="form-label">Select Video File</label>
                                <input type="file" class="form-control" name="videoFile"
                                       accept="video/*" required id="videoFile">
                                <small class="text-muted">Max file size: 3 GB</small>
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

                            <button type="submit" class="btn btn-primary btn-lg w-100" id="uploadBtn">
                                Upload & Convert
                            </button>
                        </form>

                        <!-- Loading Overlay -->
                        <div id="uploadingOverlay" style="display: none;">
                            <div class="uploading-content">
                                <div class="spinner-border text-primary mb-3" role="status" style="width: 4rem; height: 4rem;">
                                    <span class="visually-hidden">Loading...</span>
                                </div>
                                <h4 class="text-primary mb-2">Uploading...</h4>
                                <p class="text-muted mb-3">Please don't exit or close this window</p>
                                <div class="progress" style="width: 300px; height: 25px;">
                                    <div id="uploadProgress" class="progress-bar progress-bar-striped progress-bar-animated"
                                         role="progressbar" style="width: 0%">0%</div>
                                </div>
                                <p class="text-muted mt-2" id="fileInfo"></p>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="mt-4 text-center">
                    <a href="status" class="text-decoration-none">View your conversion jobs →</a>
                </div>
            </div>
        </div>
    </div>

    <script>
        document.getElementById('uploadForm').addEventListener('submit', function(e) {
            e.preventDefault();

            const fileInput = document.getElementById('videoFile');
            const file = fileInput.files[0];

            if (!file) {
                alert('Please select a file');
                return;
            }

            // Show overlay
            const overlay = document.getElementById('uploadingOverlay');
            overlay.style.display = 'flex';

            // Show file info
            const fileSize = (file.size / (1024 * 1024)).toFixed(2);
            document.getElementById('fileInfo').textContent = `Uploading ${file.name} (${fileSize} MB)`;

            // Disable form
            document.getElementById('uploadBtn').disabled = true;

            // Create FormData
            const formData = new FormData(this);

            // Upload with progress
            const xhr = new XMLHttpRequest();

            xhr.upload.addEventListener('progress', function(e) {
                if (e.lengthComputable) {
                    const percent = Math.round((e.loaded / e.total) * 100);
                    const progressBar = document.getElementById('uploadProgress');
                    progressBar.style.width = percent + '%';
                    progressBar.textContent = percent + '%';
                }
            });

            xhr.addEventListener('load', function() {
                if (xhr.status === 200) {
                    // Update message
                    document.querySelector('.uploading-content h4').textContent = 'Upload Complete!';
                    document.querySelector('.uploading-content p').textContent = 'Redirecting to status page...';

                    // Redirect after short delay
                    setTimeout(function() {
                        window.location.href = 'status?success=true';
                    }, 1000);
                } else {
                    alert('Upload failed. Please try again.');
                    overlay.style.display = 'none';
                    document.getElementById('uploadBtn').disabled = false;
                }
            });

            xhr.addEventListener('error', function() {
                alert('Upload error. Please check your connection and try again.');
                overlay.style.display = 'none';
                document.getElementById('uploadBtn').disabled = false;
            });

            xhr.open('POST', 'upload', true);
            xhr.send(formData);
        });

        // Prevent accidental page close
        window.addEventListener('beforeunload', function(e) {
            const overlay = document.getElementById('uploadingOverlay');
            if (overlay.style.display === 'flex') {
                e.preventDefault();
                e.returnValue = 'Upload in progress. Are you sure you want to leave?';
                return e.returnValue;
            }
        });
    </script>
</body>
</html>

