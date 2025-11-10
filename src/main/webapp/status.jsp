<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="includes/header.jsp">
    <jsp:param name="title" value="Conversion Status - Video Converter"/>
</jsp:include>

<div class="row">
    <div class="col-12">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2><i class="bi bi-list-check"></i> Conversion Status</h2>
            <a href="${pageContext.request.contextPath}/upload" class="btn btn-primary">
                <i class="bi bi-plus-circle me-2"></i>New Conversion
            </a>
        </div>
        
        <!-- Success Message -->
        <c:if test="${not empty sessionScope.successMessage}">
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                <i class="bi bi-check-circle-fill me-2"></i>${sessionScope.successMessage}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
            <c:remove var="successMessage" scope="session"/>
        </c:if>
        
        <!-- Error Message -->
        <c:if test="${not empty sessionScope.errorMessage}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2"></i>${sessionScope.errorMessage}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
            <c:remove var="errorMessage" scope="session"/>
        </c:if>
        
        <!-- Statistics Cards -->
        <div class="row mb-4">
            <div class="col-md-3">
                <div class="card bg-primary text-white">
                    <div class="card-body text-center">
                        <h3 class="mb-0">${totalJobs != null ? totalJobs : 0}</h3>
                        <small>Total Jobs</small>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="card bg-warning text-dark">
                    <div class="card-body text-center">
                        <h3 class="mb-0">${pendingCount != null ? pendingCount : 0}</h3>
                        <small>Pending</small>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="card bg-info text-white">
                    <div class="card-body text-center">
                        <h3 class="mb-0">${processingCount != null ? processingCount : 0}</h3>
                        <small>Processing</small>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="card bg-success text-white">
                    <div class="card-body text-center">
                        <h3 class="mb-0">${completedCount != null ? completedCount : 0}</h3>
                        <small>Completed</small>
                    </div>
                </div>
            </div>
        </div>
        
        <!-- Filter Buttons -->
        <div class="card mb-4">
            <div class="card-body">
                <div class="btn-group" role="group">
                    <a href="${pageContext.request.contextPath}/status" 
                       class="btn ${empty statusFilter ? 'btn-primary' : 'btn-outline-primary'}">
                        All
                    </a>
                    <a href="${pageContext.request.contextPath}/status?status=PENDING" 
                       class="btn ${statusFilter == 'PENDING' ? 'btn-warning' : 'btn-outline-warning'}">
                        Pending
                    </a>
                    <a href="${pageContext.request.contextPath}/status?status=PROCESSING" 
                       class="btn ${statusFilter == 'PROCESSING' ? 'btn-info' : 'btn-outline-info'}">
                        Processing
                    </a>
                    <a href="${pageContext.request.contextPath}/status?status=COMPLETED" 
                       class="btn ${statusFilter == 'COMPLETED' ? 'btn-success' : 'btn-outline-success'}">
                        Completed
                    </a>
                    <a href="${pageContext.request.contextPath}/status?status=FAILED" 
                       class="btn ${statusFilter == 'FAILED' ? 'btn-danger' : 'btn-outline-danger'}">
                        Failed
                    </a>
                </div>
                
                <button class="btn btn-outline-secondary float-end" onclick="location.reload()">
                    <i class="bi bi-arrow-clockwise"></i> Refresh
                </button>
            </div>
        </div>
        
        <!-- Jobs Table -->
        <div class="card">
            <div class="card-body">
                <c:choose>
                    <c:when test="${empty jobs}">
                        <div class="text-center py-5">
                            <i class="bi bi-inbox" style="font-size: 4rem; color: #ccc;"></i>
                            <h4 class="mt-3">No conversion jobs found</h4>
                            <p class="text-muted">Upload a video to get started</p>
                            <a href="${pageContext.request.contextPath}/upload" class="btn btn-primary">
                                <i class="bi bi-cloud-upload me-2"></i>Upload Video
                            </a>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="table-responsive">
                            <table class="table table-hover">
                                <thead class="table-light">
                                    <tr>
                                        <th>Job ID</th>
                                        <th>Output Format</th>
                                        <th>Quality</th>
                                        <th>Status</th>
                                        <th>Progress</th>
                                        <th>Created</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="job" items="${jobs}">
                                        <tr>
                                            <td><strong>#${job.jobId}</strong></td>
                                            <td>
                                                <span class="badge bg-secondary">${job.outputFormat}</span>
                                                <c:if test="${not empty job.outputResolution}">
                                                    <br><small class="text-muted">${job.outputResolution}</small>
                                                </c:if>
                                            </td>
                                            <td>${job.quality}</td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${job.status == 'PENDING'}">
                                                        <span class="badge bg-warning text-dark">
                                                            <i class="bi bi-clock"></i> Pending
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${job.status == 'PROCESSING'}">
                                                        <span class="badge bg-info">
                                                            <i class="bi bi-gear-fill"></i> Processing
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${job.status == 'COMPLETED'}">
                                                        <span class="badge bg-success">
                                                            <i class="bi bi-check-circle-fill"></i> Completed
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${job.status == 'FAILED'}">
                                                        <span class="badge bg-danger">
                                                            <i class="bi bi-x-circle-fill"></i> Failed
                                                        </span>
                                                    </c:when>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:set var="progressBarClass" value="${job.status == 'COMPLETED' ? 'bg-success' : job.status == 'FAILED' ? 'bg-danger' : job.status == 'PROCESSING' ? 'bg-info progress-bar-striped progress-bar-animated' : 'bg-warning'}" />
                                                <div class="progress" style="height: 25px;">
                                                    <div class="progress-bar ${progressBarClass}" 
                                                         role="progressbar" 
                                                         data-progress="${job.progress}"
                                                         aria-valuenow="${job.progress}"
                                                         aria-valuemin="0"
                                                         aria-valuemax="100">
                                                        ${job.progress}%
                                                    </div>
                                                </div>
                                            </td>
                                            <td>
                                                <small>
                                                    <fmt:formatDate value="${job.createdAt}" 
                                                                    pattern="yyyy-MM-dd HH:mm"/>
                                                </small>
                                            </td>
                                            <td>
                                                <div class="btn-group btn-group-sm">
                                                    <c:if test="${job.status == 'COMPLETED'}">
                                                        <a href="${pageContext.request.contextPath}/download?jobId=${job.jobId}" 
                                                           class="btn btn-success" title="Download">
                                                            <i class="bi bi-download"></i>
                                                        </a>
                                                    </c:if>
                                                    <a href="${pageContext.request.contextPath}/deleteJob?jobId=${job.jobId}" 
                                                       class="btn btn-danger" 
                                                       onclick="return confirm('Are you sure you want to delete this job?')"
                                                       title="Delete">
                                                        <i class="bi bi-trash"></i>
                                                    </a>
                                                </div>
                                            </td>
                                        </tr>
                                        <c:if test="${not empty job.errorMessage}">
                                            <tr>
                                                <td colspan="7">
                                                    <div class="alert alert-danger mb-0">
                                                        <strong>Error:</strong> ${job.errorMessage}
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:if>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>

<!-- Data for JavaScript -->
<div id="statusPageData" 
     data-processing-count="${processingCount != null ? processingCount : 0}"
     style="display: none;"></div>

<script>
    // Set progress bar widths from data attributes
    document.addEventListener('DOMContentLoaded', function() {
        var progressBars = document.querySelectorAll('.progress-bar[data-progress]');
        progressBars.forEach(function(bar) {
            var progress = bar.getAttribute('data-progress');
            bar.style.width = progress + '%';
        });
    });
    
    // Auto-reload page every 5 seconds to update progress
    (function() {
        var dataEl = document.getElementById('statusPageData');
        var processingCount = parseInt(dataEl.getAttribute('data-processing-count')) || 0;
        
        if (processingCount > 0) {
            console.log('Auto-refresh enabled: ' + processingCount + ' processing job(s)');
            
            // Show countdown notification
            var countdown = 5;
            var notification = document.createElement('div');
            notification.className = 'position-fixed bottom-0 end-0 m-3 alert alert-info fade-in';
            notification.innerHTML = '<i class="bi bi-arrow-clockwise me-2"></i>Auto-refresh in <strong id="countdown">' + countdown + '</strong>s';
            document.body.appendChild(notification);
            
            var timer = setInterval(function() {
                countdown--;
                var countdownEl = document.getElementById('countdown');
                if (countdownEl) {
                    countdownEl.textContent = countdown;
                }
                
                if (countdown <= 0) {
                    clearInterval(timer);
                }
            }, 1000);
            
            // Reload page after 5 seconds
            setTimeout(function() {
                console.log('Refreshing page...');
                location.reload();
            }, 5000);
        } else {
            console.log('Auto-refresh disabled: No processing jobs');
        }
    })();
</script>

<jsp:include page="includes/footer.jsp"/>
