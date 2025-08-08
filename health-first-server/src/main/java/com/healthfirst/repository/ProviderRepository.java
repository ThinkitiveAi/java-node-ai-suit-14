package com.healthfirst.repository;

import com.healthfirst.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, UUID> {
    
    /**
     * Find provider by email
     */
    Optional<Provider> findByEmail(String email);
    
    /**
     * Find provider by phone number
     */
    Optional<Provider> findByPhoneNumber(String phoneNumber);
    
    /**
     * Find provider by license number
     */
    Optional<Provider> findByLicenseNumber(String licenseNumber);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Check if phone number exists
     */
    boolean existsByPhoneNumber(String phoneNumber);
    
    /**
     * Check if license number exists
     */
    boolean existsByLicenseNumber(String licenseNumber);
    
    /**
     * Find provider by email and is active
     */
    Optional<Provider> findByEmailAndIsActiveTrue(String email);
    
    /**
     * Find provider by phone number and is active
     */
    Optional<Provider> findByPhoneNumberAndIsActiveTrue(String phoneNumber);
    
    /**
     * Find provider by license number and is active
     */
    Optional<Provider> findByLicenseNumberAndIsActiveTrue(String licenseNumber);
    
    /**
     * Custom query to find providers by verification status
     */
    @Query("SELECT p FROM Provider p WHERE p.verificationStatus = :status AND p.isActive = true")
    java.util.List<Provider> findByVerificationStatus(@Param("status") com.healthfirst.entity.VerificationStatus status);
} 