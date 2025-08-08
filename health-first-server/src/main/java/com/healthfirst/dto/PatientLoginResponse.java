package com.healthfirst.dto;

import com.healthfirst.entity.Patient;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatientLoginResponse {
    
    private String accessToken;
    private long expiresIn;
    private String tokenType;
    private PatientData patient;
    
    public PatientLoginResponse() {}
    
    public PatientLoginResponse(String accessToken, long expiresIn, String tokenType, PatientData patient) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.tokenType = tokenType;
        this.patient = patient;
    }
    
    // Getters and Setters
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public long getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    public PatientData getPatient() {
        return patient;
    }
    
    public void setPatient(PatientData patient) {
        this.patient = patient;
    }
    
    // Inner class for patient data (excluding sensitive information)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PatientData {
        private String id;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private String dateOfBirth;
        private String gender;
        private String bloodType;
        private String ssn;
        private String verificationStatus;
        private boolean isActive;
        
        public PatientData() {}
        
        public PatientData(Patient patient) {
            this.id = patient.getId().toString();
            this.firstName = patient.getFirstName();
            this.lastName = patient.getLastName();
            this.email = patient.getEmail();
            this.phoneNumber = patient.getPhoneNumber();
            this.dateOfBirth = patient.getDateOfBirth() != null ? patient.getDateOfBirth().toString() : null;
            this.gender = patient.getGender();
            this.bloodType = patient.getBloodType();
            this.ssn = patient.getSsn();
            this.verificationStatus = patient.getVerificationStatus().name();
            this.isActive = patient.getIsActive();
        }
        
        // Getters and Setters
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getFirstName() {
            return firstName;
        }
        
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
        
        public String getLastName() {
            return lastName;
        }
        
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getPhoneNumber() {
            return phoneNumber;
        }
        
        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
        
        public String getDateOfBirth() {
            return dateOfBirth;
        }
        
        public void setDateOfBirth(String dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
        }
        
        public String getGender() {
            return gender;
        }
        
        public void setGender(String gender) {
            this.gender = gender;
        }
        
        public String getBloodType() {
            return bloodType;
        }
        
        public void setBloodType(String bloodType) {
            this.bloodType = bloodType;
        }
        
        public String getSsn() {
            return ssn;
        }
        
        public void setSsn(String ssn) {
            this.ssn = ssn;
        }
        
        public String getVerificationStatus() {
            return verificationStatus;
        }
        
        public void setVerificationStatus(String verificationStatus) {
            this.verificationStatus = verificationStatus;
        }
        
        public boolean isActive() {
            return isActive;
        }
        
        public void setActive(boolean active) {
            isActive = active;
        }
    }
} 