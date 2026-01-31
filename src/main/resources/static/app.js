// Application State
const state = {
    currentPage: 'landing-page',
    user: null,
    email: null,
    token: null
};

// API Base URL
const API_BASE = '/api';

// Initialize App
document.addEventListener('DOMContentLoaded', () => {
    // Check if user is already logged in
    const token = localStorage.getItem('authToken');
    if (token) {
        state.token = token;
        const userData = localStorage.getItem('userData');
        if (userData) {
            state.user = JSON.parse(userData);
            showDashboard();
        }
    }

    // Setup logout button
    document.getElementById('logout-btn').addEventListener('click', handleLogout);
});

// Page Navigation
function showPage(pageId) {
    // Hide all pages
    const pages = document.querySelectorAll('.page');
    pages.forEach(page => page.classList.remove('active'));

    // Show selected page
    const selectedPage = document.getElementById(pageId);
    if (selectedPage) {
        selectedPage.classList.add('active');
        state.currentPage = pageId;
    }

    // Update navbar visibility
    const navbar = document.getElementById('navbar');
    if (pageId === 'dashboard-page' || pageId === 'change-password-page') {
        navbar.classList.remove('hidden');
    } else {
        navbar.classList.add('hidden');
    }

    // Clear error messages when changing pages
    clearErrors();
}

// Clear all error messages
function clearErrors() {
    const errors = document.querySelectorAll('.alert-error, .alert-success');
    errors.forEach(error => {
        error.classList.add('hidden');
        error.textContent = '';
    });
}

// Show error message
function showError(elementId, message) {
    const errorElement = document.getElementById(elementId);
    if (errorElement) {
        errorElement.textContent = message;
        errorElement.classList.remove('hidden');
    }
}

// Show success message
function showSuccess(elementId, message) {
    const successElement = document.getElementById(elementId);
    if (successElement) {
        successElement.textContent = message;
        successElement.classList.remove('hidden');
    }
}

// Handle Registration
async function handleRegister(event) {
    event.preventDefault();
    clearErrors();

    const username = document.getElementById('reg-username').value;
    const email = document.getElementById('reg-email').value;
    const password = document.getElementById('reg-password').value;

    try {
        const response = await fetch(`${API_BASE}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                userName: username,
                email: email,
                password: password
            })
        });

        if (response.ok) {
            // Store email for OTP verification
            state.email = email;
            document.getElementById('otp-email-display').textContent = email;
            
            // Show OTP page
            showPage('otp-page');
        } else {
            const errorData = await response.json();
            showError('register-error', errorData.message || 'Registration failed. Please try again.');
        }
    } catch (error) {
        console.error('Registration error:', error);
        showError('register-error', 'Network error. Please check your connection.');
    }
}

// Handle OTP Verification
async function handleVerifyOtp(event) {
    event.preventDefault();
    clearErrors();

    const otp = document.getElementById('otp-code').value;

    if (!state.email) {
        showError('otp-error', 'Email not found. Please register again.');
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/auth/verify/otp`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                email: state.email,
                otp: otp
            })
        });

        if (response.ok) {
            const data = await response.json();
            
            // Show success message and redirect to login
            alert('Email verified successfully! Please login.');
            showPage('login-page');
            
            // Pre-fill email in login form
            const loginEmail = data.email;
            if (loginEmail) {
                // Note: Login uses username, so we'll clear the form
                document.getElementById('login-form').reset();
            }
        } else {
            const errorData = await response.json();
            showError('otp-error', errorData.message || 'Invalid OTP. Please try again.');
        }
    } catch (error) {
        console.error('OTP verification error:', error);
        showError('otp-error', 'Network error. Please check your connection.');
    }
}

// Handle Resend OTP
async function handleResendOtp() {
    if (!state.email) {
        showError('otp-error', 'Email not found. Please register again.');
        return;
    }

    clearErrors();

    try {
        const response = await fetch(`${API_BASE}/auth/otp`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                email: state.email,
                purpose: 'EMAIL_VERIFICATION'
            })
        });

        if (response.ok) {
            showSuccess('otp-error', 'OTP resent successfully! Check your email.');
        } else {
            const errorData = await response.json();
            showError('otp-error', errorData.message || 'Failed to resend OTP.');
        }
    } catch (error) {
        console.error('Resend OTP error:', error);
        showError('otp-error', 'Network error. Please check your connection.');
    }
}

// Handle Login
async function handleLogin(event) {
    event.preventDefault();
    clearErrors();

    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;

    // Create form data for Spring Security form login
    const formData = new URLSearchParams();
    formData.append('username', username);
    formData.append('password', password);

    try {
        const response = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: formData
        });

        if (response.ok) {
            const data = await response.json();
            
            // Store token and user data
            if (data.token) {
                state.token = data.token;
                localStorage.setItem('authToken', data.token);
            }
            
            state.user = {
                username: data.username || username,
                email: data.email || '',
                provider: data.provider || 'LOCAL'
            };
            localStorage.setItem('userData', JSON.stringify(state.user));
            
            // Show dashboard
            showDashboard();
        } else {
            const errorData = await response.json();
            showError('login-error', errorData.message || 'Invalid username or password.');
        }
    } catch (error) {
        console.error('Login error:', error);
        showError('login-error', 'Network error. Please check your connection.');
    }
}

// Show Dashboard
function showDashboard() {
    if (state.user) {
        document.getElementById('user-display').textContent = `Hello, ${state.user.username}`;
        document.getElementById('dashboard-username').textContent = state.user.username;
        document.getElementById('dashboard-email').textContent = state.user.email;
        document.getElementById('dashboard-provider').textContent = state.user.provider;
        showPage('dashboard-page');
    }
}

// Handle Logout
async function handleLogout() {
    try {
        const response = await fetch(`${API_BASE}/auth/logout`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${state.token}`
            }
        });

        // Clear local state regardless of response
        state.user = null;
        state.token = null;
        state.email = null;
        localStorage.removeItem('authToken');
        localStorage.removeItem('userData');
        
        // Redirect to landing page
        showPage('landing-page');
    } catch (error) {
        console.error('Logout error:', error);
        // Still clear local state on error
        state.user = null;
        state.token = null;
        localStorage.removeItem('authToken');
        localStorage.removeItem('userData');
        showPage('landing-page');
    }
}

// Handle Forgot Password
async function handleForgotPassword(event) {
    event.preventDefault();
    clearErrors();

    const email = document.getElementById('forgot-email').value;

    try {
        const response = await fetch(`${API_BASE}/auth/password/forgot`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                email: email
            })
        });

        if (response.ok) {
            showSuccess('forgot-success', 'Password reset code sent to your email!');
            
            // After 2 seconds, redirect to reset password page
            setTimeout(() => {
                document.getElementById('reset-email').value = email;
                showPage('reset-password-page');
            }, 2000);
        } else {
            const errorData = await response.json();
            showError('forgot-error', errorData.message || 'Failed to send reset code.');
        }
    } catch (error) {
        console.error('Forgot password error:', error);
        showError('forgot-error', 'Network error. Please check your connection.');
    }
}

// Handle Reset Password
async function handleResetPassword(event) {
    event.preventDefault();
    clearErrors();

    const email = document.getElementById('reset-email').value;
    const otp = document.getElementById('reset-otp').value;
    const newPassword = document.getElementById('reset-new-password').value;

    try {
        const response = await fetch(`${API_BASE}/auth/password/reset`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                email: email,
                otp: otp,
                newPassword: newPassword
            })
        });

        if (response.ok) {
            showSuccess('reset-success', 'Password reset successfully!');
            
            // After 2 seconds, redirect to login page
            setTimeout(() => {
                showPage('login-page');
            }, 2000);
        } else {
            const errorData = await response.json();
            showError('reset-error', errorData.message || 'Failed to reset password.');
        }
    } catch (error) {
        console.error('Reset password error:', error);
        showError('reset-error', 'Network error. Please check your connection.');
    }
}

// Handle Change Password
async function handleChangePassword(event) {
    event.preventDefault();
    clearErrors();

    const currentPassword = document.getElementById('current-password').value;
    const newPassword = document.getElementById('new-password').value;

    try {
        // First verify current password
        const verifyResponse = await fetch(`${API_BASE}/user/password/verify`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${state.token}`
            },
            body: JSON.stringify({
                password: currentPassword
            })
        });

        if (!verifyResponse.ok) {
            showError('change-password-error', 'Current password is incorrect.');
            return;
        }

        // Now change password
        const changeResponse = await fetch(`${API_BASE}/user/password/change`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${state.token}`
            },
            body: JSON.stringify({
                currentPassword: currentPassword,
                newPassword: newPassword
            })
        });

        if (changeResponse.ok) {
            showSuccess('change-password-success', 'Password changed successfully!');
            
            // Clear form
            document.getElementById('change-password-form').reset();
            
            // After 2 seconds, redirect to dashboard
            setTimeout(() => {
                showPage('dashboard-page');
            }, 2000);
        } else {
            const errorData = await changeResponse.json();
            showError('change-password-error', errorData.message || 'Failed to change password.');
        }
    } catch (error) {
        console.error('Change password error:', error);
        showError('change-password-error', 'Network error. Please check your connection.');
    }
}
