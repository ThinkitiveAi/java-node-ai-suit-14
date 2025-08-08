package com.healthfirst.dto;

import com.healthfirst.entity.AppointmentSlot;
import com.healthfirst.entity.ProviderAvailability;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class UpdateAvailabilityRequest {
    
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Start time must be in HH:mm format (24-hour)")
    private String startTime;
    
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "End time must be in HH:mm format (24-hour)")
    private String endTime;
    
    private AppointmentSlot.SlotStatus status;
    
    private String appointmentType;
    
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
    
    private PricingDto pricing;
    
    public static class PricingDto {
        private BigDecimal baseFee;
        private Boolean insuranceAccepted;
        private String currency;
        
        public PricingDto() {}
        
        public PricingDto(BigDecimal baseFee, Boolean insuranceAccepted, String currency) {
            this.baseFee = baseFee;
            this.insuranceAccepted = insuranceAccepted;
            this.currency = currency;
        }
        
        // Getters and Setters
        public BigDecimal getBaseFee() {
            return baseFee;
        }
        
        public void setBaseFee(BigDecimal baseFee) {
            this.baseFee = baseFee;
        }
        
        public Boolean getInsuranceAccepted() {
            return insuranceAccepted;
        }
        
        public void setInsuranceAccepted(Boolean insuranceAccepted) {
            this.insuranceAccepted = insuranceAccepted;
        }
        
        public String getCurrency() {
            return currency;
        }
        
        public void setCurrency(String currency) {
            this.currency = currency;
        }
    }
    
    // Default constructor
    public UpdateAvailabilityRequest() {}
    
    // Constructor with required fields
    public UpdateAvailabilityRequest(String startTime, String endTime, AppointmentSlot.SlotStatus status, 
                                  String appointmentType, String notes, PricingDto pricing) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.appointmentType = appointmentType;
        this.notes = notes;
        this.pricing = pricing;
    }
    
    // Getters and Setters
    public String getStartTime() {
        return startTime;
    }
    
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    
    public String getEndTime() {
        return endTime;
    }
    
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
    
    public AppointmentSlot.SlotStatus getStatus() {
        return status;
    }
    
    public void setStatus(AppointmentSlot.SlotStatus status) {
        this.status = status;
    }
    
    public String getAppointmentType() {
        return appointmentType;
    }
    
    public void setAppointmentType(String appointmentType) {
        this.appointmentType = appointmentType;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public PricingDto getPricing() {
        return pricing;
    }
    
    public void setPricing(PricingDto pricing) {
        this.pricing = pricing;
    }
} 