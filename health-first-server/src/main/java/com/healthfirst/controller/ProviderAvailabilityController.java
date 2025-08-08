package com.healthfirst.controller;

import com.healthfirst.dto.ApiResponse;
import com.healthfirst.dto.CreateAvailabilityRequest;
import com.healthfirst.dto.CreateAvailabilityResponse;
import com.healthfirst.dto.ProviderAvailabilityResponse;
import com.healthfirst.dto.UpdateAvailabilityRequest;
import com.healthfirst.entity.AppointmentSlot;
import com.healthfirst.entity.ProviderAvailability;
import com.healthfirst.service.ProviderAvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Comparator;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/provider")
@CrossOrigin(origins = "*")
@Tag(name = "Provider Availability Management", description = "APIs for managing healthcare provider availability and appointment slots")
public class ProviderAvailabilityController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProviderAvailabilityController.class);
    
    private final ProviderAvailabilityService availabilityService;
    
    @Autowired
    public ProviderAvailabilityController(ProviderAvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }
    
    /**
     * Create availability slots for a provider
     * POST /api/v1/provider/availability
     */
    @PostMapping("/availability")
    @Operation(summary = "Create availability slots", description = "Create availability slots for a healthcare provider with optional recurring patterns")
    public ResponseEntity<ApiResponse<CreateAvailabilityResponse>> createAvailability(
            @Parameter(description = "Provider ID") @RequestParam UUID providerId,
            @Valid @RequestBody CreateAvailabilityRequest request) {
        
        logger.info("Received availability creation request for provider: {}", providerId);
        
        try {
            CreateAvailabilityResponse response = availabilityService.createAvailability(providerId, request);
            
            ApiResponse<CreateAvailabilityResponse> apiResponse = ApiResponse.success(
                "Availability slots created successfully",
                response
            );
            
            logger.info("Availability creation successful for provider: {}", providerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Availability creation failed for provider: {} - {}", providerId, e.getMessage());
            ApiResponse<CreateAvailabilityResponse> apiResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
            
        } catch (Exception e) {
            logger.error("Unexpected error during availability creation for provider: {}", providerId, e);
            ApiResponse<CreateAvailabilityResponse> apiResponse = ApiResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }
    
    /**
     * Health check endpoint
     * GET /api/v1/provider/availability/health
     */
    @GetMapping("/availability/health")
    @Operation(summary = "Health check", description = "Check if the availability management service is running")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        ApiResponse<String> apiResponse = ApiResponse.success(
            "Provider Availability Management Service is running",
            "Service is healthy"
        );
        return ResponseEntity.ok(apiResponse);
    }
    
    /**
     * Get provider availability with filtering
     * GET /api/v1/provider/{providerId}/availability
     */
    @GetMapping("/{providerId}/availability")
    @Operation(summary = "Get provider availability", description = "Retrieve provider availability with filtering options")
    public ResponseEntity<ApiResponse<ProviderAvailabilityResponse>> getProviderAvailability(
            @Parameter(description = "Provider ID") @PathVariable UUID providerId,
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Status filter") @RequestParam(required = false) AppointmentSlot.SlotStatus status,
            @Parameter(description = "Appointment type filter") @RequestParam(required = false) String appointmentType,
            @Parameter(description = "Timezone") @RequestParam(required = false) String timezone) {
        
        try {
            // Get availability data from service
            List<ProviderAvailability> availabilities = availabilityService.getProviderAvailabilityInRange(providerId, startDate, endDate);
            List<AppointmentSlot> slots = availabilityService.getAppointmentSlots(providerId);
            
            // Filter slots by date range and status
            List<AppointmentSlot> filteredSlots = slots.stream()
                .filter(slot -> {
                    LocalDate slotDate = slot.getSlotStartTime().toLocalDate();
                    boolean inDateRange = !slotDate.isBefore(startDate) && !slotDate.isAfter(endDate);
                    boolean matchesStatus = status == null || slot.getStatus() == status;
                    boolean matchesType = appointmentType == null || appointmentType.equals(slot.getAppointmentType());
                    return inDateRange && matchesStatus && matchesType;
                })
                .toList();
            
            // Calculate summary
            int totalSlots = filteredSlots.size();
            int availableSlots = (int) filteredSlots.stream().filter(slot -> slot.getStatus() == AppointmentSlot.SlotStatus.AVAILABLE).count();
            int bookedSlots = (int) filteredSlots.stream().filter(slot -> slot.getStatus() == AppointmentSlot.SlotStatus.BOOKED).count();
            int cancelledSlots = (int) filteredSlots.stream().filter(slot -> slot.getStatus() == AppointmentSlot.SlotStatus.CANCELLED).count();
            
            ProviderAvailabilityResponse.AvailabilitySummaryDto summary = new ProviderAvailabilityResponse.AvailabilitySummaryDto(
                totalSlots, availableSlots, bookedSlots, cancelledSlots
            );
            
            // Group slots by date
            Map<LocalDate, List<AppointmentSlot>> slotsByDate = filteredSlots.stream()
                .collect(Collectors.groupingBy(slot -> slot.getSlotStartTime().toLocalDate()));
            
            List<ProviderAvailabilityResponse.DailyAvailabilityDto> dailyAvailability = slotsByDate.entrySet().stream()
                .map(entry -> {
                    List<ProviderAvailabilityResponse.SlotDto> slotDtos = entry.getValue().stream()
                        .map(this::convertToSlotDto)
                        .toList();
                    return new ProviderAvailabilityResponse.DailyAvailabilityDto(entry.getKey(), slotDtos);
                })
                .sorted(Comparator.comparing(ProviderAvailabilityResponse.DailyAvailabilityDto::getDate))
                .toList();
            
            ProviderAvailabilityResponse response = new ProviderAvailabilityResponse(
                providerId.toString(), summary, dailyAvailability
            );
            
            ApiResponse<ProviderAvailabilityResponse> apiResponse = ApiResponse.success(
                "Provider availability retrieved successfully",
                response
            );
            
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            logger.error("Error retrieving availability for provider: {}", providerId, e);
            ApiResponse<ProviderAvailabilityResponse> apiResponse = ApiResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }
    
    /**
     * Get available appointment slots for a provider
     * GET /api/v1/provider/{providerId}/availability/slots/available
     */
    @GetMapping("/{providerId}/availability/slots/available")
    @Operation(summary = "Get available appointment slots", description = "Retrieve all available appointment slots for a provider")
    public ResponseEntity<ApiResponse<List<AppointmentSlot>>> getAvailableAppointmentSlots(
            @Parameter(description = "Provider ID") @PathVariable UUID providerId) {
        
        try {
            List<AppointmentSlot> slots = availabilityService.getAvailableAppointmentSlots(providerId);
            
            ApiResponse<List<AppointmentSlot>> apiResponse = ApiResponse.success(
                "Available appointment slots retrieved successfully",
                slots
            );
            
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            logger.error("Error retrieving available appointment slots for provider: {}", providerId, e);
            ApiResponse<List<AppointmentSlot>> apiResponse = ApiResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }
    
    /**
     * Book an appointment slot
     * POST /api/v1/provider/availability/slots/{slotId}/book
     */
    @PostMapping("/availability/slots/{slotId}/book")
    @Operation(summary = "Book appointment slot", description = "Book an available appointment slot for a patient")
    public ResponseEntity<ApiResponse<AppointmentSlot>> bookAppointmentSlot(
            @Parameter(description = "Slot ID") @PathVariable UUID slotId,
            @Parameter(description = "Patient ID") @RequestParam UUID patientId) {
        
        try {
            AppointmentSlot slot = availabilityService.bookAppointmentSlot(slotId, patientId);
            
            ApiResponse<AppointmentSlot> apiResponse = ApiResponse.success(
                "Appointment slot booked successfully",
                slot
            );
            
            return ResponseEntity.ok(apiResponse);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Appointment booking failed for slot: {} - {}", slotId, e.getMessage());
            ApiResponse<AppointmentSlot> apiResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
            
        } catch (Exception e) {
            logger.error("Error booking appointment slot: {}", slotId, e);
            ApiResponse<AppointmentSlot> apiResponse = ApiResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }
    
    /**
     * Cancel an appointment slot
     * POST /api/v1/provider/availability/slots/{slotId}/cancel
     */
    @PostMapping("/availability/slots/{slotId}/cancel")
    @Operation(summary = "Cancel appointment slot", description = "Cancel a booked appointment slot")
    public ResponseEntity<ApiResponse<AppointmentSlot>> cancelAppointmentSlot(
            @Parameter(description = "Slot ID") @PathVariable UUID slotId) {
        
        try {
            AppointmentSlot slot = availabilityService.cancelAppointmentSlot(slotId);
            
            ApiResponse<AppointmentSlot> apiResponse = ApiResponse.success(
                "Appointment slot cancelled successfully",
                slot
            );
            
            return ResponseEntity.ok(apiResponse);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Appointment cancellation failed for slot: {} - {}", slotId, e.getMessage());
            ApiResponse<AppointmentSlot> apiResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
            
        } catch (Exception e) {
            logger.error("Error cancelling appointment slot: {}", slotId, e);
            ApiResponse<AppointmentSlot> apiResponse = ApiResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }
    
    /**
     * Update specific availability slot
     * PUT /api/v1/provider/availability/{slotId}
     */
    @PutMapping("/availability/{slotId}")
    @Operation(summary = "Update availability slot", description = "Update a specific availability slot with new time, status, notes, and pricing")
    public ResponseEntity<ApiResponse<AppointmentSlot>> updateAvailabilitySlot(
            @Parameter(description = "Slot ID") @PathVariable UUID slotId,
            @Valid @RequestBody UpdateAvailabilityRequest request) {
        
        logger.info("Received availability update request for slot: {}", slotId);
        
        try {
            AppointmentSlot updatedSlot = availabilityService.updateAvailabilitySlot(slotId, request);
            
            ApiResponse<AppointmentSlot> apiResponse = ApiResponse.success(
                "Availability slot updated successfully",
                updatedSlot
            );
            
            logger.info("Availability slot update successful for slot: {}", slotId);
            return ResponseEntity.ok(apiResponse);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Availability slot update failed for slot: {} - {}", slotId, e.getMessage());
            ApiResponse<AppointmentSlot> apiResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
            
        } catch (Exception e) {
            logger.error("Unexpected error during availability slot update for slot: {}", slotId, e);
            ApiResponse<AppointmentSlot> apiResponse = ApiResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }
    
    /**
     * Delete availability slot
     * DELETE /api/v1/provider/availability/{slotId}
     */
    @DeleteMapping("/availability/{slotId}")
    @Operation(summary = "Delete availability slot", description = "Delete a specific availability slot with optional recurring deletion")
    public ResponseEntity<ApiResponse<String>> deleteAvailabilitySlot(
            @Parameter(description = "Slot ID") @PathVariable UUID slotId,
            @Parameter(description = "Delete all recurring instances") @RequestParam(required = false) Boolean deleteRecurring,
            @Parameter(description = "Reason for deletion") @RequestParam(required = false) String reason) {
        
        logger.info("Received availability deletion request for slot: {} with recurring: {}, reason: {}", 
                   slotId, deleteRecurring, reason);
        
        try {
            availabilityService.deleteAvailabilitySlot(slotId, deleteRecurring != null ? deleteRecurring : false, reason);
            
            ApiResponse<String> apiResponse = ApiResponse.success(
                "Availability slot deleted successfully",
                "Slot with ID " + slotId + " has been deleted"
            );
            
            logger.info("Availability slot deletion successful for slot: {}", slotId);
            return ResponseEntity.ok(apiResponse);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Availability slot deletion failed for slot: {} - {}", slotId, e.getMessage());
            ApiResponse<String> apiResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
            
        } catch (Exception e) {
            logger.error("Unexpected error during availability slot deletion for slot: {}", slotId, e);
            ApiResponse<String> apiResponse = ApiResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }
    
    /**
     * Convert AppointmentSlot to SlotDto
     */
    private ProviderAvailabilityResponse.SlotDto convertToSlotDto(AppointmentSlot slot) {
        // Get the associated availability for location and pricing info
        ProviderAvailability availability = availabilityService.getProviderAvailability(slot.getProviderId())
            .stream()
            .filter(avail -> avail.getId().toString().equals(slot.getAvailabilityId().toString()))
            .findFirst()
            .orElse(null);
        
        ProviderAvailabilityResponse.LocationDto location = null;
        ProviderAvailabilityResponse.PricingDto pricing = null;
        
        if (availability != null) {
            if (availability.getLocation() != null) {
                location = new ProviderAvailabilityResponse.LocationDto(
                    availability.getLocation().getType().name().toLowerCase(),
                    availability.getLocation().getAddress(),
                    availability.getLocation().getRoomNumber()
                );
            }
            
            if (availability.getPricing() != null) {
                pricing = new ProviderAvailabilityResponse.PricingDto(
                    availability.getPricing().getBaseFee(),
                    availability.getPricing().getInsuranceAccepted()
                );
            }
        }
        
        return new ProviderAvailabilityResponse.SlotDto(
            slot.getId().toString(),
            slot.getSlotStartTime().toLocalTime(),
            slot.getSlotEndTime().toLocalTime(),
            slot.getStatus().name().toLowerCase(),
            slot.getAppointmentType(),
            location,
            pricing
        );
    }
} 