package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class Service { // Note: It is highly recommended to rename this class to something like
                       // ValidationService or CoreService to avoid confusion with the @Service
                       // annotation.

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    // Constructor Injection
    public Service(TokenService tokenService,
            AdminRepository adminRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository,
            DoctorService doctorService,
            PatientService patientService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    /**
     * Checks the validity of a token for a given user role.
     * Returns an empty map if valid, or a map with an error message if invalid.
     */
    public Map<String, String> validateToken(String token, String userRole) {
        Map<String, String> errors = new HashMap<>();

        if (token == null || token.isEmpty()) {
            errors.put("message", "Token is missing.");
            return errors;
        }

        boolean isValid = tokenService.validateToken(token, userRole);
        if (!isValid) {
            errors.put("message", "Invalid or expired token.");
        }

        return errors;
    }

    /**
     * Validates the login credentials of an admin.
     */
    public ResponseEntity<Map<String, String>> validateAdmin(Admin receivedAdmin) {
        Map<String, String> response = new HashMap<>();
        try {
            Admin admin = adminRepository.findByUsername(receivedAdmin.getUsername());

            if (admin != null && admin.getPassword().equals(receivedAdmin.getPassword())) {
                // Assuming Admin ID is 0 or 1, or generate token based on username depending on
                // your TokenService implementation
                String token = tokenService.generateToken(admin.getId(), "admin");
                response.put("token", token);
                return ResponseEntity.ok(response);
            }

            response.put("message", "Invalid admin credentials.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            response.put("message", "An error occurred during admin validation.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Filters doctors based on name, specialty, and available time (AM/PM).
     */
    public Map<String, Object> filterDoctor(String name, String specialty, String time) {
        boolean hasName = (name != null && !name.trim().isEmpty());
        boolean hasSpecialty = (specialty != null && !specialty.trim().isEmpty());
        boolean hasTime = (time != null && !time.trim().isEmpty());

        if (hasName && hasSpecialty && hasTime) {
            return doctorService.filterDoctorsByNameSpecilityandTime(name, specialty, time);
        } else if (hasName && hasTime) {
            return doctorService.filterDoctorByNameAndTime(name, time);
        } else if (hasName && hasSpecialty) {
            return doctorService.filterDoctorByNameAndSpecility(name, specialty);
        } else if (hasSpecialty && hasTime) {
            return doctorService.filterDoctorByTimeAndSpecility(specialty, time);
        } else if (hasName) {
            return doctorService.findDoctorByName(name);
        } else if (hasSpecialty) {
            return doctorService.filterDoctorBySpecility(specialty);
        } else if (hasTime) {
            return doctorService.filterDoctorsByTime(time);
        } else {
            // No filters provided, return all doctors
            List<Doctor> allDoctors = doctorService.getDoctors();
            return Map.of("doctors", allDoctors);
        }
    }

    /**
     * Validates whether an appointment time is available based on the doctor's
     * schedule.
     * Return: 1 (valid), 0 (unavailable), -1 (doctor not found)
     */
    public int validateAppointment(Appointment appointment) {
        if (appointment.getDoctor() == null || appointment.getDoctor().getId() == null) {
            return -1;
        }

        Long doctorId = appointment.getDoctor().getId();
        Optional<Doctor> optionalDoctor = doctorRepository.findById(doctorId);

        if (optionalDoctor.isEmpty()) {
            return -1;
        }

        // Get available slots (this already filters out booked slots)
        List<String> availableSlots = doctorService.getDoctorAvailability(doctorId,
                appointment.getAppointmentTime().toLocalDate());

        // Format the requested appointment time to match the slot start format (e.g.,
        // "09:00")
        String requestedStartTime = appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("HH:mm"));

        // Check if any available slot starts with the requested start time
        boolean isAvailable = availableSlots.stream().anyMatch(slot -> slot.startsWith(requestedStartTime));

        return isAvailable ? 1 : 0;
    }

    /**
     * A helper method required by AppointmentService.updateAppointment to return
     * specific error messages.
     * This acts as an overloaded wrapper for the integer-based validateAppointment
     * method above.
     */
    public Map<String, String> validateAppointment(Appointment appointment, boolean isUpdate) {
        Map<String, String> errors = new HashMap<>();
        int status = validateAppointment(appointment);

        if (status == -1) {
            errors.put("message", "Invalid Doctor ID.");
        } else if (status == 0) {
            errors.put("message", "The requested time slot is no longer available.");
        }
        return errors;
    }

    /**
     * Checks whether a patient exists based on their email or phone number.
     * Return true if patient does NOT exist (valid for registration), false if they
     * do exist.
     */
    public boolean validatePatient(Patient patient) {
        Patient existingPatient = patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone());
        return existingPatient == null;
    }

    /**
     * Validates a patient's login credentials.
     */
    public ResponseEntity<Map<String, String>> validatePatientLogin(Login login) {
        Map<String, String> response = new HashMap<>();
        try {
            Patient patient = patientRepository.findByEmail(login.getEmail());

            if (patient != null && patient.getPassword().equals(login.getPassword())) {
                String token = tokenService.generateToken(patient.getId(), "loggedPatient"); // Or just "patient"
                                                                                             // depending on your setup
                response.put("token", token);
                return ResponseEntity.ok(response);
            }

            response.put("message", "Invalid email or password.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            response.put("message", "An error occurred during patient login.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Filters patient appointments based on specific criteria.
     */
    public ResponseEntity<Map<String, Object>> filterPatient(String condition, String name, String token) {
        try {
            Long patientId = tokenService.extractId(token);

            boolean hasCondition = (condition != null && !condition.trim().isEmpty()
                    && !condition.equalsIgnoreCase("null"));
            boolean hasName = (name != null && !name.trim().isEmpty() && !name.equalsIgnoreCase("null"));

            if (hasCondition && hasName) {
                return patientService.filterByDoctorAndCondition(condition, name, patientId);
            } else if (hasCondition) {
                return patientService.filterByCondition(condition, patientId);
            } else if (hasName) {
                return patientService.filterByDoctor(name, patientId);
            } else {
                return patientService.getPatientAppointment(patientId, token);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid token or error filtering appointments.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
}