package com.healthfirst.dto;

import java.time.LocalDate;
import java.util.List;

public class CreateAvailabilityResponse {
    
    private String availabilityId;
    private int slotsCreated;
    private DateRangeDto dateRange;
    private int totalAppointmentsAvailable;
    private List<SlotInfoDto> generatedSlots;
    
    // Embedded DTOs
    public static class DateRangeDto {
        private LocalDate start;
        private LocalDate end;
        
        public DateRangeDto() {}
        
        public DateRangeDto(LocalDate start, LocalDate end) {
            this.start = start;
            this.end = end;
        }
        
        public LocalDate getStart() {
            return start;
        }
        
        public void setStart(LocalDate start) {
            this.start = start;
        }
        
        public LocalDate getEnd() {
            return end;
        }
        
        public void setEnd(LocalDate end) {
            this.end = end;
        }
    }
    
    public static class SlotInfoDto {
        private String slotId;
        private String date;
        private String startTime;
        private String endTime;
        private String timezone;
        private String status;
        private String appointmentType;
        
        public SlotInfoDto() {}
        
        public SlotInfoDto(String slotId, String date, String startTime, String endTime, 
                          String timezone, String status, String appointmentType) {
            this.slotId = slotId;
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
            this.timezone = timezone;
            this.status = status;
            this.appointmentType = appointmentType;
        }
        
        public String getSlotId() {
            return slotId;
        }
        
        public void setSlotId(String slotId) {
            this.slotId = slotId;
        }
        
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
    }
    
    // Default constructor
    public CreateAvailabilityResponse() {}
    
    // Constructor with required fields
    public CreateAvailabilityResponse(String availabilityId, int slotsCreated, 
                                   DateRangeDto dateRange, int totalAppointmentsAvailable) {
        this.availabilityId = availabilityId;
        this.slotsCreated = slotsCreated;
        this.dateRange = dateRange;
        this.totalAppointmentsAvailable = totalAppointmentsAvailable;
    }
    
    // Getters and Setters
    public String getAvailabilityId() {
        return availabilityId;
    }
    
    public void setAvailabilityId(String availabilityId) {
        this.availabilityId = availabilityId;
    }
    
    public int getSlotsCreated() {
        return slotsCreated;
    }
    
    public void setSlotsCreated(int slotsCreated) {
        this.slotsCreated = slotsCreated;
    }
    
    public DateRangeDto getDateRange() {
        return dateRange;
    }
    
    public void setDateRange(DateRangeDto dateRange) {
        this.dateRange = dateRange;
    }
    
    public int getTotalAppointmentsAvailable() {
        return totalAppointmentsAvailable;
    }
    
    public void setTotalAppointmentsAvailable(int totalAppointmentsAvailable) {
        this.totalAppointmentsAvailable = totalAppointmentsAvailable;
    }
    
    public List<SlotInfoDto> getGeneratedSlots() {
        return generatedSlots;
    }
    
    public void setGeneratedSlots(List<SlotInfoDto> generatedSlots) {
        this.generatedSlots = generatedSlots;
    }
    
    @Override
    public String toString() {
        return "CreateAvailabilityResponse{" +
                "availabilityId='" + availabilityId + '\'' +
                ", slotsCreated=" + slotsCreated +
                ", dateRange=" + dateRange +
                ", totalAppointmentsAvailable=" + totalAppointmentsAvailable +
                '}';
    }
} 