package com.healthfirst.controller;

import com.healthfirst.dto.ApiResponse;
import com.healthfirst.dto.AvailabilitySearchResponse;
import com.healthfirst.entity.AppointmentSlot;
import com.healthfirst.entity.Provider;
import com.healthfirst.entity.ProviderAvailability;
import com.healthfirst.repository.AppointmentSlotRepository;
import com.healthfirst.repository.ProviderRepository;
import com.healthfirst.service.AvailabilitySearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/availability")
@CrossOrigin(origins = "*")
@Tag(name = "Availability Search", description = "APIs for patients to search available appointment slots")
public class AvailabilitySearchController {
    
    private static final Logger logger = LoggerFactory.getLogger(AvailabilitySearchController.class);
    
    private final AvailabilitySearchService searchService;
    
    @Autowired
    public AvailabilitySearchController(AvailabilitySearchService searchService) {
        this.searchService = searchService;
    }
    
    /**
     * Search for available appointment slots
     * GET /api/v1/availability/search
     */
    @GetMapping("/search")
    @Operation(summary = "Search available slots", description = "Search for available appointment slots based on various criteria")
    public ResponseEntity<ApiResponse<AvailabilitySearchResponse>> searchAvailableSlots(
            @Parameter(description = "Specific date (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Start date for range (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date for range (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Provider specialization") @RequestParam(required = false) String specialization,
            @Parameter(description = "Location (city, state, or zip)") @RequestParam(required = false) String location,
            @Parameter(description = "Appointment type") @RequestParam(required = false) String appointmentType,
            @Parameter(description = "Insurance accepted") @RequestParam(required = false) Boolean insuranceAccepted,
            @Parameter(description = "Maximum price") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Timezone") @RequestParam(required = false) String timezone,
            @Parameter(description = "Available slots only") @RequestParam(defaultValue = "true") Boolean availableOnly) {
        
        logger.info("Received availability search request with criteria: date={}, startDate={}, endDate={}, specialization={}, location={}, appointmentType={}, insuranceAccepted={}, maxPrice={}, timezone={}, availableOnly={}", 
                   date, startDate, endDate, specialization, location, appointmentType, insuranceAccepted, maxPrice, timezone, availableOnly);
        
        try {
            AvailabilitySearchResponse response = searchService.searchAvailableSlots(
                date, startDate, endDate, specialization, location, appointmentType, 
                insuranceAccepted, maxPrice, timezone, availableOnly
            );
            
            ApiResponse<AvailabilitySearchResponse> apiResponse = ApiResponse.success(
                "Search completed successfully",
                response
            );
            
            logger.info("Availability search successful, found {} results", response.getTotalResults());
            return ResponseEntity.ok(apiResponse);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Availability search failed - {}", e.getMessage());
            ApiResponse<AvailabilitySearchResponse> apiResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
            
        } catch (Exception e) {
            logger.error("Unexpected error during availability search", e);
            ApiResponse<AvailabilitySearchResponse> apiResponse = ApiResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }
} 