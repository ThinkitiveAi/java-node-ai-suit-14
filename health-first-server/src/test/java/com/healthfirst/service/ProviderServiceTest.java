package com.healthfirst.service;

import com.healthfirst.dto.ClinicAddressDto;
import com.healthfirst.dto.ProviderRegistrationRequest;
import com.healthfirst.dto.ProviderRegistrationResponse;
import com.healthfirst.entity.ClinicAddress;
import com.healthfirst.entity.Provider;
import com.healthfirst.entity.VerificationStatus;
import com.healthfirst.repository.ProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProviderServiceTest {
    
    @Mock
    private ProviderRepository providerRepository;
    
    @Mock
    private PasswordService passwordService;
    
    @Mock
    private EmailService emailService;
    
    @InjectMocks
    private ProviderService providerService;
    
    private ProviderRegistrationRequest validRequest;
    private Provider savedProvider;
    
    @BeforeEach
    void setUp() {
        // Create valid request
        ClinicAddressDto address = new ClinicAddressDto(
            "123 Medical Center Dr",
            "New York",
            "NY",
            "10001"
        );
        
        validRequest = new ProviderRegistrationRequest(
            "Dr. John",
            "Smith",
            "john.smith@clinic.com",
            "+1234567890",
            "SecurePassword123!",
            "SecurePassword123!",
            "Cardiology",
            "MD123456789",
            15,
            address
        );
        
        // Create saved provider
        savedProvider = new Provider();
        savedProvider.setId(UUID.randomUUID());
        savedProvider.setFirstName("Dr. John");
        savedProvider.setLastName("Smith");
        savedProvider.setEmail("john.smith@clinic.com");
        savedProvider.setPhoneNumber("+1234567890");
        savedProvider.setPasswordHash("hashedPassword");
        savedProvider.setSpecialization("Cardiology");
        savedProvider.setLicenseNumber("MD123456789");
        savedProvider.setYearsOfExperience(15);
        savedProvider.setClinicAddress(new ClinicAddress(
            "123 Medical Center Dr",
            "New York",
            "NY",
            "10001"
        ));
        savedProvider.setVerificationStatus(VerificationStatus.PENDING);
        savedProvider.setIsActive(true);
    }
    
    @Test
    void registerProvider_Success() {
        // Arrange
        when(providerRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
        when(providerRepository.existsByPhoneNumber(validRequest.getPhoneNumber())).thenReturn(false);
        when(providerRepository.existsByLicenseNumber(validRequest.getLicenseNumber())).thenReturn(false);
        when(passwordService.isValidPassword(validRequest.getPassword())).thenReturn(true);
        when(passwordService.hashPassword(validRequest.getPassword())).thenReturn("hashedPassword");
        when(providerRepository.save(any(Provider.class))).thenReturn(savedProvider);
        
        // Act
        ProviderRegistrationResponse response = providerService.registerProvider(validRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(savedProvider.getId(), response.getProviderId());
        assertEquals(savedProvider.getEmail(), response.getEmail());
        assertEquals(VerificationStatus.PENDING, response.getVerificationStatus());
        
        verify(providerRepository).existsByEmail(validRequest.getEmail());
        verify(providerRepository).existsByPhoneNumber(validRequest.getPhoneNumber());
        verify(providerRepository).existsByLicenseNumber(validRequest.getLicenseNumber());
        verify(passwordService).isValidPassword(validRequest.getPassword());
        verify(passwordService).hashPassword(validRequest.getPassword());
        verify(providerRepository).save(any(Provider.class));
        verify(emailService).sendVerificationEmail(
            eq(savedProvider.getEmail()),
            eq("Dr. John Smith"),
            anyString()
        );
    }
    
    @Test
    void registerProvider_DuplicateEmail() {
        // Arrange
        when(providerRepository.existsByEmail(validRequest.getEmail())).thenReturn(true);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> providerService.registerProvider(validRequest)
        );
        
        assertEquals("Email address is already registered", exception.getMessage());
        verify(providerRepository).existsByEmail(validRequest.getEmail());
        verify(providerRepository, never()).save(any(Provider.class));
    }
    
    @Test
    void registerProvider_DuplicatePhoneNumber() {
        // Arrange
        when(providerRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
        when(providerRepository.existsByPhoneNumber(validRequest.getPhoneNumber())).thenReturn(true);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> providerService.registerProvider(validRequest)
        );
        
        assertEquals("Phone number is already registered", exception.getMessage());
        verify(providerRepository).existsByEmail(validRequest.getEmail());
        verify(providerRepository).existsByPhoneNumber(validRequest.getPhoneNumber());
        verify(providerRepository, never()).save(any(Provider.class));
    }
    
    @Test
    void registerProvider_DuplicateLicenseNumber() {
        // Arrange
        when(providerRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
        when(providerRepository.existsByPhoneNumber(validRequest.getPhoneNumber())).thenReturn(false);
        when(providerRepository.existsByLicenseNumber(validRequest.getLicenseNumber())).thenReturn(true);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> providerService.registerProvider(validRequest)
        );
        
        assertEquals("License number is already registered", exception.getMessage());
        verify(providerRepository).existsByEmail(validRequest.getEmail());
        verify(providerRepository).existsByPhoneNumber(validRequest.getPhoneNumber());
        verify(providerRepository).existsByLicenseNumber(validRequest.getLicenseNumber());
        verify(providerRepository, never()).save(any(Provider.class));
    }
    
    @Test
    void registerProvider_PasswordMismatch() {
        // Arrange
        validRequest.setConfirmPassword("DifferentPassword123!");
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> providerService.registerProvider(validRequest)
        );
        
        assertEquals("Password and confirmation password do not match", exception.getMessage());
        verify(providerRepository, never()).save(any(Provider.class));
    }
    
    @Test
    void registerProvider_InvalidPassword() {
        // Arrange
        when(passwordService.isValidPassword(validRequest.getPassword())).thenReturn(false);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> providerService.registerProvider(validRequest)
        );
        
        assertEquals("Password does not meet security requirements", exception.getMessage());
        verify(passwordService).isValidPassword(validRequest.getPassword());
        verify(providerRepository, never()).save(any(Provider.class));
    }
    
    @Test
    void registerProvider_InvalidSpecialization() {
        // Arrange
        validRequest.setSpecialization("InvalidSpecialization");
        when(providerRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
        when(providerRepository.existsByPhoneNumber(validRequest.getPhoneNumber())).thenReturn(false);
        when(providerRepository.existsByLicenseNumber(validRequest.getLicenseNumber())).thenReturn(false);
        when(passwordService.isValidPassword(validRequest.getPassword())).thenReturn(true);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> providerService.registerProvider(validRequest)
        );
        
        assertEquals("Invalid specialization. Please choose from the predefined list.", exception.getMessage());
        verify(providerRepository, never()).save(any(Provider.class));
    }
    
    @Test
    void registerProvider_ValidSpecializations() {
        // Test that all predefined specializations are valid
        String[] validSpecializations = {
            "Cardiology", "Dermatology", "Endocrinology", "Gastroenterology", 
            "General Practice", "Gynecology", "Hematology", "Infectious Disease",
            "Internal Medicine", "Nephrology", "Neurology", "Oncology", 
            "Ophthalmology", "Orthopedics", "Otolaryngology", "Pathology",
            "Pediatrics", "Psychiatry", "Pulmonology", "Radiology", 
            "Rheumatology", "Surgery", "Urology", "Emergency Medicine",
            "Family Medicine", "Geriatrics", "Obstetrics", "Physical Medicine",
            "Preventive Medicine", "Sports Medicine"
        };
        
        for (String specialization : validSpecializations) {
            validRequest.setSpecialization(specialization);
            when(providerRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
            when(providerRepository.existsByPhoneNumber(validRequest.getPhoneNumber())).thenReturn(false);
            when(providerRepository.existsByLicenseNumber(validRequest.getLicenseNumber())).thenReturn(false);
            when(passwordService.isValidPassword(validRequest.getPassword())).thenReturn(true);
            when(passwordService.hashPassword(validRequest.getPassword())).thenReturn("hashedPassword");
            when(providerRepository.save(any(Provider.class))).thenReturn(savedProvider);
            
            // Should not throw exception for valid specializations
            assertDoesNotThrow(() -> providerService.registerProvider(validRequest));
            
            // Reset mocks for next iteration
            reset(providerRepository, passwordService);
        }
    }
    
    @Test
    void getValidSpecializations() {
        // Act
        var specializations = providerService.getValidSpecializations();
        
        // Assert
        assertNotNull(specializations);
        assertTrue(specializations.contains("Cardiology"));
        assertTrue(specializations.contains("Dermatology"));
        assertTrue(specializations.contains("Emergency Medicine"));
        assertFalse(specializations.contains("InvalidSpecialization"));
    }
    
    @Test
    void getProviderById_Success() {
        // Arrange
        UUID providerId = UUID.randomUUID();
        when(providerRepository.findById(providerId)).thenReturn(Optional.of(savedProvider));
        
        // Act
        Provider result = providerService.getProviderById(providerId);
        
        // Assert
        assertNotNull(result);
        assertEquals(savedProvider.getId(), result.getId());
        verify(providerRepository).findById(providerId);
    }
    
    @Test
    void getProviderById_NotFound() {
        // Arrange
        UUID providerId = UUID.randomUUID();
        when(providerRepository.findById(providerId)).thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> providerService.getProviderById(providerId)
        );
        
        assertEquals("Provider not found with ID: " + providerId, exception.getMessage());
        verify(providerRepository).findById(providerId);
    }
} 