/**
 * Authentication utility functions for Health First Provider Portal
 */

const API_BASE_URL = 'http://localhost:8080';

// Utility function to check if the user is authenticated
function isAuthenticated() {
  return localStorage.getItem('token') !== null;
}

// Function to handle user logout
function logout() {
  localStorage.removeItem('token');
  localStorage.removeItem('provider');
  window.location.href = 'index.html';
}

// Function to get the stored provider data
function getProvider() {
  const providerData = localStorage.getItem('provider');
  return providerData ? JSON.parse(providerData) : null;
}

// Function to get the authorization headers for API requests
function getAuthHeaders() {
  const token = localStorage.getItem('token');
  return {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  };
}

// Function to make authenticated API requests
async function fetchWithAuth(endpoint, options = {}) {
  if (!isAuthenticated()) {
    throw new Error('User is not authenticated');
  }

  const url = `${API_BASE_URL}${endpoint}`;
  
  const requestOptions = {
    ...options,
    headers: {
      ...getAuthHeaders(),
      ...(options.headers || {})
    }
  };

  try {
    const response = await fetch(url, requestOptions);
    
    // If token is invalid or expired, redirect to login
    if (response.status === 401 || response.status === 403) {
      logout();
      return;
    }
    
    return response;
  } catch (error) {
    console.error('API request error:', error);
    throw error;
  }
}

// Check authentication on page load
document.addEventListener('DOMContentLoaded', () => {
  // If on login page and already authenticated, redirect to dashboard
  if (window.location.pathname.endsWith('index.html') && isAuthenticated()) {
    window.location.href = 'dashboard.html';
    return;
  }
  
  // If on any other page and not authenticated, redirect to login
  if (!window.location.pathname.endsWith('index.html') && !isAuthenticated()) {
    window.location.href = 'index.html';
    return;
  }
  
  // If we have a logout button, attach the event handler
  const logoutButton = document.getElementById('logout-button');
  if (logoutButton) {
    logoutButton.addEventListener('click', logout);
  }
  
  // If we're on the dashboard, populate provider name
  const providerNameElement = document.getElementById('provider-name');
  if (providerNameElement) {
    const provider = getProvider();
    if (provider) {
      providerNameElement.textContent = provider.name;
    }
  }
});