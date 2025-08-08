package com.healthfirst.service;

import com.healthfirst.dto.PatientLoginRequest;
import com.healthfirst.dto.PatientLoginResponse;
import com.healthfirst.dto.ProviderLoginRequest;
import com.healthfirst.dto.ProviderLoginResponse;
import com.healthfirst.entity.Patient;
import com.healthfirst.entity.Provider;
import com.healthfirst.entity.VerificationStatus;
import com.healthfirst.exception.AuthenticationException;
import com.healthfirst.repository.PatientRepository;
import com.healthfirst.repository.ProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    private final ProviderRepository providerRepository;
    private final PatientRepository patientRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    
    @Autowired
    public AuthService(ProviderRepository providerRepository, 
                      PatientRepository patientRepository,
                      PasswordService passwordService, 
                      JwtService jwtService) {
        this.providerRepository = providerRepository;
        this.patientRepository = patientRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
    }
    
    public ProviderLoginResponse authenticateProvider(ProviderLoginRequest loginRequest) {
        logger.info("Attempting authentication for provider: {}", loginRequest.getEmail());
        
        // Find provider by email
        Optional<Provider> providerOpt = providerRepository.findByEmail(loginRequest.getEmail());
        
        if (providerOpt.isEmpty()) {
            logger.warn("Authentication failed: Provider not found with email: {}", loginRequest.getEmail());
            throw new AuthenticationException("Invalid email or password");
        }
        
        Provider provider = providerOpt.get();
        
        // Check if provider is active
        if (!provider.getIsActive()) {
            logger.warn("Authentication failed: Provider account is inactive for email: {}", loginRequest.getEmail());
            throw new AuthenticationException("Account is inactive. Please contact support.");
        }
        
        // Check verification status
        if (provider.getVerificationStatus() != VerificationStatus.VERIFIED) {
            logger.warn("Authentication failed: Provider not verified for email: {}", loginRequest.getEmail());
            throw new AuthenticationException("Account not verified. Please wait for verification or contact support.");
        }
        
        // Verify password
        if (!passwordService.verifyPassword(loginRequest.getPassword(), provider.getPasswordHash())) {
            logger.warn("Authentication failed: Invalid password for email: {}", loginRequest.getEmail());
            throw new AuthenticationException("Invalid email or password");
        }
        
        // Generate JWT token
        String token = jwtService.generateToken(
            provider.getId().toString(),
            provider.getEmail(),
            "PROVIDER",
            provider.getSpecialization()
        );
        
        // Create response
        ProviderLoginResponse.ProviderData providerData = new ProviderLoginResponse.ProviderData(provider);
        ProviderLoginResponse response = new ProviderLoginResponse(
            token,
            jwtService.getTokenExpirationTime() / 1000, // Convert to seconds
            "Bearer",
            providerData
        );
        
        logger.info("Authentication successful for provider: {}", loginRequest.getEmail());
        return response;
    }
    
    public boolean validateProviderToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        // Remove "Bearer " prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        return jwtService.validateToken(token) && !jwtService.isTokenExpired(token);
    }
    
    public String getProviderIdFromToken(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        return jwtService.extractProviderId(token);
    }
    
    public String getEmailFromToken(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        return jwtService.extractEmail(token);
    }
    
    public PatientLoginResponse authenticatePatient(PatientLoginRequest loginRequest) {
        logger.info("Attempting authentication for patient: {}", loginRequest.getEmail());
        
        // Find patient by email
        Optional<Patient> patientOpt = patientRepository.findByEmail(loginRequest.getEmail());
        
        if (patientOpt.isEmpty()) {
            logger.warn("Authentication failed: Patient not found with email: {}", loginRequest.getEmail());
            throw new AuthenticationException("Invalid email or password");
        }
        
        Patient patient = patientOpt.get();
        
        // Check if patient is active
        if (!patient.getIsActive()) {
            logger.warn("Authentication failed: Patient account is inactive for email: {}", loginRequest.getEmail());
            throw new AuthenticationException("Account is inactive. Please contact support.");
        }
        
        // Check verification status
        if (patient.getVerificationStatus() != VerificationStatus.VERIFIED) {
            logger.warn("Authentication failed: Patient not verified for email: {}", loginRequest.getEmail());
            throw new AuthenticationException("Account not verified. Please wait for verification or contact support.");
        }
        
        // Verify password
        if (!passwordService.verifyPassword(loginRequest.getPassword(), patient.getPasswordHash())) {
            logger.warn("Authentication failed: Invalid password for email: {}", loginRequest.getEmail());
            throw new AuthenticationException("Invalid email or password");
        }
        
        // Generate JWT token for patient (30 minutes expiry)
        String token = jwtService.generatePatientToken(
            patient.getId().toString(),
            patient.getEmail(),
            "PATIENT"
        );
        
        // Create response
        PatientLoginResponse.PatientData patientData = new PatientLoginResponse.PatientData(patient);
        PatientLoginResponse response = new PatientLoginResponse(
            token,
            jwtService.getPatientTokenExpirationTime() / 1000, // Convert to seconds
            "Bearer",
            patientData
        );
        
        logger.info("Authentication successful for patient: {}", loginRequest.getEmail());
        return response;
    }
    
    public boolean validatePatientToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        // Remove "Bearer " prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        return jwtService.validateToken(token) && !jwtService.isTokenExpired(token);
    }
    
    public String getPatientIdFromToken(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        return jwtService.extractPatientId(token);
    }
} 