package com.project.back_end.services;

import com.project.back_end.models.Admin;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class TokenService {

    // Injects the secret key defined in application.properties
    @Value("${jwt.secret}")
    private String secret;

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    // Constructor Injection
    public TokenService(AdminRepository adminRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    /**
     * Retrieves the signing key used for JWT token signing.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a JWT token for a given user's identifier (email or username).
     * Expiration is set to 7 days.
     */
    public String generateToken(String identifier) {
        long sevenDaysInMillis = 7L * 24 * 60 * 60 * 1000;

        return Jwts.builder()
                .setSubject(identifier)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + sevenDaysInMillis))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Helper Overload: Generates a token specifically using an ID and Role if
     * needed by other services.
     * We map the ID back to their email/username to keep the token subject
     * consistent.
     */
    public String generateToken(Long id, String role) {
        String identifier = "";
        if ("admin".equalsIgnoreCase(role)) {
            Admin admin = adminRepository.findById(id).orElse(null);
            if (admin != null)
                identifier = admin.getUsername();
        } else if ("doctor".equalsIgnoreCase(role)) {
            Doctor doctor = doctorRepository.findById(id).orElse(null);
            if (doctor != null)
                identifier = doctor.getEmail();
        } else {
            Patient patient = patientRepository.findById(id).orElse(null);
            if (patient != null)
                identifier = patient.getEmail();
        }
        return generateToken(identifier);
    }

    /**
     * Extracts the identifier (subject) from a JWT token.
     */
    public String extractIdentifier(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /**
     * Alias for extractIdentifier to match specific service calls.
     */
    public String extractEmail(String token) {
        return extractIdentifier(token);
    }

    /**
     * Helper Method: Extracts the identifier and maps it back to the database ID.
     * Crucial for Appointment checks.
     */
    public Long extractId(String token) {
        String identifier = extractIdentifier(token);

        // Try to find the user in any of the repositories to return their ID
        Patient patient = patientRepository.findByEmail(identifier);
        if (patient != null)
            return patient.getId();

        Doctor doctor = doctorRepository.findByEmail(identifier);
        if (doctor != null)
            return doctor.getId();

        Admin admin = adminRepository.findByUsername(identifier);
        if (admin != null)
            return admin.getId();

        return null;
    }

    /**
     * Validates the JWT token for a given user type.
     */
    public boolean validateToken(String token, String user) {
        try {
            String identifier = extractIdentifier(token);

            if ("admin".equalsIgnoreCase(user)) {
                return adminRepository.findByUsername(identifier) != null;
            } else if ("doctor".equalsIgnoreCase(user)) {
                return doctorRepository.findByEmail(identifier) != null;
            } else if ("patient".equalsIgnoreCase(user) || "loggedPatient".equalsIgnoreCase(user)) {
                return patientRepository.findByEmail(identifier) != null;
            }

            return false;
        } catch (Exception e) {
            // Token is expired, malformed, or tampered with
            System.err.println("Token validation failed: " + e.getMessage());
            return false;
        }
    }
}