package com.healthfirst.service;

import com.healthfirst.dto.ClinicAddressDto;
import com.healthfirst.dto.PatientRegistrationRequest;
import com.healthfirst.dto.PatientRegistrationResponse;
import com.healthfirst.entity.ClinicAddress;
import com.healthfirst.entity.Patient;
import com.healthfirst.entity.VerificationStatus;
import com.healthfirst.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {
    
    @Mock
    private PatientRepository patientRepository;
    
    @Mock
    private PasswordService passwordService;
    
    @Mock
    private EmailService emailService;
    
    @InjectMocks
    private PatientService patientService;
    
    private PatientRegistrationRequest validRequest;
    private Patient savedPatient;
    
    @BeforeEach
    void setUp() {
        // Create valid request
        ClinicAddressDto address = new ClinicAddressDto(
            "123 Main St",
            "New York",
            "NY",
            "10001"
        );
        
        validRequest = new PatientRegistrationRequest(
            "John",
            "Doe",
            "john.doe@example.com",
            "+1234567890",
            "SecurePassword123!",
            "SecurePassword123!",
            LocalDate.of(1990, 1, 1),
            "123-45-6789",
            "MALE"
        );
        validRequest.setAddress(address);
        validRequest.setBloodType("A+");
        validRequest.setHeightCm(175);
        validRequest.setWeightKg(70.0);
        validRequest.setEmergencyContactName("Jane Doe");
        validRequest.setEmergencyContactPhone("+1987654321");
        validRequest.setEmergencyContactRelationship("Spouse");
        validRequest.setMedicalHistory("No significant medical history");
        validRequest.setAllergies("None");
        validRequest.setCurrentMedications("None");
        
        // Create saved patient
        savedPatient = new Patient();
        savedPatient.setId(UUID.randomUUID());
        savedPatient.setFirstName("John");
        savedPatient.setLastName("Doe");
        savedPatient.setEmail("john.doe@example.com");
        savedPatient.setPhoneNumber("+1234567890");
        savedPatient.setPasswordHash("hashedPassword");
        savedPatient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        savedPatient.setSsn("123-45-6789");
        savedPatient.setGender("MALE");
        savedPatient.setBloodType("A+");
        savedPatient.setHeightCm(175);
        savedPatient.setWeightKg(70.0);
        savedPatient.setAddress(new ClinicAddress(
            "123 Main St",
            "New York",
            "NY",
            "10001"
        ));
        savedPatient.setEmergencyContactName("Jane Doe");
        savedPatient.setEmergencyContactPhone("+1987654321");
        savedPatient.setEmergencyContactRelationship("Spouse");
        savedPatient.setMedicalHistory("No significant medical history");
        savedPatient.setAllergies("None");
        savedPatient.setCurrentMedications("None");
        savedPatient.setVerificationStatus(VerificationStatus.PENDING);
        savedPatient.setIsActive(true);
    }
    
    @Test
    void registerPatient_Success() {
        // Arrange
        when(patientRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
        when(patientRepository.existsByPhoneNumber(validRequest.getPhoneNumber())).thenReturn(false);
        when(patientRepository.existsBySsn(validRequest.getSsn())).thenReturn(false);
        when(passwordService.isValidPassword(validRequest.getPassword())).thenReturn(true);
        when(passwordService.hashPassword(validRequest.getPassword())).thenReturn("hashedPassword");
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);
        
        // Act
        PatientRegistrationResponse response = patientService.registerPatient(validRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(savedPatient.getId(), response.getPatientId());
        assertEquals(savedPatient.getEmail(), response.getEmail());
        assertEquals(VerificationStatus.PENDING, response.getVerificationStatus());
        
        verify(patientRepository).existsByEmail(validRequest.getEmail());
        verify(patientRepository).existsByPhoneNumber(validRequest.getPhoneNumber());
        verify(patientRepository).existsBySsn(validRequest.getSsn());
        verify(passwordService).isValidPassword(validRequest.getPassword());
        verify(passwordService).hashPassword(validRequest.getPassword());
        verify(patientRepository).save(any(Patient.class));
        verify(emailService).sendVerificationEmail(
            eq(savedPatient.getEmail()),
            eq("John Doe"),
            anyString()
        );
    }
    
    @Test
    void registerPatient_DuplicateEmail() {
        // Arrange
        when(patientRepository.existsByEmail(validRequest.getEmail())).thenReturn(true);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> patientService.registerPatient(validRequest)
        );
        
        assertEquals("Email address is already registered", exception.getMessage());
        verify(patientRepository).existsByEmail(validRequest.getEmail());
        verify(patientRepository, never()).save(any(Patient.class));
    }
    
    @Test
    void registerPatient_DuplicatePhoneNumber() {
        // Arrange
        when(patientRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
        when(patientRepository.existsByPhoneNumber(validRequest.getPhoneNumber())).thenReturn(true);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> patientService.registerPatient(validRequest)
        );
        
        assertEquals("Phone number is already registered", exception.getMessage());
        verify(patientRepository).existsByEmail(validRequest.getEmail());
        verify(patientRepository).existsByPhoneNumber(validRequest.getPhoneNumber());
        verify(patientRepository, never()).save(any(Patient.class));
    }
    
    @Test
    void registerPatient_DuplicateSsn() {
        // Arrange
        when(patientRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
        when(patientRepository.existsByPhoneNumber(validRequest.getPhoneNumber())).thenReturn(false);
        when(patientRepository.existsBySsn(validRequest.getSsn())).thenReturn(true);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> patientService.registerPatient(validRequest)
        );
        
        assertEquals("SSN is already registered", exception.getMessage());
        verify(patientRepository).existsByEmail(validRequest.getEmail());
        verify(patientRepository).existsByPhoneNumber(validRequest.getPhoneNumber());
        verify(patientRepository).existsBySsn(validRequest.getSsn());
        verify(patientRepository, never()).save(any(Patient.class));
    }
    
    @Test
    void registerPatient_PasswordMismatch() {
        // Arrange
        validRequest.setConfirmPassword("DifferentPassword123!");
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> patientService.registerPatient(validRequest)
        );
        
        assertEquals("Password and confirmation password do not match", exception.getMessage());
        verify(patientRepository, never()).save(any(Patient.class));
    }
    
    @Test
    void registerPatient_InvalidPassword() {
        // Arrange
        when(passwordService.isValidPassword(validRequest.getPassword())).thenReturn(false);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> patientService.registerPatient(validRequest)
        );
        
        assertEquals("Password does not meet security requirements", exception.getMessage());
        verify(passwordService).isValidPassword(validRequest.getPassword());
        verify(patientRepository, never()).save(any(Patient.class));
    }
    
    @Test
    void registerPatient_Underage() {
        // Arrange
        validRequest.setDateOfBirth(LocalDate.now().minusYears(17)); // Under 18
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> patientService.registerPatient(validRequest)
        );
        
        assertEquals("Patient must be at least 18 years old", exception.getMessage());
        verify(patientRepository, never()).save(any(Patient.class));
    }
    
    @Test
    void registerPatient_InvalidBloodType() {
        // Arrange
        validRequest.setBloodType("INVALID");
        when(patientRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
        when(patientRepository.existsByPhoneNumber(validRequest.getPhoneNumber())).thenReturn(false);
        when(patientRepository.existsBySsn(validRequest.getSsn())).thenReturn(false);
        when(passwordService.isValidPassword(validRequest.getPassword())).thenReturn(true);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> patientService.registerPatient(validRequest)
        );
        
        assertEquals("Invalid blood type. Please choose from the predefined list.", exception.getMessage());
        verify(patientRepository, never()).save(any(Patient.class));
    }
    
    @Test
    void registerPatient_InvalidGender() {
        // Arrange
        validRequest.setGender("INVALID");
        when(patientRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
        when(patientRepository.existsByPhoneNumber(validRequest.getPhoneNumber())).thenReturn(false);
        when(patientRepository.existsBySsn(validRequest.getSsn())).thenReturn(false);
        when(passwordService.isValidPassword(validRequest.getPassword())).thenReturn(true);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> patientService.registerPatient(validRequest)
        );
        
        assertEquals("Invalid gender. Please choose from MALE, FEMALE, or OTHER.", exception.getMessage());
        verify(patientRepository, never()).save(any(Patient.class));
    }
    
    @Test
    void getValidBloodTypes() {
        // Act
        var bloodTypes = patientService.getValidBloodTypes();
        
        // Assert
        assertNotNull(bloodTypes);
        assertTrue(bloodTypes.contains("A+"));
        assertTrue(bloodTypes.contains("O-"));
        assertFalse(bloodTypes.contains("INVALID"));
    }
    
    @Test
    void getValidGenders() {
        // Act
        var genders = patientService.getValidGenders();
        
        // Assert
        assertNotNull(genders);
        assertTrue(genders.contains("MALE"));
        assertTrue(genders.contains("FEMALE"));
        assertTrue(genders.contains("OTHER"));
        assertFalse(genders.contains("INVALID"));
    }
    
    @Test
    void getPatientById_Success() {
        // Arrange
        UUID patientId = UUID.randomUUID();
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(savedPatient));
        
        // Act
        Patient result = patientService.getPatientById(patientId);
        
        // Assert
        assertNotNull(result);
        assertEquals(savedPatient.getId(), result.getId());
        verify(patientRepository).findById(patientId);
    }
    
    @Test
    void getPatientById_NotFound() {
        // Arrange
        UUID patientId = UUID.randomUUID();
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> patientService.getPatientById(patientId)
        );
        
        assertEquals("Patient not found with ID: " + patientId, exception.getMessage());
        verify(patientRepository).findById(patientId);
    }
} 