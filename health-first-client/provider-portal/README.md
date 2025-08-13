# Health First Provider Portal

This is the frontend for the Health First Provider Portal, a secure platform for healthcare providers to access patient information and manage services.

## Login Features

- Secure provider login with email and password
- Form validation for proper email format and minimum password length
- Toggle password visibility for better user experience
- Secure HTTPS communication with backend API
- Mobile-responsive design

## Setup

1. Simply open the `index.html` file in your browser for development
2. For production, deploy the entire directory to your web server

## Security Features

- Prevents password autofill
- Securely submits credentials via HTTPS
- Client-side validation for improved user experience
- Server-side validation required for security

## API Endpoints

- Login: `/api/v1/provider/login` (POST)