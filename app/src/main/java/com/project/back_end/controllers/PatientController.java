package com.project.back_end.controllers;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Patient;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.Service; // Shared validation service
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/patient")
public class PatientController {

    private final PatientService patientService;
    private final Service validationService;

    // Constructor Injection
    public PatientController(PatientService patientService, Service validationService) {
        this.patientService = patientService;
        this.validationService = validationService;
    }

    /**
     * Retrieves the details of the logged-in patient.
     */
    @GetMapping("/{token}")
    public ResponseEntity<Map<String, Object>> getPatientDetails(@PathVariable String token) {
        // Validate token for the "patient" role
        Map<String, String> tokenErrors = validationService.validateToken(token, "patient");
        if (!tokenErrors.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>(tokenErrors);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        return patientService.getPatientDetails(token);
    }

    /**
     * Registers a new patient.
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> createPatient(@RequestBody Patient patient) {
        Map<String, String> response = new HashMap<>();

        // Check if a patient with this email or phone already exists
        boolean isNewPatient = validationService.validatePatient(patient);

        if (!isNewPatient) {
            response.put("message", "Patient with email id or phone no already exist");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        // Proceed to save the new patient
        int saveStatus = patientService.createPatient(patient);
        if (saveStatus == 1) {
            response.put("message", "Signup successful");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Handles patient login validation.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Login login) {
        return validationService.validatePatientLogin(login);
    }

    /**
     * Retrieves all appointments for a specific patient.
     */
    @GetMapping("/{id}/{token}")
    public ResponseEntity<Map<String, Object>> getPatientAppointment(
            @PathVariable Long id,
            @PathVariable String token) {

        // Validate token for the "patient" role
        Map<String, String> tokenErrors = validationService.validateToken(token, "patient");
        if (!tokenErrors.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>(tokenErrors);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        return patientService.getPatientAppointment(id, token);
    }

    /**
     * Filters a patient's appointments based on condition (past/future) and doctor
     * name.
     */
    @GetMapping("/filter/{condition}/{name}/{token}")
    public ResponseEntity<Map<String, Object>> filterPatientAppointment(
            @PathVariable String condition,
            @PathVariable String name,
            @PathVariable String token) {

        // Validate token for the "patient" role
        Map<String, String> tokenErrors = validationService.validateToken(token, "patient");
        if (!tokenErrors.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>(tokenErrors);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        // Handle string "null" from frontend paths to prevent mapping errors
        String filteredCondition = "null".equalsIgnoreCase(condition) ? null : condition;
        String filteredName = "null".equalsIgnoreCase(name) ? null : name;

        return validationService.filterPatient(filteredCondition, filteredName, token);
    }
}