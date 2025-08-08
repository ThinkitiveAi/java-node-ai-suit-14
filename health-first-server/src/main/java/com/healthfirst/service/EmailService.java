package com.healthfirst.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    private final JavaMailSender mailSender;
    
    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    /**
     * Send verification email to provider
     */
    public void sendVerificationEmail(String to, String providerName, String verificationToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Provider Registration Verification");
            message.setText(createVerificationEmailContent(providerName, verificationToken));
            
            mailSender.send(message);
            logger.info("Verification email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send verification email to: {}", to, e);
            // In a production environment, you might want to queue this for retry
            // or use a more robust email service
        }
    }
    
    /**
     * Create verification email content
     */
    private String createVerificationEmailContent(String providerName, String verificationToken) {
        return String.format(
            "Dear %s,\n\n" +
            "Thank you for registering as a healthcare provider with Health First.\n\n" +
            "Your account is currently pending verification. Our team will review your application " +
            "and contact you within 2-3 business days.\n\n" +
            "If you have any questions, please contact our support team.\n\n" +
            "Best regards,\n" +
            "Health First Team",
            providerName
        );
    }
    
    /**
     * Send account verification status update
     */
    public void sendVerificationStatusUpdate(String to, String providerName, String status) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Provider Verification Status Update");
            message.setText(createStatusUpdateEmailContent(providerName, status));
            
            mailSender.send(message);
            logger.info("Status update email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send status update email to: {}", to, e);
        }
    }
    
    /**
     * Create status update email content
     */
    private String createStatusUpdateEmailContent(String providerName, String status) {
        String statusMessage;
        if ("VERIFIED".equals(status)) {
            statusMessage = "Your account has been verified successfully. You can now log in and start using our platform.";
        } else if ("REJECTED".equals(status)) {
            statusMessage = "Your account verification has been rejected. Please contact our support team for more information.";
        } else {
            statusMessage = "Your account verification is still pending. We will notify you once the review is complete.";
        }
        
        return String.format(
            "Dear %s,\n\n" +
            "Your provider verification status has been updated.\n\n" +
            "Status: %s\n\n" +
            "%s\n\n" +
            "If you have the questions, please contact our support team.\n\n" +
            "Best regards,\n" +
            "Health First Team",
            providerName, status, statusMessage
        );
    }
} 