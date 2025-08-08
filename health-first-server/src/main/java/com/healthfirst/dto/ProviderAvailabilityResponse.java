package com.healthfirst.dto;

import com.healthfirst.entity.AppointmentSlot;
import com.healthfirst.entity.ProviderAvailability;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProviderAvailabilityResponse {
    
    private String providerId;
    private AvailabilitySummaryDto availabilitySummary;
    private List<DailyAvailabilityDto> availability;
    
    // Embedded DTOs
    public static class AvailabilitySummaryDto {
        private int totalSlots;
        private int availableSlots;
        private int bookedSlots;
        private int cancelledSlots;
        
        public AvailabilitySummaryDto() {}
        
        public AvailabilitySummaryDto(int totalSlots, int availableSlots, int bookedSlots, int cancelledSlots) {
            this.totalSlots = totalSlots;
            this.availableSlots = availableSlots;
            this.bookedSlots = bookedSlots;
            this.cancelledSlots = cancelledSlots;
        }
        
        // Getters and Setters
        public int getTotalSlots() {
            return totalSlots;
        }
        
        public void setTotalSlots(int totalSlots) {
            this.totalSlots = totalSlots;
        }
        
        public int getAvailableSlots() {
            return availableSlots;
        }
        
        public void setAvailableSlots(int availableSlots) {
            this.availableSlots = availableSlots;
        }
        
        public int getBookedSlots() {
            return bookedSlots;
        }
        
        public void setBookedSlots(int bookedSlots) {
            this.bookedSlots = bookedSlots;
        }
        
        public int getCancelledSlots() {
            return cancelledSlots;
        }
        
        public void setCancelledSlots(int cancelledSlots) {
            this.cancelledSlots = cancelledSlots;
        }
    }
    
    public static class DailyAvailabilityDto {
        private LocalDate date;
        private List<SlotDto> slots;
        
        public DailyAvailabilityDto() {}
        
        public DailyAvailabilityDto(LocalDate date, List<SlotDto> slots) {
            this.date = date;
            this.slots = slots;
        }
        
        // Getters and Setters
        public LocalDate getDate() {
            return date;
        }
        
        public void setDate(LocalDate date) {
            this.date = date;
        }
        
        public List<SlotDto> getSlots() {
            return slots;
        }
        
        public void setSlots(List<SlotDto> slots) {
            this.slots = slots;
        }
    }
    
    public static class SlotDto {
        private String slotId;
        private LocalTime startTime;
        private LocalTime endTime;
        private String status;
        private String appointmentType;
        private LocationDto location;
        private PricingDto pricing;
        
        public SlotDto() {}
        
        public SlotDto(String slotId, LocalTime startTime, LocalTime endTime, String status, 
                      String appointmentType, LocationDto location, PricingDto pricing) {
            this.slotId = slotId;
            this.startTime = startTime;
            this.endTime = endTime;
            this.status = status;
            this.appointmentType = appointmentType;
            this.location = location;
            this.pricing = pricing;
        }
        
        // Getters and Setters
        public String getSlotId() {
            return slotId;
        }
        
        public void setSlotId(String slotId) {
            this.slotId = slotId;
        }
        
        public LocalTime getStartTime() {
            return startTime;
        }
        
        public void setStartTime(LocalTime startTime) {
            this.startTime = startTime;
        }
        
        public LocalTime getEndTime() {
            return endTime;
        }
        
        public void setEndTime(LocalTime endTime) {
            this.endTime = endTime;
        }
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        public String getAppointmentType() {
            return appointmentType;
        }
        
        public void setAppointmentType(String appointmentType) {
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
    }
    
    public static class LocationDto {
        private String type;
        private String address;
        private String roomNumber;
        
        public LocationDto() {}
        
        public LocationDto(String type, String address, String roomNumber) {
            this.type = type;
            this.address = address;
            this.roomNumber = roomNumber;
        }
        
        // Getters and Setters
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
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
        private BigDecimal baseFee;
        private Boolean insuranceAccepted;
        
        public PricingDto() {}
        
        public PricingDto(BigDecimal baseFee, Boolean insuranceAccepted) {
            this.baseFee = baseFee;
            this.insuranceAccepted = insuranceAccepted;
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
    }
    
    // Default constructor
    public ProviderAvailabilityResponse() {}
    
    // Constructor with required fields
    public ProviderAvailabilityResponse(String providerId, AvailabilitySummaryDto availabilitySummary, 
                                     List<DailyAvailabilityDto> availability) {
        this.providerId = providerId;
        this.availabilitySummary = availabilitySummary;
        this.availability = availability;
    }
    
    // Getters and Setters
    public String getProviderId() {
        return providerId;
    }
    
    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }
    
    public AvailabilitySummaryDto getAvailabilitySummary() {
        return availabilitySummary;
    }
    
    public void setAvailabilitySummary(AvailabilitySummaryDto availabilitySummary) {
        this.availabilitySummary = availabilitySummary;
    }
    
    public List<DailyAvailabilityDto> getAvailability() {
        return availability;
    }
    
    public void setAvailability(List<DailyAvailabilityDto> availability) {
        this.availability = availability;
    }
} 