<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="includes/header.jsp">
    <jsp:param name="title" value="Upload Video - Video Converter"/>
</jsp:include>

<div class="row justify-content-center">
    <div class="col-lg-8">
        <div class="card">
            <div class="card-header bg-primary text-white">
                <h4 class="mb-0"><i class="bi bi-cloud-upload-fill"></i> Upload & Convert Video</h4>
            </div>
            <div class="card-body p-4">
                <!-- Success Message -->
                <c:if test="${not empty sessionScope.successMessage}">
                    <div class="alert alert-success alert-dismissible fade show" role="alert">
                        <i class="bi bi-check-circle-fill me-2"></i>${sessionScope.successMessage}
                        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                    </div>
                    <c:remove var="successMessage" scope="session"/>
                </c:if>
                
                <!-- Error Message -->
                <c:if test="${not empty error}">
                    <div class="alert alert-danger alert-dismissible fade show" role="alert">
                        <i class="bi bi-exclamation-triangle-fill me-2"></i>${error}
                        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                    </div>
                </c:if>
                
                <!-- Upload Form -->
                <form action="${pageContext.request.contextPath}/upload" method="post" 
                      enctype="multipart/form-data" id="uploadForm">
                    
                    <!-- File Upload -->
                    <div class="mb-4">
                        <label for="videoFile" class="form-label">
                            <i class="bi bi-file-earmark-play"></i> Select Video File *
                        </label>
                        <input type="file" class="form-control form-control-lg" id="videoFile" 
                               name="videoFile" accept="video/*" required>
                        <div class="form-text">
                            Maximum file size: 500 MB. Supported formats: MP4, AVI, MKV, WebM, MOV, FLV
                        </div>
                        <div id="fileInfo" class="mt-2"></div>
                    </div>
                    
                    <hr class="my-4">
                    
                    <h5 class="mb-3"><i class="bi bi-gear-fill"></i> Conversion Settings</h5>
                    
                    <div class="row">
                        <!-- Output Format -->
                        <div class="col-md-4 mb-3">
                            <label for="outputFormat" class="form-label">Output Format *</label>
                            <select class="form-select" id="outputFormat" name="outputFormat" required>
                                <option value="mp4" selected>MP4 (Recommended)</option>
                                <option value="avi">AVI</option>
                                <option value="mkv">MKV</option>
                                <option value="webm">WebM</option>
                                <option value="mov">MOV</option>
                                <option value="flv">FLV</option>
                            </select>
                        </div>
                        
                        <!-- Output Resolution -->
                        <div class="col-md-4 mb-3">
                            <label for="outputResolution" class="form-label">Resolution</label>
                            <select class="form-select" id="outputResolution" name="outputResolution">
                                <option value="">Keep Original</option>
                                <option value="3840x2160">4K (3840x2160)</option>
                                <option value="1920x1080">Full HD (1920x1080)</option>
                                <option value="1280x720" selected>HD (1280x720)</option>
                                <option value="854x480">SD (854x480)</option>
                                <option value="640x360">Low (640x360)</option>
                            </select>
                        </div>
                        
                        <!-- Quality -->
                        <div class="col-md-4 mb-3">
                            <label for="quality" class="form-label">Quality *</label>
                            <select class="form-select" id="quality" name="quality" required>
                                <option value="high">High Quality</option>
                                <option value="medium" selected>Medium Quality</option>
                                <option value="low">Low Quality</option>
                            </select>
                        </div>
                    </div>
                    
                    <!-- Advanced Options (Collapsible) -->
                    <div class="mb-3">
                        <button class="btn btn-outline-secondary btn-sm" type="button" 
                                data-bs-toggle="collapse" data-bs-target="#advancedOptions">
                            <i class="bi bi-chevron-down"></i> Advanced Options
                        </button>
                    </div>
                    
                    <div class="collapse" id="advancedOptions">
                        <div class="card card-body bg-light">
                            <h6><i class="bi bi-scissors"></i> Trim Video (Optional)</h6>
                            <div class="row">
                                <div class="col-md-6 mb-3">
                                    <label for="startTime" class="form-label">Start Time (seconds)</label>
                                    <input type="number" class="form-control" id="startTime" 
                                           name="startTime" min="0" placeholder="0">
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label for="endTime" class="form-label">End Time (seconds)</label>
                                    <input type="number" class="form-control" id="endTime" 
                                           name="endTime" min="0" placeholder="Leave empty for full video">
                                </div>
                            </div>
                            <small class="text-muted">
                                <i class="bi bi-info-circle"></i> 
                                Leave both empty to convert the entire video
                            </small>
                        </div>
                    </div>
                    
                    <hr class="my-4">
                    
                    <!-- Submit Button -->
                    <div class="d-grid gap-2">
                        <button type="submit" class="btn btn-primary btn-lg" id="submitBtn">
                            <i class="bi bi-cloud-upload-fill me-2"></i>Upload & Convert
                        </button>
                        <a href="${pageContext.request.contextPath}/status" class="btn btn-outline-secondary">
                            <i class="bi bi-list-check me-2"></i>View Conversion Status
                        </a>
                    </div>
                </form>
            </div>
        </div>
        
        <!-- Info Card -->
        <div class="card mt-4 bg-light">
            <div class="card-body">
                <h6><i class="bi bi-info-circle-fill"></i> How it works:</h6>
                <ol class="mb-0">
                    <li>Select a video file from your device</li>
                    <li>Choose output format, resolution, and quality</li>
                    <li>Click "Upload & Convert" to start the conversion</li>
                    <li>Your video will be processed in the background</li>
                    <li>Check the Status page to monitor progress</li>
                    <li>Download your converted video when ready</li>
                </ol>
            </div>
        </div>
    </div>
</div>

<script>
    // Page-specific file validation
    document.getElementById('videoFile').addEventListener('change', function(e) {
        const file = e.target.files[0];
        const fileInfo = document.getElementById('fileInfo');
        
        if (file) {
            const size = (file.size / (1024 * 1024)).toFixed(2);
            fileInfo.innerHTML = `
                <div class="alert alert-info mb-0">
                    <strong>File:</strong> ${file.name}<br>
                    <strong>Size:</strong> ${size} MB<br>
                    <strong>Type:</strong> ${file.type}
                </div>
            `;
            
            // Check file size
            if (file.size > 500 * 1024 * 1024) {
                fileInfo.innerHTML = `
                    <div class="alert alert-danger mb-0">
                        File size exceeds 500 MB limit!
                    </div>
                `;
            }
        } else {
            fileInfo.innerHTML = '';
        }
    });
</script>

<jsp:include page="includes/footer.jsp"/>
