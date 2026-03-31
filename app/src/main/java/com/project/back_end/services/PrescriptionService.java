package com.project.back_end.services;

import com.project.back_end.models.Prescription;
import com.project.back_end.repo.PrescriptionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    // Constructor Injection for Dependencies
    public PrescriptionService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    /**
     * Saves a new prescription to the database.
     * Checks if a prescription already exists for the given appointment to prevent duplicates.
     */
    public ResponseEntity<Map<String, String>> savePrescription(Prescription prescription) {
        Map<String, String> response = new HashMap<>();
        try {
            // Check if a prescription for this appointment already exists
            List<Prescription> existingPrescriptions = prescriptionRepository.findByAppointmentId(prescription.getAppointmentId());
            
            if (existingPrescriptions != null && !existingPrescriptions.isEmpty()) {
                response.put("message", "A prescription already exists for this appointment.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Save the new prescription
            prescriptionRepository.save(prescription);
            response.put("message", "Prescription saved successfully.");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            System.err.println("Error saving prescription: " + e.getMessage());
            response.put("message", "An internal error occurred while saving the prescription.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Retrieves the prescription associated with a specific appointment ID.
     */
    public ResponseEntity<Map<String, Object>> getPrescription(Long appointmentId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Prescription> prescriptions = prescriptionRepository.findByAppointmentId(appointmentId);
            
            if (prescriptions != null && !prescriptions.isEmpty()) {
                // Assuming a 1-to-1 relationship, we return the first prescription found
                response.put("prescription", prescriptions.get(0));
            } else {
                response.put("prescription", null);
                response.put("message", "No prescription found for this appointment.");
            }
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error fetching prescription: " + e.getMessage());
            response.put("message", "An internal error occurred while fetching the prescription.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}