package com.healthfirst.service;

import com.healthfirst.dto.ClinicAddressDto;
import com.healthfirst.dto.ProviderRegistrationRequest;
import com.healthfirst.dto.ProviderRegistrationResponse;
import com.healthfirst.entity.ClinicAddress;
import com.healthfirst.entity.Provider;
import com.healthfirst.entity.VerificationStatus;
import com.healthfirst.repository.ProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProviderService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProviderService.class);
    
    private final ProviderRepository providerRepository;
    private final PasswordService passwordService;
    private final EmailService emailService;
    
    // Predefined list of valid specializations
    private static final List<String> VALID_SPECIALIZATIONS = Arrays.asList(
        "Cardiology", "Dermatology", "Endocrinology", "Gastroenterology", 
        "General Practice", "Gynecology", "Hematology", "Infectious Disease",
        "Internal Medicine", "Nephrology", "Neurology", "Oncology", 
        "Ophthalmology", "Orthopedics", "Otolaryngology", "Pathology",
        "Pediatrics", "Psychiatry", "Pulmonology", "Radiology", 
        "Rheumatology", "Surgery", "Urology", "Emergency Medicine",
        "Family Medicine", "Geriatrics", "Obstetrics", "Physical Medicine",
        "Preventive Medicine", "Sports Medicine"
    );
    
    @Autowired
    public ProviderService(ProviderRepository providerRepository, 
                         PasswordService passwordService, 
                         EmailService emailService) {
        this.providerRepository = providerRepository;
        this.passwordService = passwordService;
        this.emailService = emailService;
    }
    
    /**
     * Register a new provider
     */
    public ProviderRegistrationResponse registerProvider(ProviderRegistrationRequest request) {
        logger.info("Starting provider registration for email: {}", request.getEmail());
        
        // Validate request
        validateRegistrationRequest(request);
        
        // Check for existing providers
        checkForExistingProviders(request);
        
        // Validate specialization
        validateSpecialization(request.getSpecialization());
        
        // Create provider entity
        Provider provider = createProviderFromRequest(request);
        
        // Save provider
        Provider savedProvider = providerRepository.save(provider);
        
        // Send verification email
        emailService.sendVerificationEmail(
            savedProvider.getEmail(), 
            savedProvider.getFirstName() + " " + savedProvider.getLastName(),
            "verification-token-placeholder"
        );
        
        logger.info("Provider registered successfully with ID: {}", savedProvider.getId());
        
        return new ProviderRegistrationResponse(
            savedProvider.getId(),
            savedProvider.getEmail(),
            savedProvider.getVerificationStatus()
        );
    }
    
    /**
     * Validate registration request
     */
    private void validateRegistrationRequest(ProviderRegistrationRequest request) {
        // Password confirmation validation
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and confirmation password do not match");
        }
        
        // Password strength validation
        if (!passwordService.isValidPassword(request.getPassword())) {
            throw new IllegalArgumentException("Password does not meet security requirements");
        }
    }
    
    /**
     * Check for existing providers with same email, phone, or license
     */
    private void checkForExistingProviders(ProviderRegistrationRequest request) {
        if (providerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email address is already registered");
        }
        
        if (providerRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number is already registered");
        }
        
        if (providerRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new IllegalArgumentException("License number is already registered");
        }
    }
    
    /**
     * Validate specialization
     */
    private void validateSpecialization(String specialization) {
        if (!VALID_SPECIALIZATIONS.contains(specialization)) {
            throw new IllegalArgumentException("Invalid specialization. Please choose from the predefined list.");
        }
    }
    
    /**
     * Create provider entity from request
     */
    private Provider createProviderFromRequest(ProviderRegistrationRequest request) {
        // Hash password
        String hashedPassword = passwordService.hashPassword(request.getPassword());
        
        // Create clinic address
        ClinicAddress clinicAddress = new ClinicAddress(
            request.getClinicAddress().getStreet(),
            request.getClinicAddress().getCity(),
            request.getClinicAddress().getState(),
            request.getClinicAddress().getZip()
        );
        
        // Create provider
        return new Provider(
            request.getFirstName(),
            request.getLastName(),
            request.getEmail(),
            request.getPhoneNumber(),
            hashedPassword,
            request.getSpecialization(),
            request.getLicenseNumber(),
            request.getYearsOfExperience(),
            clinicAddress
        );
    }
    
    /**
     * Get provider by ID
     */
    public Provider getProviderById(UUID id) {
        return providerRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Provider not found with ID: " + id));
    }
    
    /**
     * Get provider by email
     */
    public Provider getProviderByEmail(String email) {
        return providerRepository.findByEmailAndIsActiveTrue(email)
            .orElseThrow(() -> new IllegalArgumentException("Provider not found with email: " + email));
    }
    
    /**
     * Update provider verification status
     */
    public void updateVerificationStatus(UUID providerId, VerificationStatus status) {
        Provider provider = getProviderById(providerId);
        provider.setVerificationStatus(status);
        providerRepository.save(provider);
        
        // Send status update email
        emailService.sendVerificationStatusUpdate(
            provider.getEmail(),
            provider.getFirstName() + " " + provider.getLastName(),
            status.name()
        );
    }
    
    /**
     * Get all providers by verification status
     */
    public List<Provider> getProvidersByVerificationStatus(VerificationStatus status) {
        return providerRepository.findByVerificationStatus(status);
    }
    
    /**
     * Get valid specializations
     */
    public List<String> getValidSpecializations() {
        return VALID_SPECIALIZATIONS;
    }
} 