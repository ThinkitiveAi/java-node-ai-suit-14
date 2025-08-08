package com.healthfirst.controller;

import com.healthfirst.dto.ApiResponse;
import com.healthfirst.dto.PatientLoginRequest;
import com.healthfirst.dto.PatientLoginResponse;
import com.healthfirst.dto.PatientRegistrationRequest;
import com.healthfirst.dto.PatientRegistrationResponse;
import com.healthfirst.service.AuthService;
import com.healthfirst.service.PatientService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/patient")
@CrossOrigin(origins = "*")
public class PatientController {

    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);

    private final PatientService patientService;
    private final AuthService authService;

    @Autowired
    public PatientController(PatientService patientService, AuthService authService) {
        this.patientService = patientService;
        this.authService = authService;
    }

    /**
     * Register a new patient
     * POST /api/v1/patient/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<PatientRegistrationResponse>> registerPatient(
            @Valid @RequestBody PatientRegistrationRequest request) {

        logger.info("Received patient registration request for email: {}", request.getEmail());

        try {
            PatientRegistrationResponse response = patientService.registerPatient(request);

            ApiResponse<PatientRegistrationResponse> apiResponse = ApiResponse.success(
                    "Patient registered successfully. Verification email sent.",
                    response
            );

            logger.info("Patient registration successful for email: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);

        } catch (IllegalArgumentException e) {
            logger.warn("Patient registration failed for email: {} - {}", request.getEmail(), e.getMessage());
            ApiResponse<PatientRegistrationResponse> apiResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);

        } catch (Exception e) {
            logger.error("Unexpected error during patient registration for email: {}", request.getEmail(), e);
            ApiResponse<PatientRegistrationResponse> apiResponse = ApiResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    /**
     * Login patient
     * POST /api/v1/patient/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<PatientLoginResponse>> loginPatient(
            @Valid @RequestBody PatientLoginRequest request) {

        logger.info("Received patient login request for email: {}", request.getEmail());

        try {
            PatientLoginResponse response = authService.authenticatePatient(request);

            ApiResponse<PatientLoginResponse> apiResponse = ApiResponse.success(
                    "Login successful",
                    response
            );

            logger.info("Patient login successful for email: {}", request.getEmail());
            return ResponseEntity.ok(apiResponse);

        } catch (IllegalArgumentException e) {
            logger.warn("Patient login failed for email: {} - {}", request.getEmail(), e.getMessage());
            ApiResponse<PatientLoginResponse> apiResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);

        } catch (Exception e) {
            logger.error("Unexpected error during patient login for email: {}", request.getEmail(), e);
            ApiResponse<PatientLoginResponse> apiResponse = ApiResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    /**
     * Get patient by ID
     * GET /api/v1/patient/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> getPatientById(@PathVariable UUID id) {
        try {
            var patient = patientService.getPatientById(id);
            ApiResponse<Object> apiResponse = ApiResponse.success("Patient retrieved successfully", patient);
            return ResponseEntity.ok(apiResponse);
        } catch (IllegalArgumentException e) {
            ApiResponse<Object> apiResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
        } catch (Exception e) {
            logger.error("Error retrieving patient with ID: {}", id, e);
            ApiResponse<Object> apiResponse = ApiResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    /**
     * Get patient by email
     * GET /api/v1/patient/email/{email}
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<Object>> getPatientByEmail(@PathVariable String email) {
        try {
            var patient = patientService.getPatientByEmail(email);
            ApiResponse<Object> apiResponse = ApiResponse.success("Patient retrieved successfully", patient);
            return ResponseEntity.ok(apiResponse);
        } catch (IllegalArgumentException e) {
            ApiResponse<Object> apiResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
        } catch (Exception e) {
            logger.error("Error retrieving patient with email: {}", email, e);
            ApiResponse<Object> apiResponse = ApiResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    /**
     * Get patient by SSN
     * GET /api/v1/patient/ssn/{ssn}
     */
    @GetMapping("/ssn/{ssn}")
    public ResponseEntity<ApiResponse<Object>> getPatientBySsn(@PathVariable String ssn) {
        try {
            var patient = patientService.getPatientBySsn(ssn);
            ApiResponse<Object> apiResponse = ApiResponse.success("Patient retrieved successfully", patient);
            return ResponseEntity.ok(apiResponse);
        } catch (IllegalArgumentException e) {
            ApiResponse<Object> apiResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
        } catch (Exception e) {
            logger.error("Error retrieving patient with SSN: {}", ssn, e);
            ApiResponse<Object> apiResponse = ApiResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    /**
     * Update patient verification status
     * PUT /api/v1/patient/{id}/verification-status
     */
    @PutMapping("/{id}/verification-status")
    public ResponseEntity<ApiResponse<String>> updateVerificationStatus(
            @PathVariable UUID id,
            @RequestParam String status) {
        try {
            var verificationStatus = com.healthfirst.entity.VerificationStatus.valueOf(status.toUpperCase());
            patientService.updateVerificationStatus(id, verificationStatus);
            ApiResponse<String> apiResponse = ApiResponse.success("Verification status updated successfully");
            return ResponseEntity.ok(apiResponse);
        } catch (IllegalArgumentException e) {
            ApiResponse<String> apiResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        } catch (Exception e) {
            logger.error("Error updating verification status for patient ID: {}", id, e);
            ApiResponse<String> apiResponse = ApiResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    /**
     * Get patients by verification status
     * GET /api/v1/patient/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<Object>>> getPatientsByStatus(@PathVariable String status) {
        try {
            var verificationStatus = com.healthfirst.entity.VerificationStatus.valueOf(status.toUpperCase());
            var patients = patientService.getPatientsByVerificationStatus(verificationStatus);
            ApiResponse<List<Object>> apiResponse = ApiResponse.success("Patients retrieved successfully", (List<Object>) (List<?>) patients);
            return ResponseEntity.ok(apiResponse);
        } catch (IllegalArgumentException e) {
            ApiResponse<List<Object>> apiResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        } catch (Exception e) {
            logger.error("Error retrieving patients with status: {}", status, e);
            ApiResponse<List<Object>> apiResponse = ApiResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    /**
     * Get patients by gender
     * GET /api/v1/patient/gender/{gender}
     */
    @GetMapping("/gender/{gender}")
    public ResponseEntity<ApiResponse<List<Object>>> getPatientsByGender(@PathVariable String gender) {
        try {
            var patients = patientService.getPatientsByGender(gender);
            ApiResponse<List<Object>> apiResponse = ApiResponse.success("Patients retrieved successfully", (List<Object>) (List<?>) patients);
            return ResponseEntity.ok(apiResponse);
        } catch (IllegalArgumentException e) {
            ApiResponse<List<Object>> apiResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        } catch (Exception e) {
            logger.error("Error retrieving patients by gender: {}", gender, e);
            ApiResponse<List<Object>> apiResponse = ApiResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    /**
     * Get patients by blood type
     * GET /api/v1/patient/blood-type/{bloodType}
     */
    @GetMapping("/blood-type/{bloodType}")
    public ResponseEntity<ApiResponse<List<Object>>> getPatientsByBloodType(@PathVariable String bloodType) {
        try {
            var patients = patientService.getPatientsByBloodType(bloodType);
            ApiResponse<List<Object>> apiResponse = ApiResponse.success("Patients retrieved successfully", (List<Object>) (List<?>) patients);
            return ResponseEntity.ok(apiResponse);
        } catch (IllegalArgumentException e) {
            ApiResponse<List<Object>> apiResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        } catch (Exception e) {
            logger.error("Error retrieving patients by blood type: {}", bloodType, e);
            ApiResponse<List<Object>> apiResponse = ApiResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    /**
     * Get patients by age range
     * GET /api/v1/patient/age-range?minAge={minAge}&maxAge={maxAge}
     */
    @GetMapping("/age-range")
    public ResponseEntity<ApiResponse<List<Object>>> getPatientsByAgeRange(
            @RequestParam int minAge,
            @RequestParam int maxAge) {
        try {
            var patients = patientService.getPatientsByAgeRange(minAge, maxAge);
            ApiResponse<List<Object>> apiResponse = ApiResponse.success("Patients retrieved successfully", (List<Object>) (List<?>) patients);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            logger.error("Error retrieving patients by age range: {}-{}", minAge, maxAge, e);
            ApiResponse<List<Object>> apiResponse = ApiResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    /**
     * Get patients by city
     * GET /api/v1/patient/city/{city}
     */
    @GetMapping("/city/{city}")
    public ResponseEntity<ApiResponse<List<Object>>> getPatientsByCity(@PathVariable String city) {
        try {
            var patients = patientService.getPatientsByCity(city);
            ApiResponse<List<Object>> apiResponse = ApiResponse.success("Patients retrieved successfully", (List<Object>) (List<?>) patients);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            logger.error("Error retrieving patients by city: {}", city, e);
            ApiResponse<List<Object>> apiResponse = ApiResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    /**
     * Get patients by state
     * GET /api/v1/patient/state/{state}
     */
    @GetMapping("/state/{state}")
    public ResponseEntity<ApiResponse<List<Object>>> getPatientsByState(@PathVariable String state) {
        try {
            var patients = patientService.getPatientsByState(state);
            ApiResponse<List<Object>> apiResponse = ApiResponse.success("Patients retrieved successfully", (List<Object>) (List<?>) patients);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            logger.error("Error retrieving patients by state: {}", state, e);
            ApiResponse<List<Object>> apiResponse = ApiResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    /**
     * Get valid blood types
     * GET /api/v1/patient/blood-types
     */
    @GetMapping("/blood-types")
    public ResponseEntity<ApiResponse<List<String>>> getBloodTypes() {
        try {
            List<String> bloodTypes = patientService.getValidBloodTypes();
            ApiResponse<List<String>> apiResponse = ApiResponse.success("Blood types retrieved successfully", bloodTypes);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            logger.error("Error retrieving blood types", e);
            ApiResponse<List<String>> apiResponse = ApiResponse.error("Failed to retrieve blood types");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    /**
     * Get valid genders
     * GET /api/v1/patient/genders
     */
    @GetMapping("/genders")
    public ResponseEntity<ApiResponse<List<String>>> getGenders() {
        try {
            List<String> genders = patientService.getValidGenders();
            ApiResponse<List<String>> apiResponse = ApiResponse.success("Genders retrieved successfully", genders);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            logger.error("Error retrieving genders", e);
            ApiResponse<List<String>> apiResponse = ApiResponse.error("Failed to retrieve genders");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    /**
     * Health check endpoint
     * GET /api/v1/patient/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        ApiResponse<String> apiResponse = ApiResponse.success("Patient service is running");
        return ResponseEntity.ok(apiResponse);
    }
} 