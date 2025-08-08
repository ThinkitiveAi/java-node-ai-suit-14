package com.healthfirst.dto;

import com.healthfirst.entity.Provider;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProviderLoginResponse {
    
    private String accessToken;
    private long expiresIn;
    private String tokenType;
    private ProviderData provider;
    
    public ProviderLoginResponse() {}
    
    public ProviderLoginResponse(String accessToken, long expiresIn, String tokenType, ProviderData provider) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.tokenType = tokenType;
        this.provider = provider;
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
    
    public ProviderData getProvider() {
        return provider;
    }
    
    public void setProvider(ProviderData provider) {
        this.provider = provider;
    }
    
    // Inner class for provider data (excluding sensitive information)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProviderData {
        private String id;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private String specialization;
        private String licenseNumber;
        private Integer yearsOfExperience;
        private String verificationStatus;
        private boolean isActive;
        
        public ProviderData() {}
        
        public ProviderData(Provider provider) {
            this.id = provider.getId().toString();
            this.firstName = provider.getFirstName();
            this.lastName = provider.getLastName();
            this.email = provider.getEmail();
            this.phoneNumber = provider.getPhoneNumber();
            this.specialization = provider.getSpecialization();
            this.licenseNumber = provider.getLicenseNumber();
            this.yearsOfExperience = provider.getYearsOfExperience();
            this.verificationStatus = provider.getVerificationStatus().name();
            this.isActive = provider.getIsActive();
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
        
        public String getSpecialization() {
            return specialization;
        }
        
        public void setSpecialization(String specialization) {
            this.specialization = specialization;
        }
        
        public String getLicenseNumber() {
            return licenseNumber;
        }
        
        public void setLicenseNumber(String licenseNumber) {
            this.licenseNumber = licenseNumber;
        }
        
        public Integer getYearsOfExperience() {
            return yearsOfExperience;
        }
        
        public void setYearsOfExperience(Integer yearsOfExperience) {
            this.yearsOfExperience = yearsOfExperience;
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