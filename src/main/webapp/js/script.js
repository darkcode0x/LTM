/**
 * Video Converter - Main JavaScript File
 * Contains form validation, dynamic UI updates, and utility functions
 */

// ================================
// Global Variables
// ================================

const APP = {
    contextPath: '',
    autoRefreshInterval: null,
    autoRefreshDelay: 5000, // 5 seconds
};

// ================================
// Initialization
// ================================

document.addEventListener('DOMContentLoaded', function() {
    // Set context path
    const metaContextPath = document.querySelector('meta[name="context-path"]');
    if (metaContextPath) {
        APP.contextPath = metaContextPath.getAttribute('content');
    }
    
    // Initialize tooltips
    initTooltips();
    
    // Initialize form validations
    initFormValidations();
    
    // Initialize auto-dismiss alerts
    initAutoDismissAlerts();
    
    // Add animation to cards
    addCardAnimations();
    
    console.log('Video Converter App initialized');
});

// ================================
// Form Validation Functions
// ================================

function initFormValidations() {
    // Register Form
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        setupRegisterFormValidation(registerForm);
    }
    
    // Upload Form
    const uploadForm = document.getElementById('uploadForm');
    if (uploadForm) {
        setupUploadFormValidation(uploadForm);
    }
    
    // Profile Forms
    const profileUpdateForm = document.querySelector('form[action*="profile"][input[name="action"][value="updateProfile"]]');
    if (profileUpdateForm) {
        setupProfileUpdateValidation(profileUpdateForm);
    }
}

// Register Form Validation
function setupRegisterFormValidation(form) {
    const username = form.querySelector('#username');
    const email = form.querySelector('#email');
    const password = form.querySelector('#password');
    const confirmPassword = form.querySelector('#confirmPassword');
    const togglePassword = form.querySelector('#togglePassword');
    
    // Username validation
    if (username) {
        username.addEventListener('input', function() {
            validateUsername(this);
        });
    }
    
    // Email validation
    if (email) {
        email.addEventListener('input', function() {
            validateEmail(this);
        });
    }
    
    // Password strength indicator
    if (password) {
        password.addEventListener('input', function() {
            updatePasswordStrength(this);
        });
    }
    
    // Password match validation
    if (confirmPassword) {
        confirmPassword.addEventListener('input', function() {
            validatePasswordMatch(password, this);
        });
    }
    
    // Toggle password visibility
    if (togglePassword) {
        togglePassword.addEventListener('click', function() {
            togglePasswordVisibility(password, this);
        });
    }
    
    // Form submission
    form.addEventListener('submit', function(e) {
        if (!validateRegisterForm(this)) {
            e.preventDefault();
            showError('Please fix the errors before submitting');
        }
    });
}

// Upload Form Validation
function setupUploadFormValidation(form) {
    const videoFile = form.querySelector('#videoFile');
    const submitBtn = form.querySelector('#submitBtn');
    const fileInfo = document.getElementById('fileInfo');
    
    if (videoFile) {
        videoFile.addEventListener('change', function(e) {
            const file = e.target.files[0];
            
            if (file) {
                displayFileInfo(file, fileInfo);
                
                // Validate file size (500 MB max)
                if (file.size > 500 * 1024 * 1024) {
                    showError('File size exceeds 500 MB limit!');
                    this.value = '';
                    fileInfo.innerHTML = '';
                }
            }
        });
    }
    
    // Form submission with loading state
    form.addEventListener('submit', function(e) {
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Uploading...';
        }
    });
}

// Profile Update Validation
function setupProfileUpdateValidation(form) {
    const email = form.querySelector('#email');
    
    if (email) {
        email.addEventListener('input', function() {
            validateEmail(this);
        });
    }
}

// ================================
// Validation Helper Functions
// ================================

function validateUsername(input) {
    const username = input.value.trim();
    const pattern = /^[a-zA-Z0-9_]{3,50}$/;
    
    if (!pattern.test(username)) {
        setInvalid(input, 'Username must be 3-50 characters (letters, numbers, underscore only)');
        return false;
    } else {
        setValid(input);
        return true;
    }
}

function validateEmail(input) {
    const email = input.value.trim();
    const pattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    
    if (!pattern.test(email)) {
        setInvalid(input, 'Please enter a valid email address');
        return false;
    } else {
        setValid(input);
        return true;
    }
}

function updatePasswordStrength(input) {
    const password = input.value;
    const strengthBar = document.getElementById('passwordStrength');
    
    if (!strengthBar) return;
    
    let score = 0;
    
    // Length
    if (password.length >= 6) score++;
    if (password.length >= 10) score++;
    
    // Complexity
    if (/[a-z]/.test(password) && /[A-Z]/.test(password)) score++;
    if (/\d/.test(password)) score++;
    if (/[^a-zA-Z0-9]/.test(password)) score++;
    
    // Update visual indicator
    strengthBar.className = 'password-strength mt-2';
    
    if (score === 0) {
        strengthBar.classList.add('bg-secondary');
    } else if (score <= 2) {
        strengthBar.classList.add('bg-danger');
    } else if (score === 3) {
        strengthBar.classList.add('bg-warning');
    } else {
        strengthBar.classList.add('bg-success');
    }
}

function validatePasswordMatch(passwordInput, confirmInput) {
    const password = passwordInput.value;
    const confirm = confirmInput.value;
    const matchMsg = document.getElementById('passwordMatch');
    
    if (confirm && password !== confirm) {
        if (matchMsg) {
            matchMsg.classList.remove('d-none');
        }
        setInvalid(confirmInput, 'Passwords do not match');
        return false;
    } else {
        if (matchMsg) {
            matchMsg.classList.add('d-none');
        }
        setValid(confirmInput);
        return true;
    }
}

function validateRegisterForm(form) {
    const username = form.querySelector('#username');
    const email = form.querySelector('#email');
    const password = form.querySelector('#password');
    const confirmPassword = form.querySelector('#confirmPassword');
    
    let isValid = true;
    
    if (username && !validateUsername(username)) isValid = false;
    if (email && !validateEmail(email)) isValid = false;
    
    if (password && confirmPassword) {
        if (password.value !== confirmPassword.value) {
            showError('Passwords do not match!');
            isValid = false;
        }
        
        if (password.value.length < 6) {
            showError('Password must be at least 6 characters!');
            isValid = false;
        }
    }
    
    return isValid;
}

function setValid(input) {
    input.classList.remove('is-invalid');
    input.classList.add('is-valid');
    
    const feedback = input.nextElementSibling;
    if (feedback && feedback.classList.contains('invalid-feedback')) {
        feedback.style.display = 'none';
    }
}

function setInvalid(input, message) {
    input.classList.remove('is-valid');
    input.classList.add('is-invalid');
    
    let feedback = input.nextElementSibling;
    if (!feedback || !feedback.classList.contains('invalid-feedback')) {
        feedback = document.createElement('div');
        feedback.className = 'invalid-feedback';
        input.parentNode.insertBefore(feedback, input.nextSibling);
    }
    
    feedback.textContent = message;
    feedback.style.display = 'block';
}

// ================================
// UI Helper Functions
// ================================

function displayFileInfo(file, container) {
    if (!container) return;
    
    const size = (file.size / (1024 * 1024)).toFixed(2);
    const sizeClass = file.size > 500 * 1024 * 1024 ? 'alert-danger' : 'alert-info';
    const sizeText = file.size > 500 * 1024 * 1024 
        ? 'File size exceeds 500 MB limit!' 
        : `Size: ${size} MB`;
    
    container.innerHTML = `
        <div class="alert ${sizeClass} mb-0 fade-in">
            <strong>File:</strong> ${escapeHtml(file.name)}<br>
            <strong>${sizeText}</strong><br>
            <strong>Type:</strong> ${escapeHtml(file.type)}
        </div>
    `;
}

function togglePasswordVisibility(passwordInput, toggleButton) {
    const icon = toggleButton.querySelector('i');
    
    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        icon.classList.remove('bi-eye');
        icon.classList.add('bi-eye-slash');
    } else {
        passwordInput.type = 'password';
        icon.classList.remove('bi-eye-slash');
        icon.classList.add('bi-eye');
    }
}

function showError(message) {
    showNotification(message, 'danger');
}

function showSuccess(message) {
    showNotification(message, 'success');
}

function showNotification(message, type = 'info') {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show position-fixed top-0 start-50 translate-middle-x mt-3 slide-up`;
    alertDiv.style.zIndex = '9999';
    alertDiv.style.minWidth = '300px';
    alertDiv.innerHTML = `
        <i class="bi bi-${type === 'success' ? 'check-circle-fill' : 'exclamation-triangle-fill'} me-2"></i>
        ${escapeHtml(message)}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    document.body.appendChild(alertDiv);
    
    setTimeout(() => {
        alertDiv.classList.remove('show');
        setTimeout(() => alertDiv.remove(), 300);
    }, 5000);
}

// ================================
// Auto-Refresh for Status Page
// ================================

function initAutoRefresh() {
    const statusPage = document.querySelector('[data-page="status"]');
    
    if (statusPage) {
        const processingCount = parseInt(statusPage.dataset.processingCount || '0');
        
        if (processingCount > 0) {
            console.log(`Auto-refresh enabled: ${processingCount} processing job(s)`);
            
            APP.autoRefreshInterval = setTimeout(() => {
                console.log('Refreshing page...');
                location.reload();
            }, APP.autoRefreshDelay);
            
            // Show countdown
            showRefreshCountdown();
        } else {
            console.log('Auto-refresh disabled: No processing jobs');
        }
    }
}

function showRefreshCountdown() {
    let countdown = APP.autoRefreshDelay / 1000;
    const countdownElement = document.createElement('div');
    countdownElement.className = 'position-fixed bottom-0 end-0 m-3 alert alert-info fade-in';
    countdownElement.innerHTML = `
        <i class="bi bi-arrow-clockwise me-2"></i>
        Auto-refresh in <strong id="countdown">${countdown}</strong>s
    `;
    
    document.body.appendChild(countdownElement);
    
    const timer = setInterval(() => {
        countdown--;
        const countdownSpan = document.getElementById('countdown');
        if (countdownSpan) {
            countdownSpan.textContent = countdown;
        }
        
        if (countdown <= 0) {
            clearInterval(timer);
        }
    }, 1000);
}

// ================================
// Tooltip Initialization
// ================================

function initTooltips() {
    const tooltipTriggerList = [].slice.call(
        document.querySelectorAll('[data-bs-toggle="tooltip"]')
    );
    
    tooltipTriggerList.map(function(tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
}

// ================================
// Auto-Dismiss Alerts
// ================================

function initAutoDismissAlerts() {
    const alerts = document.querySelectorAll('.alert:not(.alert-permanent)');
    
    alerts.forEach(alert => {
        setTimeout(() => {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }, 5000);
    });
}

// ================================
// Card Animations
// ================================

function addCardAnimations() {
    const cards = document.querySelectorAll('.card');
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('fade-in');
            }
        });
    }, {
        threshold: 0.1
    });
    
    cards.forEach(card => {
        observer.observe(card);
    });
}

// ================================
// Utility Functions
// ================================

function escapeHtml(text) {
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    
    return text.replace(/[&<>"']/g, m => map[m]);
}

function formatBytes(bytes, decimals = 2) {
    if (bytes === 0) return '0 Bytes';
    
    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
}

function formatDuration(seconds) {
    const hrs = Math.floor(seconds / 3600);
    const mins = Math.floor((seconds % 3600) / 60);
    const secs = Math.floor(seconds % 60);
    
    return `${hrs.toString().padStart(2, '0')}:${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
}

// ================================
// Confirmation Dialogs
// ================================

function confirmDelete(message = 'Are you sure you want to delete this item?') {
    return confirm(message);
}

function confirmAction(message) {
    return confirm(message);
}

// ================================
// Loading State Management
// ================================

function showLoading(element, message = 'Loading...') {
    if (!element) return;
    
    element.disabled = true;
    element.dataset.originalHtml = element.innerHTML;
    element.innerHTML = `
        <span class="spinner-border spinner-border-sm me-2"></span>
        ${message}
    `;
}

function hideLoading(element) {
    if (!element) return;
    
    element.disabled = false;
    if (element.dataset.originalHtml) {
        element.innerHTML = element.dataset.originalHtml;
        delete element.dataset.originalHtml;
    }
}

// ================================
// AJAX Helper Functions
// ================================

async function makeRequest(url, options = {}) {
    try {
        const response = await fetch(url, {
            ...options,
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            }
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        return await response.json();
    } catch (error) {
        console.error('Request failed:', error);
        showError('Request failed. Please try again.');
        throw error;
    }
}

// ================================
// Export functions for global use
// ================================

window.VideoConverter = {
    showError,
    showSuccess,
    showNotification,
    confirmDelete,
    confirmAction,
    showLoading,
    hideLoading,
    formatBytes,
    formatDuration,
    makeRequest,
    validateEmail,
    validateUsername
};

console.log('Video Converter JavaScript loaded successfully');
