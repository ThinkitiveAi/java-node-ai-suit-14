package com.healthfirst.repository;

import com.healthfirst.entity.ProviderAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProviderAvailabilityRepository extends JpaRepository<ProviderAvailability, UUID> {
    
    // Find by provider ID
    List<ProviderAvailability> findByProviderId(UUID providerId);
    
    // Find by provider ID and date range
    List<ProviderAvailability> findByProviderIdAndDateBetween(UUID providerId, LocalDate startDate, LocalDate endDate);
    
    // Find by provider ID and status
    List<ProviderAvailability> findByProviderIdAndStatus(UUID providerId, ProviderAvailability.AvailabilityStatus status);
    
    // Find by provider ID, date range, and status
    List<ProviderAvailability> findByProviderIdAndDateBetweenAndStatus(
            UUID providerId, LocalDate startDate, LocalDate endDate, ProviderAvailability.AvailabilityStatus status);
    
    // Find by provider ID and appointment type
    List<ProviderAvailability> findByProviderIdAndAppointmentType(UUID providerId, ProviderAvailability.AppointmentType appointmentType);
    
    // Find available slots for a specific date
    @Query("SELECT pa FROM ProviderAvailability pa WHERE pa.providerId = :providerId " +
           "AND pa.date = :date AND pa.status = 'AVAILABLE' " +
           "AND pa.currentAppointments < pa.maxAppointmentsPerSlot")
    List<ProviderAvailability> findAvailableSlotsForDate(@Param("providerId") UUID providerId, @Param("date") LocalDate date);
    
    // Find by provider ID and date
    List<ProviderAvailability> findByProviderIdAndDate(UUID providerId, LocalDate date);
    
    // Find recurring availability for a provider
    List<ProviderAvailability> findByProviderIdAndIsRecurringTrue(UUID providerId);
    
    // Find by provider ID and recurrence pattern
    List<ProviderAvailability> findByProviderIdAndRecurrencePattern(UUID providerId, ProviderAvailability.RecurrencePattern pattern);
    
    // Count total available appointments for a provider in a date range
    @Query("SELECT SUM(pa.maxAppointmentsPerSlot - pa.currentAppointments) FROM ProviderAvailability pa " +
           "WHERE pa.providerId = :providerId AND pa.date BETWEEN :startDate AND :endDate " +
           "AND pa.status = 'AVAILABLE'")
    Long countAvailableAppointmentsInRange(@Param("providerId") UUID providerId, 
                                         @Param("startDate") LocalDate startDate, 
                                         @Param("endDate") LocalDate endDate);
    
    // Find availability with location type
    List<ProviderAvailability> findByProviderIdAndLocationType(UUID providerId, ProviderAvailability.LocationType locationType);
    
    // Find by provider ID and timezone
    List<ProviderAvailability> findByProviderIdAndTimezone(UUID providerId, String timezone);
    
    // Find availability with pricing information
    @Query("SELECT pa FROM ProviderAvailability pa WHERE pa.providerId = :providerId " +
           "AND pa.pricing.baseFee IS NOT NULL AND pa.pricing.baseFee > 0")
    List<ProviderAvailability> findAvailabilityWithPricing(@Param("providerId") UUID providerId);
    
    // Find availability with special requirements
    @Query("SELECT pa FROM ProviderAvailability pa WHERE pa.providerId = :providerId " +
           "AND :requirement MEMBER OF pa.specialRequirements")
    List<ProviderAvailability> findAvailabilityWithRequirement(@Param("providerId") UUID providerId, 
                                                             @Param("requirement") String requirement);
    
    // Delete by provider ID and date range
    void deleteByProviderIdAndDateBetween(UUID providerId, LocalDate startDate, LocalDate endDate);
    
    // Delete by provider ID and status
    void deleteByProviderIdAndStatus(UUID providerId, ProviderAvailability.AvailabilityStatus status);
    
    // Check if availability exists for provider and date
    boolean existsByProviderIdAndDate(UUID providerId, LocalDate date);
    
    // Count total availability records for a provider
    long countByProviderId(UUID providerId);
    
    // Find availability with notes containing specific text
    @Query("SELECT pa FROM ProviderAvailability pa WHERE pa.providerId = :providerId " +
           "AND pa.notes IS NOT NULL AND pa.notes LIKE %:searchText%")
    List<ProviderAvailability> findAvailabilityWithNotesContaining(@Param("providerId") UUID providerId, 
                                                                 @Param("searchText") String searchText);
} 