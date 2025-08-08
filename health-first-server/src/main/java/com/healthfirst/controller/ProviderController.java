package com.healthfirst.controller;

import com.healthfirst.dto.ApiResponse;
import com.healthfirst.dto.ProviderLoginRequest;
import com.healthfirst.dto.ProviderLoginResponse;
import com.healthfirst.dto.ProviderRegistrationRequest;
import com.healthfirst.dto.ProviderRegistrationResponse;
import com.healthfirst.service.AuthService;
import com.healthfirst.service.ProviderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/provider")
@CrossOrigin(origins = "*")
public class ProviderController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProviderController.class);
    
    private final ProviderService providerService;
    private final AuthService authService;
    
    @Autowired
    public ProviderController(ProviderService providerService, AuthService authService) {
        this.providerService = providerService;
        this.authService = authService;
    }
    
    /**
     * Register a new provider
     * POST /api/v1/provider/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<ProviderRegistrationResponse>> registerProvider(
            @Valid @RequestBody ProviderRegistrationRequest request) {
        
        logger.info("Received provider registration request for email: {}", request.getEmail());
        
        try {
            ProviderRegistrationResponse response = providerService.registerProvider(request);
            
            ApiResponse<ProviderRegistrationResponse> apiResponse = ApiResponse.success(
                "Provider registered successfully. Verification email sent.",
                response
            );
            
            logger.info("Provider registration successful for email: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Provider registration failed for email: {} - {}", request.getEmail(), e.getMessage());
            ApiResponse<ProviderRegistrationResponse> apiResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
            
        } catch (Exception e) {
            logger.error("Unexpected error during provider registration for email: {}", request.getEmail(), e);
            ApiResponse<ProviderRegistrationResponse> apiResponse = ApiResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }
    
    /**
     * Login provider
     * POST /api/v1/provider/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<ProviderLoginResponse>> loginProvider(
            @Valid @RequestBody ProviderLoginRequest request) {
        
        logger.info("Received provider login request for email: {}", request.getEmail());
        
        try {
            ProviderLoginResponse response = authService.authenticateProvider(request);
            
            ApiResponse<ProviderLoginResponse> apiResponse = ApiResponse.success(
                "Login successful",
                response
            );
            
            logger.info("Provider login successful for email: {}", request.getEmail());
            return ResponseEntity.ok(apiResponse);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Provider login failed for email: {} - {}", request.getEmail(), e.getMessage());
            ApiResponse<ProviderLoginResponse> apiResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
            
        } catch (Exception e) {
            logger.error("Unexpected error during provider login for email: {}", request.getEmail(), e);
            ApiResponse<ProviderLoginResponse> apiResponse = ApiResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }
    
    /**
     * Get provider by ID
     * GET /api/v1/provider/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> getProviderById(@PathVariable UUID id) {
        try {
            var provider = providerService.getProviderById(id);
            ApiResponse<Object> apiResponse = ApiResponse.success("Provider retrieved successfully", provider);
            return ResponseEntity.ok(apiResponse);
        } catch (IllegalArgumentException e) {
            ApiResponse<Object> apiResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
        } catch (Exception e) {
            logger.error("Error retrieving provider with ID: {}", id, e);
            ApiResponse<Object> apiResponse = ApiResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }
    
    /**
     * Get provider by email
     * GET /api/v1/provider/email/{email}
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<Object>> getProviderByEmail(@PathVariable String email) {
        try {
            var provider = providerService.getProviderByEmail(email);
            ApiResponse<Object> apiResponse = ApiResponse.success("Provider retrieved successfully", provider);
            return ResponseEntity.ok(apiResponse);
        } catch (IllegalArgumentException e) {
            ApiResponse<Object> apiResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
        } catch (Exception e) {
            logger.error("Error retrieving provider with email: {}", email, e);
            ApiResponse<Object> apiResponse = ApiResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }
    
    /**
     * Update provider verification status
     * PUT /api/v1/provider/{id}/verification-status
     */
    @PutMapping("/{id}/verification-status")
    public ResponseEntity<ApiResponse<String>> updateVerificationStatus(
            @PathVariable UUID id, 
            @RequestParam String status) {
        try {
            var verificationStatus = com.healthfirst.entity.VerificationStatus.valueOf(status.toUpperCase());
            providerService.updateVerificationStatus(id, verificationStatus);
            ApiResponse<String> apiResponse = ApiResponse.success("Verification status updated successfully");
            return ResponseEntity.ok(apiResponse);
        } catch (IllegalArgumentException e) {
            ApiResponse<String> apiResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        } catch (Exception e) {
            logger.error("Error updating verification status for provider ID: {}", id, e);
            ApiResponse<String> apiResponse = ApiResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }
    
    /**
     * Get providers by verification status
     * GET /api/v1/provider/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<Object>>> getProvidersByStatus(@PathVariable String status) {
        try {
            var verificationStatus = com.healthfirst.entity.VerificationStatus.valueOf(status.toUpperCase());
            var providers = providerService.getProvidersByVerificationStatus(verificationStatus);
            ApiResponse<List<Object>> apiResponse = ApiResponse.success("Providers retrieved successfully", (List<Object>) (List<?>) providers);
            return ResponseEntity.ok(apiResponse);
        } catch (IllegalArgumentException e) {
            ApiResponse<List<Object>> apiResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        } catch (Exception e) {
            logger.error("Error retrieving providers with status: {}", status, e);
            ApiResponse<List<Object>> apiResponse = ApiResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }
    
    /**
     * Get valid specializations
     * GET /api/v1/provider/specializations
     */
    @GetMapping("/specializations")
    public ResponseEntity<ApiResponse<List<String>>> getSpecializations() {
        try {
            List<String> specializations = providerService.getValidSpecializations();
            ApiResponse<List<String>> apiResponse = ApiResponse.success("Specializations retrieved successfully", specializations);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            logger.error("Error retrieving specializations", e);
            ApiResponse<List<String>> apiResponse = ApiResponse.error("Failed to retrieve specializations");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }
    
    /**
     * Health check endpoint
     * GET /api/v1/provider/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        ApiResponse<String> apiResponse = ApiResponse.success("Provider service is running");
        return ResponseEntity.ok(apiResponse);
    }
} 