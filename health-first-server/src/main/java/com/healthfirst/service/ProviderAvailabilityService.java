package com.healthfirst.service;

import com.healthfirst.dto.CreateAvailabilityRequest;
import com.healthfirst.dto.CreateAvailabilityResponse;
import com.healthfirst.dto.UpdateAvailabilityRequest;
import com.healthfirst.entity.AppointmentSlot;
import com.healthfirst.entity.ProviderAvailability;
import com.healthfirst.repository.AppointmentSlotRepository;
import com.healthfirst.repository.ProviderAvailabilityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProviderAvailabilityService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProviderAvailabilityService.class);
    
    private final ProviderAvailabilityRepository availabilityRepository;
    private final AppointmentSlotRepository slotRepository;
    
    @Autowired
    public ProviderAvailabilityService(ProviderAvailabilityRepository availabilityRepository,
                                    AppointmentSlotRepository slotRepository) {
        this.availabilityRepository = availabilityRepository;
        this.slotRepository = slotRepository;
    }
    
    /**
     * Create availability slots for a provider
     */
    @Transactional
    public CreateAvailabilityResponse createAvailability(UUID providerId, CreateAvailabilityRequest request) {
        logger.info("Creating availability for provider: {} with date: {}", providerId, request.getDate());
        
        // Parse date and times
        LocalDate date = LocalDate.parse(request.getDate());
        LocalTime startTime = LocalTime.parse(request.getStartTime());
        LocalTime endTime = LocalTime.parse(request.getEndTime());
        
        // Validate time range
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time cannot be after end time");
        }
        
        // Validate date is not in the past
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot create availability for past dates");
        }
        
        List<ProviderAvailability> availabilities = new ArrayList<>();
        List<AppointmentSlot> generatedSlots = new ArrayList<>();
        
        if (request.getIsRecurring() && request.getRecurrencePattern() != null) {
            // Handle recurring availability
            LocalDate endDate = request.getRecurrenceEndDate() != null ? 
                LocalDate.parse(request.getRecurrenceEndDate()) : date.plusMonths(6);
            
            availabilities = createRecurringAvailability(providerId, request, date, endDate);
        } else {
            // Handle single day availability
            ProviderAvailability availability = createSingleAvailability(providerId, request, date);
            availabilities.add(availability);
        }
        
        // Generate appointment slots for each availability
        for (ProviderAvailability availability : availabilities) {
            List<AppointmentSlot> slots = generateAppointmentSlots(availability);
            generatedSlots.addAll(slots);
        }
        
        // Save all availabilities and slots
        availabilityRepository.saveAll(availabilities);
        slotRepository.saveAll(generatedSlots);
        
        // Create response
        CreateAvailabilityResponse.DateRangeDto dateRange = new CreateAvailabilityResponse.DateRangeDto(
            availabilities.stream().map(ProviderAvailability::getDate).min(LocalDate::compareTo).orElse(date),
            availabilities.stream().map(ProviderAvailability::getDate).max(LocalDate::compareTo).orElse(date)
        );
        
        int totalAppointments = generatedSlots.size();
        
        CreateAvailabilityResponse response = new CreateAvailabilityResponse(
            availabilities.get(0).getId().toString(),
            generatedSlots.size(),
            dateRange,
            totalAppointments
        );
        
        // Add generated slots info to response
        List<CreateAvailabilityResponse.SlotInfoDto> slotInfos = generatedSlots.stream()
            .map(this::convertToSlotInfoDto)
            .collect(Collectors.toList());
        response.setGeneratedSlots(slotInfos);
        
        logger.info("Created {} availability records and {} appointment slots for provider: {}", 
                   availabilities.size(), generatedSlots.size(), providerId);
        
        return response;
    }
    
    /**
     * Create recurring availability based on pattern
     */
    private List<ProviderAvailability> createRecurringAvailability(UUID providerId, 
                                                                 CreateAvailabilityRequest request,
                                                                 LocalDate startDate, 
                                                                 LocalDate endDate) {
        List<ProviderAvailability> availabilities = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            ProviderAvailability availability = createSingleAvailability(providerId, request, currentDate);
            availabilities.add(availability);
            
            // Calculate next date based on recurrence pattern
            switch (request.getRecurrencePattern()) {
                case DAILY:
                    currentDate = currentDate.plusDays(1);
                    break;
                case WEEKLY:
                    currentDate = currentDate.plusWeeks(1);
                    break;
                case MONTHLY:
                    currentDate = currentDate.plusMonths(1);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid recurrence pattern: " + request.getRecurrencePattern());
            }
        }
        
        return availabilities;
    }
    
    /**
     * Create single availability record
     */
    private ProviderAvailability createSingleAvailability(UUID providerId, 
                                                       CreateAvailabilityRequest request,
                                                       LocalDate date) {
        ProviderAvailability availability = new ProviderAvailability();
        availability.setProviderId(providerId);
        availability.setDate(date);
        availability.setStartTime(LocalTime.parse(request.getStartTime()));
        availability.setEndTime(LocalTime.parse(request.getEndTime()));
        availability.setTimezone(request.getTimezone());
        availability.setIsRecurring(request.getIsRecurring());
        availability.setRecurrencePattern(request.getRecurrencePattern());
        availability.setSlotDuration(request.getSlotDuration());
        availability.setBreakDuration(request.getBreakDuration());
        availability.setMaxAppointmentsPerSlot(request.getMaxAppointmentsPerSlot());
        availability.setAppointmentType(request.getAppointmentType());
        availability.setNotes(request.getNotes());
        availability.setSpecialRequirements(request.getSpecialRequirements());
        
        // Set location if provided
        if (request.getLocation() != null) {
            ProviderAvailability.Location location = new ProviderAvailability.Location(
                request.getLocation().getType(),
                request.getLocation().getAddress(),
                request.getLocation().getRoomNumber()
            );
            availability.setLocation(location);
        }
        
        // Set pricing if provided
        if (request.getPricing() != null) {
            ProviderAvailability.Pricing pricing = new ProviderAvailability.Pricing(
                request.getPricing().getBaseFee(),
                request.getPricing().getInsuranceAccepted(),
                request.getPricing().getCurrency()
            );
            availability.setPricing(pricing);
        }
        
        return availability;
    }
    
    /**
     * Generate appointment slots from availability
     */
    private List<AppointmentSlot> generateAppointmentSlots(ProviderAvailability availability) {
        List<AppointmentSlot> slots = new ArrayList<>();
        
        ZoneId zoneId = ZoneId.of(availability.getTimezone());
        LocalDate date = availability.getDate();
        LocalTime currentTime = availability.getStartTime();
        
        while (currentTime.isBefore(availability.getEndTime())) {
            // Calculate slot end time
            LocalTime slotEndTime = currentTime.plusMinutes(availability.getSlotDuration());
            
            // Check if slot would exceed end time
            if (slotEndTime.isAfter(availability.getEndTime())) {
                break;
            }
            
            // Create appointment slot
            ZonedDateTime slotStart = ZonedDateTime.of(date, currentTime, zoneId);
            ZonedDateTime slotEnd = ZonedDateTime.of(date, slotEndTime, zoneId);
            
            AppointmentSlot slot = new AppointmentSlot(
                availability.getId(),
                availability.getProviderId(),
                slotStart,
                slotEnd
            );
            slot.setAppointmentType(availability.getAppointmentType().name());
            
            slots.add(slot);
            
            // Move to next slot (including break duration)
            currentTime = slotEndTime.plusMinutes(availability.getBreakDuration());
        }
        
        return slots;
    }
    
    /**
     * Convert appointment slot to DTO
     */
    private CreateAvailabilityResponse.SlotInfoDto convertToSlotInfoDto(AppointmentSlot slot) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        return new CreateAvailabilityResponse.SlotInfoDto(
            slot.getId().toString(),
            slot.getSlotStartTime().format(dateFormatter),
            slot.getSlotStartTime().format(timeFormatter),
            slot.getSlotEndTime().format(timeFormatter),
            slot.getSlotStartTime().getZone().getId(),
            slot.getStatus().name(),
            slot.getAppointmentType()
        );
    }
    
    /**
     * Get availability for a provider
     */
    public List<ProviderAvailability> getProviderAvailability(UUID providerId) {
        return availabilityRepository.findByProviderId(providerId);
    }
    
    /**
     * Get availability for a provider in date range
     */
    public List<ProviderAvailability> getProviderAvailabilityInRange(UUID providerId, 
                                                                   LocalDate startDate, 
                                                                   LocalDate endDate) {
        return availabilityRepository.findByProviderIdAndDateBetween(providerId, startDate, endDate);
    }
    
    /**
     * Get available slots for a provider on a specific date
     */
    public List<ProviderAvailability> getAvailableSlotsForDate(UUID providerId, LocalDate date) {
        return availabilityRepository.findAvailableSlotsForDate(providerId, date);
    }
    
    /**
     * Update availability status
     */
    @Transactional
    public ProviderAvailability updateAvailabilityStatus(UUID availabilityId, 
                                                       ProviderAvailability.AvailabilityStatus status) {
        ProviderAvailability availability = availabilityRepository.findById(availabilityId)
            .orElseThrow(() -> new IllegalArgumentException("Availability not found with ID: " + availabilityId));
        
        availability.setStatus(status);
        return availabilityRepository.save(availability);
    }
    
    /**
     * Delete availability and associated slots
     */
    @Transactional
    public void deleteAvailability(UUID availabilityId) {
        ProviderAvailability availability = availabilityRepository.findById(availabilityId)
            .orElseThrow(() -> new IllegalArgumentException("Availability not found with ID: " + availabilityId));
        
        // Delete associated appointment slots
        slotRepository.deleteByAvailabilityId(availabilityId);
        
        // Delete availability
        availabilityRepository.delete(availability);
        
        logger.info("Deleted availability: {} and associated slots", availabilityId);
    }
    
    /**
     * Get appointment slots for a provider
     */
    public List<AppointmentSlot> getAppointmentSlots(UUID providerId) {
        return slotRepository.findByProviderId(providerId);
    }
    
    /**
     * Get available appointment slots for a provider
     */
    public List<AppointmentSlot> getAvailableAppointmentSlots(UUID providerId) {
        return slotRepository.findByProviderIdAndStatus(providerId, AppointmentSlot.SlotStatus.AVAILABLE);
    }
    
    /**
     * Book an appointment slot
     */
    @Transactional
    public AppointmentSlot bookAppointmentSlot(UUID slotId, UUID patientId) {
        AppointmentSlot slot = slotRepository.findById(slotId)
            .orElseThrow(() -> new IllegalArgumentException("Appointment slot not found with ID: " + slotId));
        
        if (slot.getStatus() != AppointmentSlot.SlotStatus.AVAILABLE) {
            throw new IllegalArgumentException("Slot is not available for booking");
        }
        
        // Generate booking reference
        String bookingReference = generateBookingReference();
        
        slot.setStatus(AppointmentSlot.SlotStatus.BOOKED);
        slot.setPatientId(patientId);
        slot.setBookingReference(bookingReference);
        
        return slotRepository.save(slot);
    }
    
    /**
     * Cancel an appointment slot
     */
    @Transactional
    public AppointmentSlot cancelAppointmentSlot(UUID slotId) {
        AppointmentSlot slot = slotRepository.findById(slotId)
            .orElseThrow(() -> new IllegalArgumentException("Appointment slot not found with ID: " + slotId));
        
        slot.setStatus(AppointmentSlot.SlotStatus.CANCELLED);
        slot.setPatientId(null);
        slot.setBookingReference(null);
        
        return slotRepository.save(slot);
    }
    
    /**
     * Generate unique booking reference
     */
    private String generateBookingReference() {
        return "REF-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Get booked appointments for a provider
     */
    public List<AppointmentSlot> getBookedAppointmentsForProvider(UUID providerId) {
        return slotRepository.findBookedSlotsForProvider(providerId);
    }
    
    /**
     * Get booked appointments for a patient
     */
    public List<AppointmentSlot> getBookedAppointmentsForPatient(UUID patientId) {
        return slotRepository.findBookedSlotsForPatient(patientId);
    }
    
    /**
     * Get upcoming appointments for a provider
     */
    public List<AppointmentSlot> getUpcomingAppointmentsForProvider(UUID providerId) {
        ZonedDateTime currentTime = ZonedDateTime.now();
        return slotRepository.findUpcomingAppointmentsForProvider(providerId, currentTime);
    }
    
    /**
     * Get upcoming appointments for a patient
     */
    public List<AppointmentSlot> getUpcomingAppointmentsForPatient(UUID patientId) {
        ZonedDateTime currentTime = ZonedDateTime.now();
        return slotRepository.findUpcomingAppointmentsForPatient(patientId, currentTime);
    }
    
    /**
     * Update a specific availability slot
     */
    @Transactional
    public AppointmentSlot updateAvailabilitySlot(UUID slotId, UpdateAvailabilityRequest request) {
        logger.info("Updating availability slot: {}", slotId);
        
        // Find the slot
        AppointmentSlot slot = slotRepository.findById(slotId)
            .orElseThrow(() -> new IllegalArgumentException("Slot not found with ID: " + slotId));
        
        // Validate time range if provided
        if (request.getStartTime() != null && request.getEndTime() != null) {
            LocalTime startTime = LocalTime.parse(request.getStartTime());
            LocalTime endTime = LocalTime.parse(request.getEndTime());
            
            if (startTime.isAfter(endTime)) {
                throw new IllegalArgumentException("Start time cannot be after end time");
            }
            
            // Update slot times
            ZonedDateTime slotStartTime = slot.getSlotStartTime().with(startTime);
            ZonedDateTime slotEndTime = slot.getSlotEndTime().with(endTime);
            
            slot.setSlotStartTime(slotStartTime);
            slot.setSlotEndTime(slotEndTime);
        }
        
        // Update status if provided
        if (request.getStatus() != null) {
            slot.setStatus(request.getStatus());
        }
        
        // Update appointment type if provided
        if (request.getAppointmentType() != null) {
            slot.setAppointmentType(request.getAppointmentType());
        }
        
        // Update associated availability notes and pricing if provided
        if (request.getNotes() != null || request.getPricing() != null) {
            ProviderAvailability availability = availabilityRepository.findById(slot.getAvailabilityId())
                .orElseThrow(() -> new IllegalArgumentException("Associated availability not found"));
            
            if (request.getNotes() != null) {
                availability.setNotes(request.getNotes());
            }
            
            if (request.getPricing() != null) {
                ProviderAvailability.Pricing pricing = availability.getPricing();
                if (pricing == null) {
                    pricing = new ProviderAvailability.Pricing();
                }
                
                if (request.getPricing().getBaseFee() != null) {
                    pricing.setBaseFee(request.getPricing().getBaseFee());
                }
                
                if (request.getPricing().getInsuranceAccepted() != null) {
                    pricing.setInsuranceAccepted(request.getPricing().getInsuranceAccepted());
                }
                
                if (request.getPricing().getCurrency() != null) {
                    pricing.setCurrency(request.getPricing().getCurrency());
                }
                
                availability.setPricing(pricing);
                availabilityRepository.save(availability);
            }
        }
        
        // Save the updated slot
        AppointmentSlot updatedSlot = slotRepository.save(slot);
        
        logger.info("Successfully updated availability slot: {}", slotId);
        return updatedSlot;
    }
    
    /**
     * Delete a specific availability slot
     */
    @Transactional
    public void deleteAvailabilitySlot(UUID slotId, boolean deleteRecurring, String reason) {
        logger.info("Deleting availability slot: {} with recurring: {}, reason: {}", slotId, deleteRecurring, reason);
        
        // Find the slot
        AppointmentSlot slot = slotRepository.findById(slotId)
            .orElseThrow(() -> new IllegalArgumentException("Slot not found with ID: " + slotId));
        
        if (deleteRecurring) {
            // Delete all slots from the same availability
            UUID availabilityId = slot.getAvailabilityId();
            List<AppointmentSlot> relatedSlots = slotRepository.findByAvailabilityId(availabilityId);
            
            // Check if any slots are booked
            boolean hasBookedSlots = relatedSlots.stream()
                .anyMatch(s -> s.getStatus() == AppointmentSlot.SlotStatus.BOOKED);
            
            if (hasBookedSlots) {
                throw new IllegalArgumentException("Cannot delete recurring slots that have booked appointments");
            }
            
            // Delete all related slots
            slotRepository.deleteAll(relatedSlots);
            
            // Delete the availability itself
            ProviderAvailability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new IllegalArgumentException("Associated availability not found"));
            availabilityRepository.delete(availability);
            
            logger.info("Deleted recurring availability and {} related slots", relatedSlots.size());
        } else {
            // Check if the slot is booked
            if (slot.getStatus() == AppointmentSlot.SlotStatus.BOOKED) {
                throw new IllegalArgumentException("Cannot delete a booked appointment slot");
            }
            
            // Delete only this specific slot
            slotRepository.delete(slot);
            
            logger.info("Deleted single availability slot: {}", slotId);
        }
    }
} 