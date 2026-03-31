package com.project.back_end.services;

import com.project.back_end.models.Admin;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class TokenService {

    @Value("${jwt.secret}")
    private String secret;

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public TokenService(AdminRepository adminRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    private SecretKey getSigningKey() {
        // Modern 0.12.x way to create a secure key
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String identifier) {
        long sevenDaysInMillis = 7L * 24 * 60 * 60 * 1000;

        // Modern 0.12.x builder syntax (no "set" prefixes)
        return Jwts.builder()
                .subject(identifier)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + sevenDaysInMillis))
                .signWith(getSigningKey())
                .compact();
    }

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

    public String extractIdentifier(String token) {
        // Modern 0.12.x parsing syntax
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String extractEmail(String token) {
        return extractIdentifier(token);
    }

    public Long extractId(String token) {
        String identifier = extractIdentifier(token);

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
            return false;
        }
    }
}