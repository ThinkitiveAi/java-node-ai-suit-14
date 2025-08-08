package com.healthfirst.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthfirst.dto.ClinicAddressDto;
import com.healthfirst.dto.ProviderRegistrationRequest;
import com.healthfirst.entity.Provider;
import com.healthfirst.entity.VerificationStatus;
import com.healthfirst.repository.ProviderRepository;
import com.healthfirst.service.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class ProviderControllerIntegrationTest {
    
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired
    private ProviderRepository providerRepository;
    
    @Autowired
    private PasswordService passwordService;
    
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
    }
    
    @Test
    void registerProvider_Success() throws Exception {
        // Arrange
        ClinicAddressDto address = new ClinicAddressDto(
            "123 Medical Center Dr",
            "New York",
            "NY",
            "10001"
        );
        
        ProviderRegistrationRequest request = new ProviderRegistrationRequest(
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
        
        // Act & Assert
        mockMvc.perform(post("/provider/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Provider registered successfully. Verification email sent."))
                .andExpect(jsonPath("$.data.providerId").exists())
                .andExpect(jsonPath("$.data.email").value("john.smith@clinic.com"))
                .andExpect(jsonPath("$.data.verificationStatus").value("PENDING"));
    }
    
    @Test
    void registerProvider_InvalidSpecialization() throws Exception {
        // Arrange
        ClinicAddressDto address = new ClinicAddressDto(
            "123 Medical Center Dr",
            "New York",
            "NY",
            "10001"
        );
        
        ProviderRegistrationRequest request = new ProviderRegistrationRequest(
            "Dr. John",
            "Smith",
            "john.smith@clinic.com",
            "+1234567890",
            "SecurePassword123!",
            "SecurePassword123!",
            "InvalidSpecialization",
            "MD123456789",
            15,
            address
        );
        
        // Act & Assert
        mockMvc.perform(post("/provider/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid specialization. Please choose from the predefined list."));
    }
    
    @Test
    void registerProvider_InvalidLicenseNumber() throws Exception {
        // Arrange
        ClinicAddressDto address = new ClinicAddressDto(
            "123 Medical Center Dr",
            "New York",
            "NY",
            "10001"
        );
        
        ProviderRegistrationRequest request = new ProviderRegistrationRequest(
            "Dr. John",
            "Smith",
            "john.smith@clinic.com",
            "+1234567890",
            "SecurePassword123!",
            "SecurePassword123!",
            "Cardiology",
            "MD123-456-789", // Invalid: contains hyphens
            15,
            address
        );
        
        // Act & Assert
        mockMvc.perform(post("/provider/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("License number must be alphanumeric"));
    }
    
    @Test
    void registerProvider_InvalidZipCode() throws Exception {
        // Arrange
        ClinicAddressDto address = new ClinicAddressDto(
            "123 Medical Center Dr",
            "New York",
            "NY",
            "1000" // Invalid: too short
        );
        
        ProviderRegistrationRequest request = new ProviderRegistrationRequest(
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
        
        // Act & Assert
        mockMvc.perform(post("/provider/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("ZIP code must be in valid format (e.g., 12345 or 12345-6789)"));
    }
    
    @Test
    void registerProvider_ValidZipCodeFormats() throws Exception {
        // Test both valid ZIP code formats
        String[] validZipCodes = {"10001", "10001-1234"};
        
        for (String zipCode : validZipCodes) {
            ClinicAddressDto address = new ClinicAddressDto(
                "123 Medical Center Dr",
                "New York",
                "NY",
                zipCode
            );
            
            ProviderRegistrationRequest request = new ProviderRegistrationRequest(
                "Dr. John",
                "Smith",
                "john.smith" + UUID.randomUUID() + "@clinic.com", // Unique email
                "+1" + UUID.randomUUID().toString().substring(0, 10), // Unique phone
                "SecurePassword123!",
                "SecurePassword123!",
                "Cardiology",
                "MD" + UUID.randomUUID().toString().substring(0, 9), // Unique license
                15,
                address
            );
            
            // Should succeed for valid ZIP codes
            mockMvc.perform(post("/provider/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
    
    @Test
    void registerProvider_YearsOfExperienceRange() throws Exception {
        // Test valid years of experience range (0-50)
        Integer[] validYears = {0, 25, 50};
        
        for (Integer years : validYears) {
            ClinicAddressDto address = new ClinicAddressDto(
                "123 Medical Center Dr",
                "New York",
                "NY",
                "10001"
            );
            
            ProviderRegistrationRequest request = new ProviderRegistrationRequest(
                "Dr. John",
                "Smith",
                "john.smith" + UUID.randomUUID() + "@clinic.com",
                "+1" + UUID.randomUUID().toString().substring(0, 10),
                "SecurePassword123!",
                "SecurePassword123!",
                "Cardiology",
                "MD" + UUID.randomUUID().toString().substring(0, 9),
                years,
                address
            );
            
            // Should succeed for valid years of experience
            mockMvc.perform(post("/provider/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
    
    @Test
    void registerProvider_InvalidYearsOfExperience() throws Exception {
        // Test invalid years of experience
        ClinicAddressDto address = new ClinicAddressDto(
            "123 Medical Center Dr",
            "New York",
            "NY",
            "10001"
        );
        
        ProviderRegistrationRequest request = new ProviderRegistrationRequest(
            "Dr. John",
            "Smith",
            "john.smith@clinic.com",
            "+1234567890",
            "SecurePassword123!",
            "SecurePassword123!",
            "Cardiology",
            "MD123456789",
            51, // Invalid: exceeds maximum
            address
        );
        
        // Act & Assert
        mockMvc.perform(post("/provider/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Years of experience cannot exceed 50"));
    }
    
    @Test
    void getSpecializations_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/provider/specializations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.hasItems("Cardiology", "Dermatology", "Emergency Medicine")));
    }
    
    @Test
    void healthCheck_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/provider/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Provider service is running"));
    }
} 