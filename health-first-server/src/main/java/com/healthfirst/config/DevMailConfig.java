package com.healthfirst.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class DevMailConfig {
    @Bean
    public JavaMailSender javaMailSender() {
        return new JavaMailSenderImpl(); // No-op sender for dev
    }
} 