package com.healthfirst.dto;

import com.healthfirst.entity.VerificationStatus;

import java.util.UUID;

public class ProviderRegistrationResponse {
    private UUID providerId;
    private String email;
    private VerificationStatus verificationStatus;
    
    // Default constructor
    public ProviderRegistrationResponse() {}
    
    // Constructor with all fields
    public ProviderRegistrationResponse(UUID providerId, String email, VerificationStatus verificationStatus) {
        this.providerId = providerId;
        this.email = email;
        this.verificationStatus = verificationStatus;
    }
    
    // Getters and Setters
    public UUID getProviderId() {
        return providerId;
    }
    
    public void setProviderId(UUID providerId) {
        this.providerId = providerId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }
    
    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }
    
    @Override
    public String toString() {
        return "ProviderRegistrationResponse{" +
                "providerId=" + providerId +
                ", email='" + email + '\'' +
                ", verificationStatus=" + verificationStatus +
                '}';
    }
} 