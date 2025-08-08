package com.healthfirst.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "provider_availability", 
       indexes = {
           @Index(name = "idx_provider_date", columnList = "provider_id, date"),
           @Index(name = "idx_date_status", columnList = "date, status")
       })
public class ProviderAvailability {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotNull(message = "Provider ID is required")
    @Column(name = "provider_id", nullable = false)
    private UUID providerId;
    
    @NotNull(message = "Date is required")
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    @NotNull(message = "Start time is required")
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @NotNull(message = "End time is required")
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @NotBlank(message = "Timezone is required")
    @Column(name = "timezone", nullable = false)
    private String timezone;
    
    @Column(name = "is_recurring", nullable = false)
    private Boolean isRecurring = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_pattern")
    private RecurrencePattern recurrencePattern;
    
    @Column(name = "recurrence_end_date")
    private LocalDate recurrenceEndDate;
    
    @Min(value = 15, message = "Slot duration must be at least 15 minutes")
    @Max(value = 480, message = "Slot duration cannot exceed 8 hours")
    @Column(name = "slot_duration", nullable = false)
    private Integer slotDuration = 30;
    
    @Min(value = 0, message = "Break duration cannot be negative")
    @Max(value = 120, message = "Break duration cannot exceed 2 hours")
    @Column(name = "break_duration", nullable = false)
    private Integer breakDuration = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AvailabilityStatus status = AvailabilityStatus.AVAILABLE;
    
    @Min(value = 1, message = "Max appointments per slot must be at least 1")
    @Max(value = 10, message = "Max appointments per slot cannot exceed 10")
    @Column(name = "max_appointments_per_slot", nullable = false)
    private Integer maxAppointmentsPerSlot = 1;
    
    @Min(value = 0, message = "Current appointments cannot be negative")
    @Column(name = "current_appointments", nullable = false)
    private Integer currentAppointments = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "appointment_type", nullable = false)
    private AppointmentType appointmentType = AppointmentType.CONSULTATION;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "type", column = @Column(name = "location_type")),
        @AttributeOverride(name = "address", column = @Column(name = "location_address")),
        @AttributeOverride(name = "roomNumber", column = @Column(name = "location_room_number"))
    })
    private Location location;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "baseFee", column = @Column(name = "pricing_base_fee")),
        @AttributeOverride(name = "insuranceAccepted", column = @Column(name = "pricing_insurance_accepted")),
        @AttributeOverride(name = "currency", column = @Column(name = "pricing_currency"))
    })
    private Pricing pricing;
    
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Column(name = "notes", length = 500)
    private String notes;
    
    @ElementCollection
    @CollectionTable(name = "provider_availability_special_requirements", 
                    joinColumns = @JoinColumn(name = "availability_id"))
    @Column(name = "requirement")
    private List<String> specialRequirements;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private java.time.LocalDateTime updatedAt;
    
    // Enums
    public enum RecurrencePattern {
        DAILY, WEEKLY, MONTHLY
    }
    
    public enum AvailabilityStatus {
        AVAILABLE, BOOKED, CANCELLED, BLOCKED, MAINTENANCE
    }
    
    public enum AppointmentType {
        CONSULTATION, FOLLOW_UP, EMERGENCY, TELEMEDICINE
    }
    
    public enum LocationType {
        CLINIC, HOSPITAL, TELEMEDICINE, HOME_VISIT
    }
    
    // Embedded classes
    @Embeddable
    public static class Location {
        @Enumerated(EnumType.STRING)
        @Column(name = "type")
        private LocationType type;
        
        @Column(name = "address")
        private String address;
        
        @Column(name = "room_number")
        private String roomNumber;
        
        // Constructors
        public Location() {}
        
        public Location(LocationType type, String address, String roomNumber) {
            this.type = type;
            this.address = address;
            this.roomNumber = roomNumber;
        }
        
        // Getters and Setters
        public LocationType getType() {
            return type;
        }
        
        public void setType(LocationType type) {
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
    
    @Embeddable
    public static class Pricing {
        @DecimalMin(value = "0.0", message = "Base fee cannot be negative")
        @Column(name = "base_fee", precision = 10, scale = 2)
        private BigDecimal baseFee;
        
        @Column(name = "insurance_accepted")
        private Boolean insuranceAccepted = false;
        
        @Column(name = "currency", length = 3)
        private String currency = "USD";
        
        // Constructors
        public Pricing() {}
        
        public Pricing(BigDecimal baseFee, Boolean insuranceAccepted, String currency) {
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
    public ProviderAvailability() {}
    
    // Constructor with required fields
    public ProviderAvailability(UUID providerId, LocalDate date, LocalTime startTime, 
                              LocalTime endTime, String timezone) {
        this.providerId = providerId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.timezone = timezone;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getProviderId() {
        return providerId;
    }
    
    public void setProviderId(UUID providerId) {
        this.providerId = providerId;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
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
    
    public RecurrencePattern getRecurrencePattern() {
        return recurrencePattern;
    }
    
    public void setRecurrencePattern(RecurrencePattern recurrencePattern) {
        this.recurrencePattern = recurrencePattern;
    }
    
    public LocalDate getRecurrenceEndDate() {
        return recurrenceEndDate;
    }
    
    public void setRecurrenceEndDate(LocalDate recurrenceEndDate) {
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
    
    public AvailabilityStatus getStatus() {
        return status;
    }
    
    public void setStatus(AvailabilityStatus status) {
        this.status = status;
    }
    
    public Integer getMaxAppointmentsPerSlot() {
        return maxAppointmentsPerSlot;
    }
    
    public void setMaxAppointmentsPerSlot(Integer maxAppointmentsPerSlot) {
        this.maxAppointmentsPerSlot = maxAppointmentsPerSlot;
    }
    
    public Integer getCurrentAppointments() {
        return currentAppointments;
    }
    
    public void setCurrentAppointments(Integer currentAppointments) {
        this.currentAppointments = currentAppointments;
    }
    
    public AppointmentType getAppointmentType() {
        return appointmentType;
    }
    
    public void setAppointmentType(AppointmentType appointmentType) {
        this.appointmentType = appointmentType;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public void setLocation(Location location) {
        this.location = location;
    }
    
    public Pricing getPricing() {
        return pricing;
    }
    
    public void setPricing(Pricing pricing) {
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
    
    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public java.time.LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "ProviderAvailability{" +
                "id=" + id +
                ", providerId=" + providerId +
                ", date=" + date +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", timezone='" + timezone + '\'' +
                ", status=" + status +
                ", appointmentType=" + appointmentType +
                '}';
    }
} 