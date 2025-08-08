package com.healthfirst.service;

import com.healthfirst.dto.ProviderLoginRequest;
import com.healthfirst.dto.ProviderLoginResponse;
import com.healthfirst.entity.ClinicAddress;
import com.healthfirst.entity.Provider;
import com.healthfirst.entity.VerificationStatus;
import com.healthfirst.exception.AuthenticationException;
import com.healthfirst.repository.ProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private ProviderRepository providerRepository;
    
    @Mock
    private PasswordService passwordService;
    
    @Mock
    private JwtService jwtService;
    
    @InjectMocks
    private AuthService authService;
    
    private Provider testProvider;
    private ProviderLoginRequest validLoginRequest;
    private ProviderLoginRequest invalidLoginRequest;
    
    @BeforeEach
    void setUp() {
        // Create test provider
        testProvider = new Provider();
        testProvider.setId(UUID.randomUUID());
        testProvider.setFirstName("John");
        testProvider.setLastName("Doe");
        testProvider.setEmail("john.doe@clinic.com");
        testProvider.setPhoneNumber("+1234567890");
        testProvider.setPasswordHash("hashedPassword123");
        testProvider.setSpecialization("Cardiology");
        testProvider.setLicenseNumber("MD123456789");
        testProvider.setYearsOfExperience(10);
        testProvider.setVerificationStatus(VerificationStatus.VERIFIED);
        testProvider.setIsActive(true);
        
        ClinicAddress address = new ClinicAddress();
        address.setStreet("123 Medical Center Dr");
        address.setCity("New York");
        address.setState("NY");
        address.setZip("10001");
        testProvider.setClinicAddress(address);
        
        // Create valid login request
        validLoginRequest = new ProviderLoginRequest();
        validLoginRequest.setEmail("john.doe@clinic.com");
        validLoginRequest.setPassword("SecurePassword123!");
        
        // Create invalid login request
        invalidLoginRequest = new ProviderLoginRequest();
        invalidLoginRequest.setEmail("nonexistent@clinic.com");
        invalidLoginRequest.setPassword("WrongPassword123!");
    }
    
    @Test
    void testAuthenticateProvider_Success() {
        // Arrange
        when(providerRepository.findByEmail("john.doe@clinic.com"))
                .thenReturn(Optional.of(testProvider));
        when(passwordService.verifyPassword("SecurePassword123!", "hashedPassword123"))
                .thenReturn(true);
        when(jwtService.generateToken(anyString(), anyString(), anyString(), anyString()))
                .thenReturn("jwt-token-here");
        when(jwtService.getTokenExpirationTime()).thenReturn(3600000L); // 1 hour in milliseconds
        
        // Act
        ProviderLoginResponse response = authService.authenticateProvider(validLoginRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals("jwt-token-here", response.getAccessToken());
        assertEquals(3600, response.getExpiresIn()); // 1 hour in seconds
        assertEquals("Bearer", response.getTokenType());
        assertNotNull(response.getProvider());
        assertEquals("john.doe@clinic.com", response.getProvider().getEmail());
        assertEquals("John", response.getProvider().getFirstName());
        assertEquals("Doe", response.getProvider().getLastName());
        assertEquals("Cardiology", response.getProvider().getSpecialization());
        assertEquals("VERIFIED", response.getProvider().getVerificationStatus());
        assertTrue(response.getProvider().isActive());
        
        // Verify interactions
        verify(providerRepository).findByEmail("john.doe@clinic.com");
        verify(passwordService).verifyPassword("SecurePassword123!", "hashedPassword123");
        verify(jwtService).generateToken(testProvider.getId().toString(), "john.doe@clinic.com", "PROVIDER", "Cardiology");
    }
    
    @Test
    void testAuthenticateProvider_ProviderNotFound() {
        // Arrange
        when(providerRepository.findByEmail("nonexistent@clinic.com"))
                .thenReturn(Optional.empty());
        
        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, 
                () -> authService.authenticateProvider(invalidLoginRequest));
        
        assertEquals("Invalid email or password", exception.getMessage());
        
        // Verify interactions
        verify(providerRepository).findByEmail("nonexistent@clinic.com");
        verify(passwordService, never()).verifyPassword(anyString(), anyString());
        verify(jwtService, never()).generateToken(anyString(), anyString(), anyString(), anyString());
    }
    
    @Test
    void testAuthenticateProvider_InvalidPassword() {
        // Arrange
        when(providerRepository.findByEmail("john.doe@clinic.com"))
                .thenReturn(Optional.of(testProvider));
        when(passwordService.verifyPassword("WrongPassword123!", "hashedPassword123"))
                .thenReturn(false);
        
        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, 
                () -> authService.authenticateProvider(invalidLoginRequest));
        
        assertEquals("Invalid email or password", exception.getMessage());
        
        // Verify interactions
        verify(providerRepository).findByEmail("john.doe@clinic.com");
        verify(passwordService).verifyPassword("WrongPassword123!", "hashedPassword123");
        verify(jwtService, never()).generateToken(anyString(), anyString(), anyString(), anyString());
    }
    
    @Test
    void testAuthenticateProvider_InactiveAccount() {
        // Arrange
        testProvider.setIsActive(false);
        when(providerRepository.findByEmail("john.doe@clinic.com"))
                .thenReturn(Optional.of(testProvider));
        
        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, 
                () -> authService.authenticateProvider(validLoginRequest));
        
        assertEquals("Account is inactive. Please contact support.", exception.getMessage());
        
        // Verify interactions
        verify(providerRepository).findByEmail("john.doe@clinic.com");
        verify(passwordService, never()).verifyPassword(anyString(), anyString());
        verify(jwtService, never()).generateToken(anyString(), anyString(), anyString(), anyString());
    }
    
    @Test
    void testAuthenticateProvider_NotVerified() {
        // Arrange
        testProvider.setVerificationStatus(VerificationStatus.PENDING);
        when(providerRepository.findByEmail("john.doe@clinic.com"))
                .thenReturn(Optional.of(testProvider));
        
        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, 
                () -> authService.authenticateProvider(validLoginRequest));
        
        assertEquals("Account not verified. Please wait for verification or contact support.", exception.getMessage());
        
        // Verify interactions
        verify(providerRepository).findByEmail("john.doe@clinic.com");
        verify(passwordService, never()).verifyPassword(anyString(), anyString());
        verify(jwtService, never()).generateToken(anyString(), anyString(), anyString(), anyString());
    }
    
    @Test
    void testValidateProviderToken_ValidToken() {
        // Arrange
        String validToken = "valid-jwt-token";
        when(jwtService.validateToken(validToken)).thenReturn(true);
        when(jwtService.isTokenExpired(validToken)).thenReturn(false);
        
        // Act
        boolean result = authService.validateProviderToken(validToken);
        
        // Assert
        assertTrue(result);
        verify(jwtService).validateToken(validToken);
        verify(jwtService).isTokenExpired(validToken);
    }
    
    @Test
    void testValidateProviderToken_InvalidToken() {
        // Arrange
        String invalidToken = "invalid-jwt-token";
        when(jwtService.validateToken(invalidToken)).thenReturn(false);
        
        // Act
        boolean result = authService.validateProviderToken(invalidToken);
        
        // Assert
        assertFalse(result);
        verify(jwtService).validateToken(invalidToken);
        verify(jwtService, never()).isTokenExpired(anyString());
    }
    
    @Test
    void testValidateProviderToken_ExpiredToken() {
        // Arrange
        String expiredToken = "expired-jwt-token";
        when(jwtService.validateToken(expiredToken)).thenReturn(true);
        when(jwtService.isTokenExpired(expiredToken)).thenReturn(true);
        
        // Act
        boolean result = authService.validateProviderToken(expiredToken);
        
        // Assert
        assertFalse(result);
        verify(jwtService).validateToken(expiredToken);
        verify(jwtService).isTokenExpired(expiredToken);
    }
    
    @Test
    void testValidateProviderToken_WithBearerPrefix() {
        // Arrange
        String tokenWithBearer = "Bearer valid-jwt-token";
        when(jwtService.validateToken("valid-jwt-token")).thenReturn(true);
        when(jwtService.isTokenExpired("valid-jwt-token")).thenReturn(false);
        
        // Act
        boolean result = authService.validateProviderToken(tokenWithBearer);
        
        // Assert
        assertTrue(result);
        verify(jwtService).validateToken("valid-jwt-token");
        verify(jwtService).isTokenExpired("valid-jwt-token");
    }
    
    @Test
    void testValidateProviderToken_NullToken() {
        // Act
        boolean result = authService.validateProviderToken(null);
        
        // Assert
        assertFalse(result);
        verify(jwtService, never()).validateToken(anyString());
        verify(jwtService, never()).isTokenExpired(anyString());
    }
    
    @Test
    void testValidateProviderToken_EmptyToken() {
        // Act
        boolean result = authService.validateProviderToken("");
        
        // Assert
        assertFalse(result);
        verify(jwtService, never()).validateToken(anyString());
        verify(jwtService, never()).isTokenExpired(anyString());
    }
} 