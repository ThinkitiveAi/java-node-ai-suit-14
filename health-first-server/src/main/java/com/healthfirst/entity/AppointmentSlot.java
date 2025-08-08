package com.healthfirst.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointment_slots",
       indexes = {
           @Index(name = "idx_provider_slot_time", columnList = "provider_id, slot_start_time"),
           @Index(name = "idx_availability_id", columnList = "availability_id"),
           @Index(name = "idx_patient_id", columnList = "patient_id"),
           @Index(name = "idx_status", columnList = "status"),
           @Index(name = "idx_booking_reference", columnList = "booking_reference")
       })
public class AppointmentSlot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotNull(message = "Availability ID is required")
    @Column(name = "availability_id", nullable = false)
    private UUID availabilityId;
    
    @NotNull(message = "Provider ID is required")
    @Column(name = "provider_id", nullable = false)
    private UUID providerId;
    
    @NotNull(message = "Slot start time is required")
    @Column(name = "slot_start_time", nullable = false)
    private ZonedDateTime slotStartTime;
    
    @NotNull(message = "Slot end time is required")
    @Column(name = "slot_end_time", nullable = false)
    private ZonedDateTime slotEndTime;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SlotStatus status = SlotStatus.AVAILABLE;
    
    @Column(name = "patient_id")
    private UUID patientId;
    
    @Column(name = "appointment_type", length = 50)
    private String appointmentType;
    
    @Column(name = "booking_reference", unique = true)
    private String bookingReference;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private java.time.LocalDateTime updatedAt;
    
    // Enum
    public enum SlotStatus {
        AVAILABLE, BOOKED, CANCELLED, BLOCKED
    }
    
    // Default constructor
    public AppointmentSlot() {}
    
    // Constructor with required fields
    public AppointmentSlot(UUID availabilityId, UUID providerId, ZonedDateTime slotStartTime, 
                          ZonedDateTime slotEndTime) {
        this.availabilityId = availabilityId;
        this.providerId = providerId;
        this.slotStartTime = slotStartTime;
        this.slotEndTime = slotEndTime;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getAvailabilityId() {
        return availabilityId;
    }
    
    public void setAvailabilityId(UUID availabilityId) {
        this.availabilityId = availabilityId;
    }
    
    public UUID getProviderId() {
        return providerId;
    }
    
    public void setProviderId(UUID providerId) {
        this.providerId = providerId;
    }
    
    public ZonedDateTime getSlotStartTime() {
        return slotStartTime;
    }
    
    public void setSlotStartTime(ZonedDateTime slotStartTime) {
        this.slotStartTime = slotStartTime;
    }
    
    public ZonedDateTime getSlotEndTime() {
        return slotEndTime;
    }
    
    public void setSlotEndTime(ZonedDateTime slotEndTime) {
        this.slotEndTime = slotEndTime;
    }
    
    public SlotStatus getStatus() {
        return status;
    }
    
    public void setStatus(SlotStatus status) {
        this.status = status;
    }
    
    public UUID getPatientId() {
        return patientId;
    }
    
    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }
    
    public String getAppointmentType() {
        return appointmentType;
    }
    
    public void setAppointmentType(String appointmentType) {
        this.appointmentType = appointmentType;
    }
    
    public String getBookingReference() {
        return bookingReference;
    }
    
    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
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
        return "AppointmentSlot{" +
                "id=" + id +
                ", availabilityId=" + availabilityId +
                ", providerId=" + providerId +
                ", slotStartTime=" + slotStartTime +
                ", slotEndTime=" + slotEndTime +
                ", status=" + status +
                ", patientId=" + patientId +
                ", appointmentType='" + appointmentType + '\'' +
                ", bookingReference='" + bookingReference + '\'' +
                '}';
    }
} 