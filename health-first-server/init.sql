-- Health First Database Initialization Script

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS health_first;
USE health_first;

-- Create providers table
CREATE TABLE IF NOT EXISTS providers (
    id CHAR(36) PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    license_number VARCHAR(50) UNIQUE NOT NULL,
    years_of_experience INTEGER,
    clinic_street VARCHAR(200) NOT NULL,
    clinic_city VARCHAR(100) NOT NULL,
    clinic_state VARCHAR(50) NOT NULL,
    clinic_zip VARCHAR(10) NOT NULL,
    verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_email (email),
    INDEX idx_phone_number (phone_number),
    INDEX idx_license_number (license_number),
    INDEX idx_verification_status (verification_status),
    INDEX idx_is_active (is_active)
);

-- Insert sample data for testing (optional)
-- INSERT INTO providers (id, first_name, last_name, email, phone_number, password_hash, specialization, license_number, years_of_experience, clinic_street, clinic_city, clinic_state, clinic_zip, verification_status) VALUES
-- ('550e8400-e29b-41d4-a716-446655440000', 'Dr. John', 'Smith', 'john.smith@clinic.com', '+1234567890', '$2a$12$hashedpassword', 'Cardiology', 'MD123456789', 15, '123 Medical Center Dr', 'New York', 'NY', '10001', 'VERIFIED'); 