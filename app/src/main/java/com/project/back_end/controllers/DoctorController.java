package com.project.back_end.controllers;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Doctor;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.Service; // Adjust this import if your Service class is elsewhere
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.path}doctor")
public class DoctorController {

    private final DoctorService doctorService;
    private final Service validationService; // Shared service for token validation and filtering

    // Constructor Injection
    public DoctorController(DoctorService doctorService, Service validationService) {
        this.doctorService = doctorService;
        this.validationService = validationService;
    }

    /**
     * Retrieves the available time slots for a specific doctor on a given date.
     */
    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(
            @PathVariable String user,
            @PathVariable Long doctorId,
            @PathVariable String date,
            @PathVariable String token) {

        // Validate token for the requesting user type
        Map<String, String> tokenErrors = validationService.validateToken(token, user);
        if (!tokenErrors.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>(tokenErrors);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        LocalDate parsedDate = LocalDate.parse(date);
        List<String> availableSlots = doctorService.getDoctorAvailability(doctorId, parsedDate);

        Map<String, Object> response = new HashMap<>();
        response.put("availability", availableSlots);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a list of all doctors.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDoctors() {
        List<Doctor> doctors = doctorService.getDoctors();
        Map<String, Object> response = new HashMap<>();
        response.put("doctors", doctors);
        return ResponseEntity.ok(response);
    }

    /**
     * Adds a new doctor to the database.
     * Restricted to users with the "admin" role.
     */
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> saveDoctor(
            @PathVariable String token,
            @RequestBody Doctor doctor) {

        Map<String, String> response = new HashMap<>();

        // Validate token for admin role
        Map<String, String> tokenErrors = validationService.validateToken(token, "admin");
        if (!tokenErrors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(tokenErrors);
        }

        int saveStatus = doctorService.saveDoctor(doctor);
        if (saveStatus == 1) {
            response.put("message", "Doctor added to db");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else if (saveStatus == -1) {
            response.put("message", "Doctor already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } else {
            response.put("message", "Some internal error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Handles doctor login validation.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> doctorLogin(@RequestBody Login login) {
        return doctorService.validateDoctor(login);
    }

    /**
     * Updates an existing doctor's details.
     * Restricted to users with the "admin" role.
     */
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateDoctor(
            @PathVariable String token,
            @RequestBody Doctor doctor) {

        Map<String, String> response = new HashMap<>();

        // Validate token for admin role
        Map<String, String> tokenErrors = validationService.validateToken(token, "admin");
        if (!tokenErrors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(tokenErrors);
        }

        int updateStatus = doctorService.updateDoctor(doctor);
        if (updateStatus == 1) {
            response.put("message", "Doctor updated");
            return ResponseEntity.ok(response);
        } else if (updateStatus == -1) {
            response.put("message", "Doctor not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            response.put("message", "Some internal error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Deletes a doctor and all their associated appointments.
     * Restricted to users with the "admin" role.
     */
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> deleteDoctor(
            @PathVariable long id,
            @PathVariable String token) {

        Map<String, String> response = new HashMap<>();

        // Validate token for admin role
        Map<String, String> tokenErrors = validationService.validateToken(token, "admin");
        if (!tokenErrors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(tokenErrors);
        }

        int deleteStatus = doctorService.deleteDoctor(id);
        if (deleteStatus == 1) {
            response.put("message", "Doctor deleted successfully");
            return ResponseEntity.ok(response);
        } else if (deleteStatus == -1) {
            response.put("message", "Doctor not found with id");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            response.put("message", "Some internal error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Filters doctors based on partial name, time (AM/PM), and specialty.
     * Send literal "null" as path variable for parameters you don't want to filter
     * by.
     */
    @GetMapping("/filter/{name}/{time}/{speciality}")
    public ResponseEntity<Map<String, Object>> filterDoctors(
            @PathVariable String name,
            @PathVariable String time,
            @PathVariable String speciality) {

        // Handle string "null" from frontend paths
        String filteredName = "null".equalsIgnoreCase(name) ? null : name;
        String filteredTime = "null".equalsIgnoreCase(time) ? null : time;
        String filteredSpeciality = "null".equalsIgnoreCase(speciality) ? null : speciality;

        Map<String, Object> filteredData = validationService.filterDoctor(filteredName, filteredSpeciality,
                filteredTime);
        return ResponseEntity.ok(filteredData);
    }
}