package com.healthfirst.repository;

import com.healthfirst.entity.AppointmentSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, UUID> {
    
    // Find by availability ID
    List<AppointmentSlot> findByAvailabilityId(UUID availabilityId);
    
    // Find by provider ID
    List<AppointmentSlot> findByProviderId(UUID providerId);
    
    // Find by patient ID
    List<AppointmentSlot> findByPatientId(UUID patientId);
    
    // Find by provider ID and status
    List<AppointmentSlot> findByProviderIdAndStatus(UUID providerId, AppointmentSlot.SlotStatus status);
    
    // Find by patient ID and status
    List<AppointmentSlot> findByPatientIdAndStatus(UUID patientId, AppointmentSlot.SlotStatus status);
    
    // Find by provider ID and date range
    @Query("SELECT as FROM AppointmentSlot as WHERE as.providerId = :providerId " +
           "AND as.slotStartTime >= :startTime AND as.slotStartTime < :endTime")
    List<AppointmentSlot> findByProviderIdAndTimeRange(@Param("providerId") UUID providerId,
                                                      @Param("startTime") ZonedDateTime startTime,
                                                      @Param("endTime") ZonedDateTime endTime);
    
    // Find available slots for a provider in a time range
    @Query("SELECT as FROM AppointmentSlot as WHERE as.providerId = :providerId " +
           "AND as.status = 'AVAILABLE' AND as.slotStartTime >= :startTime AND as.slotStartTime < :endTime")
    List<AppointmentSlot> findAvailableSlotsInTimeRange(@Param("providerId") UUID providerId,
                                                       @Param("startTime") ZonedDateTime startTime,
                                                       @Param("endTime") ZonedDateTime endTime);
    
    // Find by booking reference
    Optional<AppointmentSlot> findByBookingReference(String bookingReference);
    
    // Find by appointment type
    List<AppointmentSlot> findByAppointmentType(String appointmentType);
    
    // Find by provider ID and appointment type
    List<AppointmentSlot> findByProviderIdAndAppointmentType(UUID providerId, String appointmentType);
    
    // Find booked slots for a provider
    @Query("SELECT as FROM AppointmentSlot as WHERE as.providerId = :providerId " +
           "AND as.status = 'BOOKED' ORDER BY as.slotStartTime")
    List<AppointmentSlot> findBookedSlotsForProvider(@Param("providerId") UUID providerId);
    
    // Find booked slots for a patient
    @Query("SELECT as FROM AppointmentSlot as WHERE as.patientId = :patientId " +
           "AND as.status = 'BOOKED' ORDER BY as.slotStartTime")
    List<AppointmentSlot> findBookedSlotsForPatient(@Param("patientId") UUID patientId);
    
    // Count available slots for a provider
    long countByProviderIdAndStatus(UUID providerId, AppointmentSlot.SlotStatus status);
    
    // Count booked slots for a provider
    @Query("SELECT COUNT(as) FROM AppointmentSlot as WHERE as.providerId = :providerId " +
           "AND as.status = 'BOOKED'")
    long countBookedSlotsForProvider(@Param("providerId") UUID providerId);
    
    // Count booked slots for a patient
    @Query("SELECT COUNT(as) FROM AppointmentSlot as WHERE as.patientId = :patientId " +
           "AND as.status = 'BOOKED'")
    long countBookedSlotsForPatient(@Param("patientId") UUID patientId);
    
    // Find slots by date range for a provider
    @Query("SELECT as FROM AppointmentSlot as WHERE as.providerId = :providerId " +
           "AND DATE(as.slotStartTime) BETWEEN :startDate AND :endDate")
    List<AppointmentSlot> findByProviderIdAndDateRange(@Param("providerId") UUID providerId,
                                                      @Param("startDate") String startDate,
                                                      @Param("endDate") String endDate);
    
    // Find upcoming appointments for a provider
    @Query("SELECT as FROM AppointmentSlot as WHERE as.providerId = :providerId " +
           "AND as.slotStartTime > :currentTime AND as.status = 'BOOKED' " +
           "ORDER BY as.slotStartTime")
    List<AppointmentSlot> findUpcomingAppointmentsForProvider(@Param("providerId") UUID providerId,
                                                             @Param("currentTime") ZonedDateTime currentTime);
    
    // Find upcoming appointments for a patient
    @Query("SELECT as FROM AppointmentSlot as WHERE as.patientId = :patientId " +
           "AND as.slotStartTime > :currentTime AND as.status = 'BOOKED' " +
           "ORDER BY as.slotStartTime")
    List<AppointmentSlot> findUpcomingAppointmentsForPatient(@Param("patientId") UUID patientId,
                                                            @Param("currentTime") ZonedDateTime currentTime);
    
    // Find conflicting slots for a time range
    @Query("SELECT as FROM AppointmentSlot as WHERE as.providerId = :providerId " +
           "AND as.status IN ('BOOKED', 'BLOCKED') " +
           "AND ((as.slotStartTime < :endTime AND as.slotEndTime > :startTime))")
    List<AppointmentSlot> findConflictingSlots(@Param("providerId") UUID providerId,
                                              @Param("startTime") ZonedDateTime startTime,
                                              @Param("endTime") ZonedDateTime endTime);
    
    // Delete slots by availability ID
    void deleteByAvailabilityId(UUID availabilityId);
    
    // Delete slots by provider ID
    void deleteByProviderId(UUID providerId);
    
    // Delete slots by patient ID
    void deleteByPatientId(UUID patientId);
    
    // Check if booking reference exists
    boolean existsByBookingReference(String bookingReference);
    
    // Find slots by status
    List<AppointmentSlot> findByStatus(AppointmentSlot.SlotStatus status);
    
    // Find slots by provider ID and date
    @Query("SELECT as FROM AppointmentSlot as WHERE as.providerId = :providerId " +
           "AND DATE(as.slotStartTime) = :date")
    List<AppointmentSlot> findByProviderIdAndDate(@Param("providerId") UUID providerId, @Param("date") String date);
    
    // Find available slots in date range
    @Query("SELECT as FROM AppointmentSlot as WHERE as.status = 'AVAILABLE' " +
           "AND DATE(as.slotStartTime) BETWEEN :startDate AND :endDate")
    List<AppointmentSlot> findAvailableSlotsInDateRange(@Param("startDate") String startDate, 
                                                        @Param("endDate") String endDate);
} 