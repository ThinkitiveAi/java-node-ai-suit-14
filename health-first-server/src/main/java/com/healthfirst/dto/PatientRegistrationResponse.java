package com.healthfirst.dto;

import com.healthfirst.entity.VerificationStatus;

import java.util.UUID;

public class PatientRegistrationResponse {
    private UUID patientId;
    private String email;
    private VerificationStatus verificationStatus;
    
    // Default constructor
    public PatientRegistrationResponse() {}
    
    // Constructor with all fields
    public PatientRegistrationResponse(UUID patientId, String email, VerificationStatus verificationStatus) {
        this.patientId = patientId;
        this.email = email;
        this.verificationStatus = verificationStatus;
    }
    
    // Getters and Setters
    public UUID getPatientId() {
        return patientId;
    }
    
    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
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
        return "PatientRegistrationResponse{" +
                "patientId=" + patientId +
                ", email='" + email + '\'' +
                ", verificationStatus=" + verificationStatus +
                '}';
    }
} 