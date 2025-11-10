<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register - Video Converter</title>
    <meta name="context-path" content="${pageContext.request.contextPath}">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
    <style>
        body {
            background: var(--gradient-primary);
            padding: 30px 0;
        }
        .register-container {
            max-width: 600px;
            width: 100%;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="register-container mx-auto">
            <div class="card">
                <div class="card-header text-center">
                    <h3 class="mb-0"><i class="bi bi-person-plus-fill"></i> Create Account</h3>
                    <p class="mb-0 mt-2">Join Video Converter today</p>
                </div>
                <div class="card-body p-4">
                    <!-- Error Message -->
                    <c:if test="${not empty error}">
                        <div class="alert alert-danger alert-dismissible fade show" role="alert">
                            <i class="bi bi-exclamation-triangle-fill me-2"></i>
                            ${error}
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                        </div>
                    </c:if>
                    
                    <!-- Registration Form -->
                    <form action="${pageContext.request.contextPath}/register" method="post" id="registerForm">
                        <div class="row">
                            <div class="col-md-6 mb-3">
                                <label for="username" class="form-label">Username *</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="bi bi-person"></i></span>
                                    <input type="text" class="form-control" id="username" name="username" 
                                           value="${username}" placeholder="Choose username" 
                                           pattern="[a-zA-Z0-9_]{3,50}" required>
                                </div>
                                <small class="text-muted">3-50 characters, letters, numbers, underscore only</small>
                            </div>
                            
                            <div class="col-md-6 mb-3">
                                <label for="email" class="form-label">Email *</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="bi bi-envelope"></i></span>
                                    <input type="email" class="form-control" id="email" name="email" 
                                           value="${email}" placeholder="your@email.com" required>
                                </div>
                            </div>
                        </div>
                        
                        <div class="mb-3">
                            <label for="fullName" class="form-label">Full Name *</label>
                            <div class="input-group">
                                <span class="input-group-text"><i class="bi bi-person-badge"></i></span>
                                <input type="text" class="form-control" id="fullName" name="fullName" 
                                       value="${fullName}" placeholder="John Doe" required>
                            </div>
                        </div>
                        
                        <div class="mb-3">
                            <label for="phone" class="form-label">Phone Number (Optional)</label>
                            <div class="input-group">
                                <span class="input-group-text"><i class="bi bi-telephone"></i></span>
                                <input type="tel" class="form-control" id="phone" name="phone" 
                                       value="${phone}" placeholder="+1234567890">
                            </div>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6 mb-3">
                                <label for="password" class="form-label">Password *</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="bi bi-lock"></i></span>
                                    <input type="password" class="form-control" id="password" name="password" 
                                           placeholder="Enter password" minlength="6" required>
                                    <button class="btn btn-outline-secondary" type="button" id="togglePassword">
                                        <i class="bi bi-eye"></i>
                                    </button>
                                </div>
                                <small class="text-muted">Min 6 characters, include letters and numbers</small>
                                <div class="password-strength mt-2 bg-secondary" id="passwordStrength"></div>
                            </div>
                            
                            <div class="col-md-6 mb-3">
                                <label for="confirmPassword" class="form-label">Confirm Password *</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="bi bi-lock-fill"></i></span>
                                    <input type="password" class="form-control" id="confirmPassword" 
                                           name="confirmPassword" placeholder="Confirm password" required>
                                </div>
                                <small class="text-danger d-none" id="passwordMatch">Passwords do not match</small>
                            </div>
                        </div>
                        
                        <div class="mb-3 form-check">
                            <input type="checkbox" class="form-check-input" id="terms" required>
                            <label class="form-check-label" for="terms">
                                I agree to the Terms and Conditions *
                            </label>
                        </div>
                        
                        <div class="d-grid">
                            <button type="submit" class="btn btn-primary btn-lg">
                                <i class="bi bi-person-check-fill me-2"></i>Create Account
                            </button>
                        </div>
                    </form>
                    
                    <hr class="my-4">
                    
                    <div class="text-center">
                        <p class="mb-0">Already have an account? 
                            <a href="${pageContext.request.contextPath}/login" class="text-decoration-none">
                                <strong>Sign In</strong>
                            </a>
                        </p>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/script.js"></script>
    <script>
        // Additional page-specific scripts
        // Toggle password visibility
        document.getElementById('togglePassword').addEventListener('click', function() {
            const password = document.getElementById('password');
            const icon = this.querySelector('i');
            
            if (password.type === 'password') {
                password.type = 'text';
                icon.classList.remove('bi-eye');
                icon.classList.add('bi-eye-slash');
            } else {
                password.type = 'password';
                icon.classList.remove('bi-eye-slash');
                icon.classList.add('bi-eye');
            }
        });
        
        // Password strength indicator
        document.getElementById('password').addEventListener('input', function() {
            const password = this.value;
            const strength = document.getElementById('passwordStrength');
            
            let score = 0;
            if (password.length >= 6) score++;
            if (password.length >= 10) score++;
            if (/[a-z]/.test(password) && /[A-Z]/.test(password)) score++;
            if (/\d/.test(password)) score++;
            if (/[^a-zA-Z0-9]/.test(password)) score++;
            
            strength.className = 'password-strength mt-2';
            if (score === 0) {
                strength.classList.add('bg-secondary');
            } else if (score <= 2) {
                strength.classList.add('bg-danger');
            } else if (score === 3) {
                strength.classList.add('bg-warning');
            } else {
                strength.classList.add('bg-success');
            }
        });
        
        // Check password match
        document.getElementById('confirmPassword').addEventListener('input', function() {
            const password = document.getElementById('password').value;
            const confirm = this.value;
            const matchMsg = document.getElementById('passwordMatch');
            
            if (confirm && password !== confirm) {
                matchMsg.classList.remove('d-none');
            } else {
                matchMsg.classList.add('d-none');
            }
        });
        
        // Form validation
        document.getElementById('registerForm').addEventListener('submit', function(e) {
            const password = document.getElementById('password').value;
            const confirm = document.getElementById('confirmPassword').value;
            
            if (password !== confirm) {
                e.preventDefault();
                alert('Passwords do not match!');
                return false;
            }
        });
    </script>
</body>
</html>
