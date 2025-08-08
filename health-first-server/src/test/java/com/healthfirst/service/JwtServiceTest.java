package com.healthfirst.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
    
    @InjectMocks
    private JwtService jwtService;
    
    private static final String TEST_SECRET = "test-secret-key-that-is-long-enough-for-hmac-sha256";
    private static final long TEST_EXPIRATION = 3600000L; // 1 hour in milliseconds
    
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", TEST_EXPIRATION);
    }
    
    @Test
    void testGenerateToken_Success() {
        // Arrange
        String providerId = "123e4567-e89b-12d3-a456-426614174000";
        String email = "john.doe@clinic.com";
        String role = "PROVIDER";
        String specialization = "Cardiology";
        
        // Act
        String token = jwtService.generateToken(providerId, email, role, specialization);
        
        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        // Verify token can be parsed and contains expected claims
        String extractedEmail = jwtService.extractEmail(token);
        String extractedProviderId = jwtService.extractProviderId(token);
        String extractedRole = jwtService.extractRole(token);
        String extractedSpecialization = jwtService.extractSpecialization(token);
        
        assertEquals(email, extractedEmail);
        assertEquals(providerId, extractedProviderId);
        assertEquals(role, extractedRole);
        assertEquals(specialization, extractedSpecialization);
    }
    
    @Test
    void testValidateToken_ValidToken() {
        // Arrange
        String token = jwtService.generateToken("123", "test@example.com", "PROVIDER", "Cardiology");
        
        // Act
        boolean isValid = jwtService.validateToken(token);
        
        // Assert
        assertTrue(isValid);
    }
    
    @Test
    void testValidateToken_InvalidToken() {
        // Arrange
        String invalidToken = "invalid.jwt.token";
        
        // Act
        boolean isValid = jwtService.validateToken(invalidToken);
        
        // Assert
        assertFalse(isValid);
    }
    
    @Test
    void testValidateToken_NullToken() {
        // Act
        boolean isValid = jwtService.validateToken(null);
        
        // Assert
        assertFalse(isValid);
    }
    
    @Test
    void testValidateToken_EmptyToken() {
        // Act
        boolean isValid = jwtService.validateToken("");
        
        // Assert
        assertFalse(isValid);
    }
    
    @Test
    void testIsTokenExpired_ValidToken() {
        // Arrange
        String token = jwtService.generateToken("123", "test@example.com", "PROVIDER", "Cardiology");
        
        // Act
        boolean isExpired = jwtService.isTokenExpired(token);
        
        // Assert
        assertFalse(isExpired);
    }
    
    @Test
    void testIsTokenExpired_ExpiredToken() {
        // Arrange - Create a token with very short expiration
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1L); // 1 millisecond
        String token = jwtService.generateToken("123", "test@example.com", "PROVIDER", "Cardiology");
        
        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Act
        boolean isExpired = jwtService.isTokenExpired(token);
        
        // Assert
        assertTrue(isExpired);
    }
    
    @Test
    void testExtractEmail_Success() {
        // Arrange
        String expectedEmail = "john.doe@clinic.com";
        String token = jwtService.generateToken("123", expectedEmail, "PROVIDER", "Cardiology");
        
        // Act
        String extractedEmail = jwtService.extractEmail(token);
        
        // Assert
        assertEquals(expectedEmail, extractedEmail);
    }
    
    @Test
    void testExtractProviderId_Success() {
        // Arrange
        String expectedProviderId = "123e4567-e89b-12d3-a456-426614174000";
        String token = jwtService.generateToken(expectedProviderId, "test@example.com", "PROVIDER", "Cardiology");
        
        // Act
        String extractedProviderId = jwtService.extractProviderId(token);
        
        // Assert
        assertEquals(expectedProviderId, extractedProviderId);
    }
    
    @Test
    void testExtractRole_Success() {
        // Arrange
        String expectedRole = "PROVIDER";
        String token = jwtService.generateToken("123", "test@example.com", expectedRole, "Cardiology");
        
        // Act
        String extractedRole = jwtService.extractRole(token);
        
        // Assert
        assertEquals(expectedRole, extractedRole);
    }
    
    @Test
    void testExtractSpecialization_Success() {
        // Arrange
        String expectedSpecialization = "Cardiology";
        String token = jwtService.generateToken("123", "test@example.com", "PROVIDER", expectedSpecialization);
        
        // Act
        String extractedSpecialization = jwtService.extractSpecialization(token);
        
        // Assert
        assertEquals(expectedSpecialization, extractedSpecialization);
    }
    
    @Test
    void testExtractExpiration_Success() {
        // Arrange
        String token = jwtService.generateToken("123", "test@example.com", "PROVIDER", "Cardiology");
        
        // Act
        Date expiration = jwtService.extractExpiration(token);
        
        // Assert
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }
    
    @Test
    void testGetTokenExpirationTime() {
        // Act
        long expirationTime = jwtService.getTokenExpirationTime();
        
        // Assert
        assertEquals(TEST_EXPIRATION, expirationTime);
    }
    
    @Test
    void testExtractClaim_WithCustomFunction() {
        // Arrange
        String token = jwtService.generateToken("123", "test@example.com", "PROVIDER", "Cardiology");
        
        // Act
        String subject = jwtService.extractClaim(token, Claims::getSubject);
        
        // Assert
        assertEquals("test@example.com", subject);
    }
    
    @Test
    void testExtractAllClaims_InvalidToken() {
        // Arrange
        String invalidToken = "invalid.jwt.token";
        
        // Act & Assert
        assertThrows(JwtException.class, () -> jwtService.extractEmail(invalidToken));
    }
} 