package com.healthfirst.repository;

import com.healthfirst.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {
    
    /**
     * Find patient by email
     */
    Optional<Patient> findByEmail(String email);
    
    /**
     * Find patient by phone number
     */
    Optional<Patient> findByPhoneNumber(String phoneNumber);
    
    /**
     * Find patient by SSN
     */
    Optional<Patient> findBySsn(String ssn);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Check if phone number exists
     */
    boolean existsByPhoneNumber(String phoneNumber);
    
    /**
     * Check if SSN exists
     */
    boolean existsBySsn(String ssn);
    
    /**
     * Find patient by email and is active
     */
    Optional<Patient> findByEmailAndIsActiveTrue(String email);
    
    /**
     * Find patient by phone number and is active
     */
    Optional<Patient> findByPhoneNumberAndIsActiveTrue(String phoneNumber);
    
    /**
     * Find patient by SSN and is active
     */
    Optional<Patient> findBySsnAndIsActiveTrue(String ssn);
    
    /**
     * Custom query to find patients by verification status
     */
    @Query("SELECT p FROM Patient p WHERE p.verificationStatus = :status AND p.isActive = true")
    List<Patient> findByVerificationStatus(@Param("status") com.healthfirst.entity.VerificationStatus status);
    
    /**
     * Find patients by gender
     */
    List<Patient> findByGenderAndIsActiveTrue(String gender);
    
    /**
     * Find patients by blood type
     */
    List<Patient> findByBloodTypeAndIsActiveTrue(String bloodType);
    
    /**
     * Find patients by age range (using date of birth)
     */
    @Query("SELECT p FROM Patient p WHERE p.dateOfBirth BETWEEN :startDate AND :endDate AND p.isActive = true")
    List<Patient> findByDateOfBirthBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * Find patients by city
     */
    @Query("SELECT p FROM Patient p WHERE p.address.city = :city AND p.isActive = true")
    List<Patient> findByAddressCity(@Param("city") String city);
    
    /**
     * Find patients by state
     */
    @Query("SELECT p FROM Patient p WHERE p.address.state = :state AND p.isActive = true")
    List<Patient> findByAddressState(@Param("state") String state);
} 