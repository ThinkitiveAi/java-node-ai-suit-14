package com.healthfirst.service;

import com.healthfirst.dto.AvailabilitySearchResponse;
import com.healthfirst.entity.AppointmentSlot;
import com.healthfirst.entity.Provider;
import com.healthfirst.entity.ProviderAvailability;
import com.healthfirst.repository.AppointmentSlotRepository;
import com.healthfirst.repository.ProviderRepository;
import com.healthfirst.repository.ProviderAvailabilityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AvailabilitySearchService {
    
    private static final Logger logger = LoggerFactory.getLogger(AvailabilitySearchService.class);
    
    private final AppointmentSlotRepository slotRepository;
    private final ProviderRepository providerRepository;
    private final ProviderAvailabilityRepository availabilityRepository;
    
    @Autowired
    public AvailabilitySearchService(AppointmentSlotRepository slotRepository,
                                  ProviderRepository providerRepository,
                                  ProviderAvailabilityRepository availabilityRepository) {
        this.slotRepository = slotRepository;
        this.providerRepository = providerRepository;
        this.availabilityRepository = availabilityRepository;
    }
    
    /**
     * Search for available appointment slots based on various criteria
     */
    public AvailabilitySearchResponse searchAvailableSlots(LocalDate date, LocalDate startDate, LocalDate endDate,
                                                        String specialization, String location, String appointmentType,
                                                        Boolean insuranceAccepted, BigDecimal maxPrice, String timezone,
                                                        Boolean availableOnly) {
        logger.info("Searching available slots with criteria: date={}, startDate={}, endDate={}, specialization={}, location={}, appointmentType={}, insuranceAccepted={}, maxPrice={}, timezone={}, availableOnly={}", 
                   date, startDate, endDate, specialization, location, appointmentType, insuranceAccepted, maxPrice, timezone, availableOnly);
        
        // Validate date parameters
        if (date != null && (startDate != null || endDate != null)) {
            throw new IllegalArgumentException("Cannot specify both specific date and date range");
        }
        
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        // Determine date range
        LocalDate searchStartDate = date != null ? date : (startDate != null ? startDate : LocalDate.now());
        LocalDate searchEndDate = date != null ? date : (endDate != null ? endDate : searchStartDate.plusMonths(1));
        
        // Get all available slots in the date range
        List<AppointmentSlot> availableSlots = slotRepository.findAvailableSlotsInDateRange(
            searchStartDate.toString(), searchEndDate.toString());
        
        // Filter by appointment type if specified
        if (appointmentType != null) {
            availableSlots = availableSlots.stream()
                .filter(slot -> appointmentType.equalsIgnoreCase(slot.getAppointmentType()))
                .collect(Collectors.toList());
        }
        
        // Get provider IDs from the filtered slots
        Set<UUID> providerIds = availableSlots.stream()
            .map(AppointmentSlot::getProviderId)
            .collect(Collectors.toSet());
        
        // Get providers and filter by specialization if specified
        List<Provider> providers = providerRepository.findAllById(providerIds);
        if (specialization != null) {
            providers = providers.stream()
                .filter(provider -> specialization.equalsIgnoreCase(provider.getSpecialization()))
                .collect(Collectors.toList());
        }
        
        // Create search criteria
        AvailabilitySearchResponse.SearchCriteriaDto searchCriteria = new AvailabilitySearchResponse.SearchCriteriaDto(
            date, specialization, location, appointmentType, insuranceAccepted, maxPrice, timezone
        );
        
        // For now, return empty results - we'll implement the full logic later
        List<AvailabilitySearchResponse.SearchResultDto> results = new ArrayList<>();
        
        logger.info("Search completed with {} results from {} providers", 0, results.size());
        
        return new AvailabilitySearchResponse(searchCriteria, 0, results);
    }
} 