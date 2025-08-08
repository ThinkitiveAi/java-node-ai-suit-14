package com.healthfirst.service;

import com.healthfirst.entity.Patient;
import com.healthfirst.entity.Provider;
import com.healthfirst.repository.PatientRepository;
import com.healthfirst.repository.ProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    private final ProviderRepository providerRepository;
    private final PatientRepository patientRepository;
    
    @Autowired
    public CustomUserDetailsService(ProviderRepository providerRepository, PatientRepository patientRepository) {
        this.providerRepository = providerRepository;
        this.patientRepository = patientRepository;
    }
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Try to find provider first
        Optional<Provider> providerOpt = providerRepository.findByEmail(email);
        if (providerOpt.isPresent()) {
            Provider provider = providerOpt.get();
            return User.builder()
                    .username(provider.getEmail())
                    .password(provider.getPasswordHash())
                    .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_PROVIDER")))
                    .accountExpired(false)
                    .accountLocked(!provider.getIsActive())
                    .credentialsExpired(false)
                    .disabled(provider.getVerificationStatus().name().equals("REJECTED"))
                    .build();
        }
        
        // If not found as provider, try to find as patient
        Optional<Patient> patientOpt = patientRepository.findByEmail(email);
        if (patientOpt.isPresent()) {
            Patient patient = patientOpt.get();
            return User.builder()
                    .username(patient.getEmail())
                    .password(patient.getPasswordHash())
                    .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_PATIENT")))
                    .accountExpired(false)
                    .accountLocked(!patient.getIsActive())
                    .credentialsExpired(false)
                    .disabled(patient.getVerificationStatus().name().equals("REJECTED"))
                    .build();
        }
        
        throw new UsernameNotFoundException("User not found with email: " + email);
    }
} 