package com.healthfirst.dto;

import com.healthfirst.entity.ProviderAvailability;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class CreateAvailabilityRequest {
    
    @NotBlank(message = "Date is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date must be in YYYY-MM-DD format")
    private String date;
    
    @NotBlank(message = "Start time is required")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Start time must be in HH:mm format (24-hour)")
    private String startTime;
    
    @NotBlank(message = "End time is required")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "End time must be in HH:mm format (24-hour)")
    private String endTime;
    
    @NotBlank(message = "Timezone is required")
    private String timezone;
    
    private Boolean isRecurring = false;
    
    private ProviderAvailability.RecurrencePattern recurrencePattern;
    
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Recurrence end date must be in YYYY-MM-DD format")
    private String recurrenceEndDate;
    
    @Min(value = 15, message = "Slot duration must be at least 15 minutes")
    @Max(value = 480, message = "Slot duration cannot exceed 8 hours")
    private Integer slotDuration = 30;
    
    @Min(value = 0, message = "Break duration cannot be negative")
    @Max(value = 120, message = "Break duration cannot exceed 2 hours")
    private Integer breakDuration = 0;
    
    @Min(value = 1, message = "Max appointments per slot must be at least 1")
    @Max(value = 10, message = "Max appointments per slot cannot exceed 10")
    private Integer maxAppointmentsPerSlot = 1;
    
    private ProviderAvailability.AppointmentType appointmentType = ProviderAvailability.AppointmentType.CONSULTATION;
    
    @Valid
    private LocationDto location;
    
    @Valid
    private PricingDto pricing;
    
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
    
    private List<String> specialRequirements;
    
    // Embedded DTOs
    public static class LocationDto {
        private ProviderAvailability.LocationType type;
        private String address;
        private String roomNumber;
        
        // Constructors
        public LocationDto() {}
        
        public LocationDto(ProviderAvailability.LocationType type, String address, String roomNumber) {
            this.type = type;
            this.address = address;
            this.roomNumber = roomNumber;
        }
        
        // Getters and Setters
        public ProviderAvailability.LocationType getType() {
            return type;
        }
        
        public void setType(ProviderAvailability.LocationType type) {
            this.type = type;
        }
        
        public String getAddress() {
            return address;
        }
        
        public void setAddress(String address) {
            this.address = address;
        }
        
        public String getRoomNumber() {
            return roomNumber;
        }
        
        public void setRoomNumber(String roomNumber) {
            this.roomNumber = roomNumber;
        }
    }
    
    public static class PricingDto {
        @DecimalMin(value = "0.0", message = "Base fee cannot be negative")
        private BigDecimal baseFee;
        
        private Boolean insuranceAccepted = false;
        
        @Size(min = 3, max = 3, message = "Currency must be 3 characters")
        private String currency = "USD";
        
        // Constructors
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
    public CreateAvailabilityRequest() {}
    
    // Constructor with required fields
    public CreateAvailabilityRequest(String date, String startTime, String endTime, String timezone) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.timezone = timezone;
    }
    
    // Getters and Setters
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
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
    
    public String getTimezone() {
        return timezone;
    }
    
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
    
    public Boolean getIsRecurring() {
        return isRecurring;
    }
    
    public void setIsRecurring(Boolean isRecurring) {
        this.isRecurring = isRecurring;
    }
    
    public ProviderAvailability.RecurrencePattern getRecurrencePattern() {
        return recurrencePattern;
    }
    
    public void setRecurrencePattern(ProviderAvailability.RecurrencePattern recurrencePattern) {
        this.recurrencePattern = recurrencePattern;
    }
    
    public String getRecurrenceEndDate() {
        return recurrenceEndDate;
    }
    
    public void setRecurrenceEndDate(String recurrenceEndDate) {
        this.recurrenceEndDate = recurrenceEndDate;
    }
    
    public Integer getSlotDuration() {
        return slotDuration;
    }
    
    public void setSlotDuration(Integer slotDuration) {
        this.slotDuration = slotDuration;
    }
    
    public Integer getBreakDuration() {
        return breakDuration;
    }
    
    public void setBreakDuration(Integer breakDuration) {
        this.breakDuration = breakDuration;
    }
    
    public Integer getMaxAppointmentsPerSlot() {
        return maxAppointmentsPerSlot;
    }
    
    public void setMaxAppointmentsPerSlot(Integer maxAppointmentsPerSlot) {
        this.maxAppointmentsPerSlot = maxAppointmentsPerSlot;
    }
    
    public ProviderAvailability.AppointmentType getAppointmentType() {
        return appointmentType;
    }
    
    public void setAppointmentType(ProviderAvailability.AppointmentType appointmentType) {
        this.appointmentType = appointmentType;
    }
    
    public LocationDto getLocation() {
        return location;
    }
    
    public void setLocation(LocationDto location) {
        this.location = location;
    }
    
    public PricingDto getPricing() {
        return pricing;
    }
    
    public void setPricing(PricingDto pricing) {
        this.pricing = pricing;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public List<String> getSpecialRequirements() {
        return specialRequirements;
    }
    
    public void setSpecialRequirements(List<String> specialRequirements) {
        this.specialRequirements = specialRequirements;
    }
    
    @Override
    public String toString() {
        return "CreateAvailabilityRequest{" +
                "date='" + date + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", timezone='" + timezone + '\'' +
                ", isRecurring=" + isRecurring +
                ", slotDuration=" + slotDuration +
                ", appointmentType=" + appointmentType +
                '}';
    }
} 