document.addEventListener('DOMContentLoaded', () => {
    // Elements
    const loginForm = document.getElementById('login-form');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const togglePasswordBtn = document.getElementById('toggle-password');
    const emailError = document.getElementById('email-error');
    const passwordError = document.getElementById('password-error');
    const loginButton = document.getElementById('login-button');
    
    // Form validation functions
    const validateEmail = () => {
        const emailValue = emailInput.value.trim();
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        
        if (!emailValue) {
            emailError.textContent = 'Email is required';
            emailInput.classList.add('error');
            return false;
        } else if (!emailRegex.test(emailValue)) {
            emailError.textContent = 'Please enter a valid email address';
            emailInput.classList.add('error');
            return false;
        } else {
            emailError.textContent = '';
            emailInput.classList.remove('error');
            return true;
        }
    };
    
    const validatePassword = () => {
        const passwordValue = passwordInput.value;
        
        if (!passwordValue) {
            passwordError.textContent = 'Password is required';
            passwordInput.classList.add('error');
            return false;
        } else if (passwordValue.length < 8) {
            passwordError.textContent = 'Password must be at least 8 characters';
            passwordInput.classList.add('error');
            return false;
        } else {
            passwordError.textContent = '';
            passwordInput.classList.remove('error');
            return true;
        }
    };
    
    // Toggle button state based on validation
    const updateButtonState = () => {
        const isEmailValid = validateEmail();
        const isPasswordValid = validatePassword();
        
        loginButton.disabled = !(isEmailValid && isPasswordValid);
    };
    
    // Toggle password visibility
    togglePasswordBtn.addEventListener('click', () => {
        const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
        passwordInput.setAttribute('type', type);
        togglePasswordBtn.textContent = type === 'password' ? 'visibility_off' : 'visibility';
    });
    
    // Input validation events
    emailInput.addEventListener('input', () => {
        validateEmail();
        updateButtonState();
    });
    
    passwordInput.addEventListener('input', () => {
        validatePassword();
        updateButtonState();
    });
    
    // Form submission
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        // Final validation before submission
        const isEmailValid = validateEmail();
        const isPasswordValid = validatePassword();
        
        if (isEmailValid && isPasswordValid) {
            try {
                // Show loading state
                loginButton.disabled = true;
                loginButton.textContent = 'Signing in...';
                
                const formData = {
                    email: emailInput.value.trim(),
                    password: passwordInput.value
                };
                
                // Submit the form data securely
                const response = await fetch('/api/v1/provider/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(formData),
                    credentials: 'same-origin' // Include cookies for session management
                });
                
                if (response.ok) {
                    // Redirect on successful login
                    window.location.href = '/dashboard';
                } else {
                    const errorData = await response.json();
                    // Display server-side error
                    const errorMessage = errorData.message || 'Invalid email or password';
                    passwordError.textContent = errorMessage;
                    loginButton.disabled = false;
                    loginButton.textContent = 'Sign In';
                }
            } catch (error) {
                console.error('Login error:', error);
                passwordError.textContent = 'An error occurred. Please try again.';
                loginButton.disabled = false;
                loginButton.textContent = 'Sign In';
            }
        }
    });
    
    // Initial button state
    updateButtonState();
    
    // Prevent autofill for sensitive fields
    emailInput.setAttribute('autocomplete', 'new-password');
    passwordInput.setAttribute('autocomplete', 'new-password');
});