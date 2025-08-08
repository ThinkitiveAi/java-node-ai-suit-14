package com.healthfirst.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {
    
    private PasswordService passwordService;
    
    @BeforeEach
    void setUp() {
        passwordService = new PasswordService(12);
    }
    
    @Test
    void hashPassword_Success() {
        // Arrange
        String rawPassword = "SecurePassword123!";
        
        // Act
        String hashedPassword = passwordService.hashPassword(rawPassword);
        
        // Assert
        assertNotNull(hashedPassword);
        assertNotEquals(rawPassword, hashedPassword);
        assertTrue(hashedPassword.startsWith("$2a$12$"));
    }
    
    @Test
    void verifyPassword_Success() {
        // Arrange
        String rawPassword = "SecurePassword123!";
        String hashedPassword = passwordService.hashPassword(rawPassword);
        
        // Act
        boolean result = passwordService.verifyPassword(rawPassword, hashedPassword);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    void verifyPassword_Failure() {
        // Arrange
        String rawPassword = "SecurePassword123!";
        String wrongPassword = "WrongPassword123!";
        String hashedPassword = passwordService.hashPassword(rawPassword);
        
        // Act
        boolean result = passwordService.verifyPassword(wrongPassword, hashedPassword);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    void isValidPassword_ValidPassword() {
        // Arrange
        String validPassword = "SecurePassword123!";
        
        // Act
        boolean result = passwordService.isValidPassword(validPassword);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    void isValidPassword_TooShort() {
        // Arrange
        String shortPassword = "Short1!";
        
        // Act
        boolean result = passwordService.isValidPassword(shortPassword);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    void isValidPassword_NoUppercase() {
        // Arrange
        String password = "securepassword123!";
        
        // Act
        boolean result = passwordService.isValidPassword(password);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    void isValidPassword_NoLowercase() {
        // Arrange
        String password = "SECUREPASSWORD123!";
        
        // Act
        boolean result = passwordService.isValidPassword(password);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    void isValidPassword_NoDigit() {
        // Arrange
        String password = "SecurePassword!";
        
        // Act
        boolean result = passwordService.isValidPassword(password);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    void isValidPassword_NoSpecialCharacter() {
        // Arrange
        String password = "SecurePassword123";
        
        // Act
        boolean result = passwordService.isValidPassword(password);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    void isValidPassword_NullPassword() {
        // Act
        boolean result = passwordService.isValidPassword(null);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    void isValidPassword_EmptyPassword() {
        // Arrange
        String password = "";
        
        // Act
        boolean result = passwordService.isValidPassword(password);
        
        // Assert
        assertFalse(result);
    }
} 