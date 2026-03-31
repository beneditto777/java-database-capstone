package com.project.back_end.controllers;

import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.Service; // Adjust import if your Service class is elsewhere
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final Service validationService; // Using 'validationService' for clarity

    // Constructor Injection
    public AppointmentController(AppointmentService appointmentService, Service validationService) {
        this.appointmentService = appointmentService;
        this.validationService = validationService;
    }

    /**
     * Retrieves appointments for a specific date and patient name.
     * Restricted to users with the "doctor" role.
     */
    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<Map<String, Object>> getAppointments(
            @PathVariable String date,
            @PathVariable String patientName,
            @PathVariable String token) {

        // Validate that the request is coming from a Doctor
        Map<String, String> tokenErrors = validationService.validateToken(token, "doctor");
        if (!tokenErrors.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>(tokenErrors);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        LocalDate parsedDate = LocalDate.parse(date);

        // Handle cases where the frontend sends "null" as a literal string
        String filteredPatientName = "null".equalsIgnoreCase(patientName) ? null : patientName;

        Map<String, Object> response = appointmentService.getAppointment(filteredPatientName, parsedDate, token);
        return ResponseEntity.ok(response);
    }

    /**
     * Books a new appointment.
     * Restricted to users with the "patient" role.
     */
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> bookAppointment(
            @PathVariable String token,
            @RequestBody Appointment appointment) {

        Map<String, String> response = new HashMap<>();

        // Validate that the request is coming from a Patient
        Map<String, String> tokenErrors = validationService.validateToken(token, "patient");
        if (!tokenErrors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(tokenErrors);
        }

        // Validate doctor availability and appointment timing
        int validationStatus = validationService.validateAppointment(appointment);
        if (validationStatus == -1) {
            response.put("message", "Invalid Doctor ID.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else if (validationStatus == 0) {
            response.put("message", "The requested time slot is no longer available.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        // Proceed to book
        int bookingStatus = appointmentService.bookAppointment(appointment);
        if (bookingStatus == 1) {
            response.put("message", "Appointment booked successfully.");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            response.put("message", "Failed to book appointment. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Updates an existing appointment.
     * Restricted to users with the "patient" role.
     */
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateAppointment(
            @PathVariable String token,
            @RequestBody Appointment appointment) {

        // Validate that the request is coming from a Patient
        Map<String, String> tokenErrors = validationService.validateToken(token, "patient");
        if (!tokenErrors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(tokenErrors);
        }

        return appointmentService.updateAppointment(appointment);
    }

    /**
     * Cancels an existing appointment.
     * Restricted to users with the "patient" role.
     */
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> cancelAppointment(
            @PathVariable long id,
            @PathVariable String token) {

        // Validate that the request is coming from a Patient
        Map<String, String> tokenErrors = validationService.validateToken(token, "patient");
        if (!tokenErrors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(tokenErrors);
        }

        return appointmentService.cancelAppointment(id, token);
    }
}