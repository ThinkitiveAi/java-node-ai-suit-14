package com.healthfirst.service;

import com.healthfirst.dto.PatientLoginRequest;
import com.healthfirst.dto.PatientLoginResponse;
import com.healthfirst.entity.Patient;
import com.healthfirst.entity.VerificationStatus;
import com.healthfirst.exception.AuthenticationException;
import com.healthfirst.repository.PatientRepository;
import com.healthfirst.repository.ProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientAuthServiceTest {
    
    @Mock
    private PatientRepository patientRepository;
    
    @Mock
    private PasswordService passwordService;
    
    @Mock
    private JwtService jwtService;
    
    @Mock
    private ProviderRepository providerRepository;
    
    @InjectMocks
    private AuthService authService;
    
    private Patient testPatient;
    private PatientLoginRequest validLoginRequest;
    private PatientLoginRequest invalidLoginRequest;
    
    @BeforeEach
    void setUp() {
        // Create test patient
        testPatient = new Patient();
        testPatient.setId(UUID.randomUUID());
        testPatient.setFirstName("Jane");
        testPatient.setLastName("Smith");
        testPatient.setEmail("jane.smith@email.com");
        testPatient.setPhoneNumber("+1234567890");
        testPatient.setPasswordHash("hashedPassword123");
        testPatient.setDateOfBirth(LocalDate.of(1990, 5, 15));
        testPatient.setGender("Female");
        testPatient.setBloodType("A+");
        testPatient.setSsn("123-45-6789");
        testPatient.setVerificationStatus(VerificationStatus.VERIFIED);
        testPatient.setIsActive(true);
        
        // Create valid login request
        validLoginRequest = new PatientLoginRequest();
        validLoginRequest.setEmail("jane.smith@email.com");
        validLoginRequest.setPassword("SecurePassword123!");
        
        // Create invalid login request
        invalidLoginRequest = new PatientLoginRequest();
        invalidLoginRequest.setEmail("nonexistent@email.com");
        invalidLoginRequest.setPassword("WrongPassword123!");
    }
    
    @Test
    void testAuthenticatePatient_Success() {
        // Arrange
        when(patientRepository.findByEmail("jane.smith@email.com"))
                .thenReturn(Optional.of(testPatient));
        when(passwordService.verifyPassword("SecurePassword123!", "hashedPassword123"))
                .thenReturn(true);
        when(jwtService.generatePatientToken(anyString(), anyString(), anyString()))
                .thenReturn("jwt-token-here");
        when(jwtService.getPatientTokenExpirationTime()).thenReturn(1800000L); // 30 minutes in milliseconds
        
        // Act
        PatientLoginResponse response = authService.authenticatePatient(validLoginRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals("jwt-token-here", response.getAccessToken());
        assertEquals(1800, response.getExpiresIn()); // 30 minutes in seconds
        assertEquals("Bearer", response.getTokenType());
        assertNotNull(response.getPatient());
        assertEquals("jane.smith@email.com", response.getPatient().getEmail());
        assertEquals("Jane", response.getPatient().getFirstName());
        assertEquals("Smith", response.getPatient().getLastName());
        assertEquals("Female", response.getPatient().getGender());
        assertEquals("A+", response.getPatient().getBloodType());
        assertEquals("VERIFIED", response.getPatient().getVerificationStatus());
        assertTrue(response.getPatient().isActive());
        
        // Verify interactions
        verify(patientRepository).findByEmail("jane.smith@email.com");
        verify(passwordService).verifyPassword("SecurePassword123!", "hashedPassword123");
        verify(jwtService).generatePatientToken(testPatient.getId().toString(), "jane.smith@email.com", "PATIENT");
    }
    
    @Test
    void testAuthenticatePatient_PatientNotFound() {
        // Arrange
        when(patientRepository.findByEmail("nonexistent@email.com"))
                .thenReturn(Optional.empty());
        
        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, 
                () -> authService.authenticatePatient(invalidLoginRequest));
        
        assertEquals("Invalid email or password", exception.getMessage());
        
        // Verify interactions
        verify(patientRepository).findByEmail("nonexistent@email.com");
        verify(passwordService, never()).verifyPassword(anyString(), anyString());
        verify(jwtService, never()).generatePatientToken(anyString(), anyString(), anyString());
    }
    
    @Test
    void testAuthenticatePatient_InvalidPassword() {
        // Arrange
        when(patientRepository.findByEmail("jane.smith@email.com"))
                .thenReturn(Optional.of(testPatient));
        when(passwordService.verifyPassword("WrongPassword123!", "hashedPassword123"))
                .thenReturn(false);
        
        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, 
                () -> authService.authenticatePatient(invalidLoginRequest));
        
        assertEquals("Invalid email or password", exception.getMessage());
        
        // Verify interactions
        verify(patientRepository).findByEmail("jane.smith@email.com");
        verify(passwordService).verifyPassword("WrongPassword123!", "hashedPassword123");
        verify(jwtService, never()).generatePatientToken(anyString(), anyString(), anyString());
    }
    
    @Test
    void testAuthenticatePatient_InactiveAccount() {
        // Arrange
        testPatient.setIsActive(false);
        when(patientRepository.findByEmail("jane.smith@email.com"))
                .thenReturn(Optional.of(testPatient));
        
        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, 
                () -> authService.authenticatePatient(validLoginRequest));
        
        assertEquals("Account is inactive. Please contact support.", exception.getMessage());
        
        // Verify interactions
        verify(patientRepository).findByEmail("jane.smith@email.com");
        verify(passwordService, never()).verifyPassword(anyString(), anyString());
        verify(jwtService, never()).generatePatientToken(anyString(), anyString(), anyString());
    }
    
    @Test
    void testAuthenticatePatient_NotVerified() {
        // Arrange
        testPatient.setVerificationStatus(VerificationStatus.PENDING);
        when(patientRepository.findByEmail("jane.smith@email.com"))
                .thenReturn(Optional.of(testPatient));
        
        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, 
                () -> authService.authenticatePatient(validLoginRequest));
        
        assertEquals("Account not verified. Please wait for verification or contact support.", exception.getMessage());
        
        // Verify interactions
        verify(patientRepository).findByEmail("jane.smith@email.com");
        verify(passwordService, never()).verifyPassword(anyString(), anyString());
        verify(jwtService, never()).generatePatientToken(anyString(), anyString(), anyString());
    }
    
    @Test
    void testValidatePatientToken_ValidToken() {
        // Arrange
        String validToken = "valid-jwt-token";
        when(jwtService.validateToken(validToken)).thenReturn(true);
        when(jwtService.isTokenExpired(validToken)).thenReturn(false);
        
        // Act
        boolean result = authService.validatePatientToken(validToken);
        
        // Assert
        assertTrue(result);
        verify(jwtService).validateToken(validToken);
        verify(jwtService).isTokenExpired(validToken);
    }
    
    @Test
    void testValidatePatientToken_InvalidToken() {
        // Arrange
        String invalidToken = "invalid-jwt-token";
        when(jwtService.validateToken(invalidToken)).thenReturn(false);
        
        // Act
        boolean result = authService.validatePatientToken(invalidToken);
        
        // Assert
        assertFalse(result);
        verify(jwtService).validateToken(invalidToken);
        verify(jwtService, never()).isTokenExpired(anyString());
    }
    
    @Test
    void testValidatePatientToken_ExpiredToken() {
        // Arrange
        String expiredToken = "expired-jwt-token";
        when(jwtService.validateToken(expiredToken)).thenReturn(true);
        when(jwtService.isTokenExpired(expiredToken)).thenReturn(true);
        
        // Act
        boolean result = authService.validatePatientToken(expiredToken);
        
        // Assert
        assertFalse(result);
        verify(jwtService).validateToken(expiredToken);
        verify(jwtService).isTokenExpired(expiredToken);
    }
    
    @Test
    void testValidatePatientToken_WithBearerPrefix() {
        // Arrange
        String tokenWithBearer = "Bearer valid-jwt-token";
        when(jwtService.validateToken("valid-jwt-token")).thenReturn(true);
        when(jwtService.isTokenExpired("valid-jwt-token")).thenReturn(false);
        
        // Act
        boolean result = authService.validatePatientToken(tokenWithBearer);
        
        // Assert
        assertTrue(result);
        verify(jwtService).validateToken("valid-jwt-token");
        verify(jwtService).isTokenExpired("valid-jwt-token");
    }
    
    @Test
    void testValidatePatientToken_NullToken() {
        // Act
        boolean result = authService.validatePatientToken(null);
        
        // Assert
        assertFalse(result);
        verify(jwtService, never()).validateToken(anyString());
        verify(jwtService, never()).isTokenExpired(anyString());
    }
    
    @Test
    void testValidatePatientToken_EmptyToken() {
        // Act
        boolean result = authService.validatePatientToken("");
        
        // Assert
        assertFalse(result);
        verify(jwtService, never()).validateToken(anyString());
        verify(jwtService, never()).isTokenExpired(anyString());
    }
    
    @Test
    void testGetPatientIdFromToken() {
        // Arrange
        String token = "valid-jwt-token";
        String expectedPatientId = "123e4567-e89b-12d3-a456-426614174000";
        when(jwtService.extractPatientId(token)).thenReturn(expectedPatientId);
        
        // Act
        String result = authService.getPatientIdFromToken(token);
        
        // Assert
        assertEquals(expectedPatientId, result);
        verify(jwtService).extractPatientId(token);
    }
    
    @Test
    void testGetPatientIdFromToken_WithBearerPrefix() {
        // Arrange
        String tokenWithBearer = "Bearer valid-jwt-token";
        String expectedPatientId = "123e4567-e89b-12d3-a456-426614174000";
        when(jwtService.extractPatientId("valid-jwt-token")).thenReturn(expectedPatientId);
        
        // Act
        String result = authService.getPatientIdFromToken(tokenWithBearer);
        
        // Assert
        assertEquals(expectedPatientId, result);
        verify(jwtService).extractPatientId("valid-jwt-token");
    }
} 