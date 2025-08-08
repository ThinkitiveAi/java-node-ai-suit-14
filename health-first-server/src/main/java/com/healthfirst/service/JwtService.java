package com.healthfirst.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    
    @Value("${security.jwt.secret}")
    private String jwtSecret;
    
    @Value("${security.jwt.expiration}")
    private long jwtExpiration;
    
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    public String generateToken(String providerId, String email, String role, String specialization) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("provider_id", providerId);
        claims.put("role", role);
        claims.put("specialization", specialization);
        
        return createToken(claims, email);
    }
    
    public String generatePatientToken(String patientId, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("patient_id", patientId);
        claims.put("role", role);
        
        return createPatientToken(claims, email);
    }
    
    private String createPatientToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        // Patient tokens expire in 30 minutes (1800000 milliseconds)
        Date expiryDate = new Date(now.getTime() + 1800000);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public String extractProviderId(String token) {
        return extractClaim(token, claims -> claims.get("provider_id", String.class));
    }
    
    public String extractPatientId(String token) {
        return extractClaim(token, claims -> claims.get("patient_id", String.class));
    }
    
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }
    
    public String extractSpecialization(String token) {
        return extractClaim(token, claims -> claims.get("specialization", String.class));
    }
    
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            logger.error("Error parsing JWT token: {}", e.getMessage());
            throw new JwtException("Invalid JWT token");
        }
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            logger.error("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            logger.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }
    
    public long getTokenExpirationTime() {
        return jwtExpiration;
    }
    
    public long getPatientTokenExpirationTime() {
        return 1800000; // 30 minutes in milliseconds
    }
} 