package com.healthfirst.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {
    
    private final BCryptPasswordEncoder passwordEncoder;
    
    public PasswordService(@Value("${security.bcrypt.salt-rounds:12}") int saltRounds) {
        this.passwordEncoder = new BCryptPasswordEncoder(saltRounds);
    }
    
    /**
     * Hash a password using BCrypt
     */
    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
    
    /**
     * Verify a password against its hash
     */
    public boolean verifyPassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }
    
    /**
     * Validate password strength
     */
    public boolean isValidPassword(String password) {
        // Check minimum length
        if (password == null || password.length() < 8) {
            return false;
        }
        
        // Check for at least one uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }
        
        // Check for at least one lowercase letter
        if (!password.matches(".*[a-z].*")) {
            return false;
        }
        
        // Check for at least one digit
        if (!password.matches(".*\\d.*")) {
            return false;
        }
        
        // Check for at least one special character
        if (!password.matches(".*[@$!%*?&].*")) {
            return false;
        }
        
        return true;
    }
} 