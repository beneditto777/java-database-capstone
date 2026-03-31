package com.project.back_end.controllers;

import com.project.back_end.models.Prescription;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.PrescriptionService;
import com.project.back_end.services.Service; // Shared validation service
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.path}prescription")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final Service validationService;
    private final AppointmentService appointmentService;

    // Constructor Injection
    public PrescriptionController(PrescriptionService prescriptionService,
            Service validationService,
            AppointmentService appointmentService) {
        this.prescriptionService = prescriptionService;
        this.validationService = validationService;
        this.appointmentService = appointmentService;
    }

    /**
     * Saves a new prescription and updates the appointment status.
     * Restricted to users with the "doctor" role.
     */
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> savePrescription(
            @PathVariable String token,
            @RequestBody Prescription prescription) {

        // Validate token for the "doctor" role
        Map<String, String> tokenErrors = validationService.validateToken(token, "doctor");
        if (!tokenErrors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(tokenErrors);
        }

        // Change the appointment status to indicate it has been completed/prescribed
        // Assuming '1' represents a completed status based on previous service logic
        appointmentService.changeStatus(1, prescription.getAppointmentId());

        // Save the prescription to MongoDB
        return prescriptionService.savePrescription(prescription);
    }

    /**
     * Retrieves a prescription by its associated appointment ID.
     * Restricted to users with the "doctor" role.
     */
    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<Map<String, Object>> getPrescription(
            @PathVariable Long appointmentId,
            @PathVariable String token) {

        // Validate token for the "doctor" role
        Map<String, String> tokenErrors = validationService.validateToken(token, "doctor");
        if (!tokenErrors.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>(tokenErrors);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        // Retrieve the prescription
        return prescriptionService.getPrescription(appointmentId);
    }
}