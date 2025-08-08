package com.healthfirst.service;

import com.healthfirst.dto.ClinicAddressDto;
import com.healthfirst.dto.PatientRegistrationRequest;
import com.healthfirst.dto.PatientRegistrationResponse;
import com.healthfirst.entity.ClinicAddress;
import com.healthfirst.entity.Patient;
import com.healthfirst.entity.VerificationStatus;
import com.healthfirst.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PatientService {
    
    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);
    
    private final PatientRepository patientRepository;
    private final PasswordService passwordService;
    private final EmailService emailService;
    
    // Predefined list of valid blood types
    private static final List<String> VALID_BLOOD_TYPES = Arrays.asList(
        "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
    );
    
    // Predefined list of valid genders
    private static final List<String> VALID_GENDERS = Arrays.asList(
        "MALE", "FEMALE", "OTHER"
    );
    
    @Autowired
    public PatientService(PatientRepository patientRepository, 
                        PasswordService passwordService, 
                        EmailService emailService) {
        this.patientRepository = patientRepository;
        this.passwordService = passwordService;
        this.emailService = emailService;
    }
    
    /**
     * Register a new patient
     */
    public PatientRegistrationResponse registerPatient(PatientRegistrationRequest request) {
        logger.info("Starting patient registration for email: {}", request.getEmail());
        
        // Validate request
        validateRegistrationRequest(request);
        
        // Check for existing patients
        checkForExistingPatients(request);
        
        // Validate blood type if provided
        if (request.getBloodType() != null && !request.getBloodType().isEmpty()) {
            validateBloodType(request.getBloodType());
        }
        
        // Validate gender
        validateGender(request.getGender());
        
        // Create patient entity
        Patient patient = createPatientFromRequest(request);
        
        // Save patient
        Patient savedPatient = patientRepository.save(patient);
        
        // Send verification email
        emailService.sendVerificationEmail(
            savedPatient.getEmail(), 
            savedPatient.getFirstName() + " " + savedPatient.getLastName(),
            "verification-token-placeholder"
        );
        
        logger.info("Patient registered successfully with ID: {}", savedPatient.getId());
        
        return new PatientRegistrationResponse(
            savedPatient.getId(),
            savedPatient.getEmail(),
            savedPatient.getVerificationStatus()
        );
    }
    
    /**
     * Validate registration request
     */
    private void validateRegistrationRequest(PatientRegistrationRequest request) {
        // Password confirmation validation
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and confirmation password do not match");
        }
        
        // Password strength validation
        if (!passwordService.isValidPassword(request.getPassword())) {
            throw new IllegalArgumentException("Password does not meet security requirements");
        }
        
        // Age validation (must be at least 18 years old)
        LocalDate minimumAge = LocalDate.now().minusYears(18);
        if (request.getDateOfBirth().isAfter(minimumAge)) {
            throw new IllegalArgumentException("Patient must be at least 18 years old");
        }
    }
    
    /**
     * Check for existing patients with same email, phone, or SSN
     */
    private void checkForExistingPatients(PatientRegistrationRequest request) {
        if (patientRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email address is already registered");
        }
        
        if (patientRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number is already registered");
        }
        
        if (patientRepository.existsBySsn(request.getSsn())) {
            throw new IllegalArgumentException("SSN is already registered");
        }
    }
    
    /**
     * Validate blood type
     */
    private void validateBloodType(String bloodType) {
        if (!VALID_BLOOD_TYPES.contains(bloodType)) {
            throw new IllegalArgumentException("Invalid blood type. Please choose from the predefined list.");
        }
    }
    
    /**
     * Validate gender
     */
    private void validateGender(String gender) {
        if (!VALID_GENDERS.contains(gender)) {
            throw new IllegalArgumentException("Invalid gender. Please choose from MALE, FEMALE, or OTHER.");
        }
    }
    
    /**
     * Create patient entity from request
     */
    private Patient createPatientFromRequest(PatientRegistrationRequest request) {
        // Hash password
        String hashedPassword = passwordService.hashPassword(request.getPassword());
        
        // Create address if provided
        ClinicAddress address = null;
        if (request.getAddress() != null) {
            address = new ClinicAddress(
                request.getAddress().getStreet(),
                request.getAddress().getCity(),
                request.getAddress().getState(),
                request.getAddress().getZip()
            );
        }
        
        // Create patient
        Patient patient = new Patient(
            request.getFirstName(),
            request.getLastName(),
            request.getEmail(),
            request.getPhoneNumber(),
            hashedPassword,
            request.getDateOfBirth(),
            request.getSsn(),
            request.getGender()
        );
        
        // Set optional fields
        patient.setBloodType(request.getBloodType());
        patient.setHeightCm(request.getHeightCm());
        patient.setWeightKg(request.getWeightKg());
        patient.setAddress(address);
        patient.setEmergencyContactName(request.getEmergencyContactName());
        patient.setEmergencyContactPhone(request.getEmergencyContactPhone());
        patient.setEmergencyContactRelationship(request.getEmergencyContactRelationship());
        patient.setMedicalHistory(request.getMedicalHistory());
        patient.setAllergies(request.getAllergies());
        patient.setCurrentMedications(request.getCurrentMedications());
        
        return patient;
    }
    
    /**
     * Get patient by ID
     */
    public Patient getPatientById(UUID id) {
        return patientRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Patient not found with ID: " + id));
    }
    
    /**
     * Get patient by email
     */
    public Patient getPatientByEmail(String email) {
        return patientRepository.findByEmailAndIsActiveTrue(email)
            .orElseThrow(() -> new IllegalArgumentException("Patient not found with email: " + email));
    }
    
    /**
     * Get patient by SSN
     */
    public Patient getPatientBySsn(String ssn) {
        return patientRepository.findBySsnAndIsActiveTrue(ssn)
            .orElseThrow(() -> new IllegalArgumentException("Patient not found with SSN: " + ssn));
    }
    
    /**
     * Update patient verification status
     */
    public void updateVerificationStatus(UUID patientId, VerificationStatus status) {
        Patient patient = getPatientById(patientId);
        patient.setVerificationStatus(status);
        patientRepository.save(patient);
        
        // Send status update email
        emailService.sendVerificationStatusUpdate(
            patient.getEmail(),
            patient.getFirstName() + " " + patient.getLastName(),
            status.name()
        );
    }
    
    /**
     * Get all patients by verification status
     */
    public List<Patient> getPatientsByVerificationStatus(VerificationStatus status) {
        return patientRepository.findByVerificationStatus(status);
    }
    
    /**
     * Get patients by gender
     */
    public List<Patient> getPatientsByGender(String gender) {
        validateGender(gender);
        return patientRepository.findByGenderAndIsActiveTrue(gender);
    }
    
    /**
     * Get patients by blood type
     */
    public List<Patient> getPatientsByBloodType(String bloodType) {
        validateBloodType(bloodType);
        return patientRepository.findByBloodTypeAndIsActiveTrue(bloodType);
    }
    
    /**
     * Get patients by age range
     */
    public List<Patient> getPatientsByAgeRange(int minAge, int maxAge) {
        LocalDate endDate = LocalDate.now().minusYears(minAge);
        LocalDate startDate = LocalDate.now().minusYears(maxAge);
        return patientRepository.findByDateOfBirthBetween(startDate, endDate);
    }
    
    /**
     * Get patients by city
     */
    public List<Patient> getPatientsByCity(String city) {
        return patientRepository.findByAddressCity(city);
    }
    
    /**
     * Get patients by state
     */
    public List<Patient> getPatientsByState(String state) {
        return patientRepository.findByAddressState(state);
    }
    
    /**
     * Get valid blood types
     */
    public List<String> getValidBloodTypes() {
        return VALID_BLOOD_TYPES;
    }
    
    /**
     * Get valid genders
     */
    public List<String> getValidGenders() {
        return VALID_GENDERS;
    }
} 