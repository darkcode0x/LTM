<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="includes/header.jsp">
    <jsp:param name="title" value="Profile - Video Converter"/>
</jsp:include>

<div class="row">
    <div class="col-lg-4 mb-4">
        <!-- User Info Card -->
        <div class="card">
            <div class="card-body text-center">
                <img src="${pageContext.request.contextPath}/<c:out value='${user.avatar}'/>" 
                     alt="Avatar" class="rounded-circle mb-3" 
                     style="width: 150px; height: 150px; object-fit: cover;"
                     onerror="this.src='${pageContext.request.contextPath}/images/default-avatar.png'">
                <h4>${user.fullName}</h4>
                <p class="text-muted mb-0">@${user.username}</p>
                <p class="text-muted"><i class="bi bi-envelope"></i> ${user.email}</p>
                
                <!-- Upload Avatar Form -->
                <form action="${pageContext.request.contextPath}/profile" method="post" 
                      enctype="multipart/form-data" class="mt-3">
                    <input type="hidden" name="action" value="uploadAvatar">
                    <div class="input-group input-group-sm">
                        <input type="file" class="form-control" name="avatarFile" 
                               accept="image/*" required>
                        <button class="btn btn-primary" type="submit">
                            <i class="bi bi-upload"></i>
                        </button>
                    </div>
                </form>
            </div>
        </div>
        
        <!-- Account Info -->
        <div class="card mt-4">
            <div class="card-header">
                <h6 class="mb-0"><i class="bi bi-info-circle"></i> Account Information</h6>
            </div>
            <div class="card-body">
                <p><strong>User ID:</strong> ${user.userId}</p>
                <p><strong>Member Since:</strong><br>
                    <fmt:formatDate value="${user.createdAt}" pattern="MMMM dd, yyyy"/>
                </p>
                <p><strong>Last Login:</strong><br>
                    <fmt:formatDate value="${user.lastLogin}" pattern="yyyy-MM-dd HH:mm"/>
                </p>
                <p><strong>Status:</strong> 
                    <span class="badge ${user.active ? 'bg-success' : 'bg-danger'}">
                        ${user.active ? 'Active' : 'Inactive'}
                    </span>
                </p>
                <p class="mb-0"><strong>Daily Quota:</strong> ${user.dailyQuota} conversions/day</p>
            </div>
        </div>
    </div>
    
    <div class="col-lg-8">
        <!-- Success/Error Messages -->
        <c:if test="${not empty sessionScope.successMessage}">
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                <i class="bi bi-check-circle-fill me-2"></i>${sessionScope.successMessage}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
            <c:remove var="successMessage" scope="session"/>
        </c:if>
        
        <c:if test="${not empty sessionScope.errorMessage}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2"></i>${sessionScope.errorMessage}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
            <c:remove var="errorMessage" scope="session"/>
        </c:if>
        
        <!-- Statistics -->
        <div class="row mb-4">
            <div class="col-md-3">
                <div class="card bg-primary text-white">
                    <div class="card-body text-center">
                        <h3>${totalVideos}</h3>
                        <small>Videos Uploaded</small>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="card bg-success text-white">
                    <div class="card-body text-center">
                        <h3>${completedJobs}</h3>
                        <small>Completed</small>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="card bg-warning text-dark">
                    <div class="card-body text-center">
                        <h3>${pendingJobs + processingJobs}</h3>
                        <small>In Progress</small>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="card bg-info text-white">
                    <div class="card-body text-center">
                        <h3>${successRate}%</h3>
                        <small>Success Rate</small>
                    </div>
                </div>
            </div>
        </div>
        
        <!-- Update Profile Form -->
        <div class="card mb-4">
            <div class="card-header">
                <h5 class="mb-0"><i class="bi bi-person-fill"></i> Update Profile</h5>
            </div>
            <div class="card-body">
                <form action="${pageContext.request.contextPath}/profile" method="post">
                    <input type="hidden" name="action" value="updateProfile">
                    
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label for="fullName" class="form-label">Full Name *</label>
                            <input type="text" class="form-control" id="fullName" name="fullName" 
                                   value="${user.fullName}" required>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label for="email" class="form-label">Email *</label>
                            <input type="email" class="form-control" id="email" name="email" 
                                   value="${user.email}" required>
                        </div>
                    </div>
                    
                    <div class="mb-3">
                        <label for="phone" class="form-label">Phone Number</label>
                        <input type="tel" class="form-control" id="phone" name="phone" 
                               value="${user.phone}">
                    </div>
                    
                    <button type="submit" class="btn btn-primary">
                        <i class="bi bi-save"></i> Save Changes
                    </button>
                </form>
            </div>
        </div>
        
        <!-- Change Password Form -->
        <div class="card mb-4">
            <div class="card-header">
                <h5 class="mb-0"><i class="bi bi-key-fill"></i> Change Password</h5>
            </div>
            <div class="card-body">
                <form action="${pageContext.request.contextPath}/profile" method="post">
                    <input type="hidden" name="action" value="changePassword">
                    
                    <div class="mb-3">
                        <label for="currentPassword" class="form-label">Current Password *</label>
                        <input type="password" class="form-control" id="currentPassword" 
                               name="currentPassword" required>
                    </div>
                    
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label for="newPassword" class="form-label">New Password *</label>
                            <input type="password" class="form-control" id="newPassword" 
                                   name="newPassword" minlength="6" required>
                            <small class="text-muted">Min 6 characters, include letters and numbers</small>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label for="confirmPassword" class="form-label">Confirm New Password *</label>
                            <input type="password" class="form-control" id="confirmPassword" 
                                   name="confirmPassword" required>
                        </div>
                    </div>
                    
                    <button type="submit" class="btn btn-warning">
                        <i class="bi bi-shield-lock"></i> Change Password
                    </button>
                </form>
            </div>
        </div>
        
        <!-- Recent Jobs -->
        <div class="card">
            <div class="card-header d-flex justify-content-between align-items-center">
                <h5 class="mb-0"><i class="bi bi-clock-history"></i> Recent Conversions</h5>
                <a href="${pageContext.request.contextPath}/status" class="btn btn-sm btn-outline-primary">
                    View All
                </a>
            </div>
            <div class="card-body">
                <c:choose>
                    <c:when test="${empty recentJobs}">
                        <p class="text-muted text-center py-3">No recent conversions</p>
                    </c:when>
                    <c:otherwise>
                        <div class="list-group list-group-flush">
                            <c:forEach var="job" items="${recentJobs}">
                                <div class="list-group-item">
                                    <div class="d-flex justify-content-between align-items-center">
                                        <div>
                                            <strong>Job #${job.jobId}</strong> - 
                                            <span class="badge bg-secondary">${job.outputFormat}</span>
                                            <c:if test="${not empty job.outputResolution}">
                                                <small class="text-muted">${job.outputResolution}</small>
                                            </c:if>
                                            <br>
                                            <small class="text-muted">
                                                <fmt:formatDate value="${job.createdAt}" pattern="yyyy-MM-dd HH:mm"/>
                                            </small>
                                        </div>
                                        <div>
                                            <c:choose>
                                                <c:when test="${job.status == 'COMPLETED'}">
                                                    <span class="badge bg-success">
                                                        <i class="bi bi-check-circle"></i> Completed
                                                    </span>
                                                </c:when>
                                                <c:when test="${job.status == 'PROCESSING'}">
                                                    <span class="badge bg-info">
                                                        <i class="bi bi-gear-fill"></i> ${job.progress}%
                                                    </span>
                                                </c:when>
                                                <c:when test="${job.status == 'PENDING'}">
                                                    <span class="badge bg-warning">
                                                        <i class="bi bi-clock"></i> Pending
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-danger">
                                                        <i class="bi bi-x-circle"></i> Failed
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
        
        <!-- Storage Info -->
        <div class="card mt-4">
            <div class="card-body">
                <h6><i class="bi bi-hdd"></i> Storage Usage</h6>
                <p class="mb-0">
                    Total: <strong>${totalStorageFormatted}</strong>
                </p>
            </div>
        </div>
    </div>
</div>

<script>
    // Page-specific password validation
    const confirmPasswordField = document.getElementById('confirmPassword');
    if (confirmPasswordField) {
        confirmPasswordField.addEventListener('input', function() {
        const newPassword = document.getElementById('newPassword').value;
        const confirm = this.value;
        
        if (confirm && newPassword !== confirm) {
            this.setCustomValidity('Passwords do not match');
        } else {
            this.setCustomValidity('');
        }
        });
    }
</script>

<jsp:include page="includes/footer.jsp"/>
