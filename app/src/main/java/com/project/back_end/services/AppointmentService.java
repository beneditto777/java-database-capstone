package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TokenService tokenService;
    private final Service validationService; // Replaced "Service" with a more descriptive name for the generic service

    // Constructor Injection for proper dependency management
    public AppointmentService(AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            TokenService tokenService,
            Service validationService) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.tokenService = tokenService;
        this.validationService = validationService;
    }

    /**
     * Books a new appointment.
     * 
     * @param appointment The appointment object to book
     * @return 1 if successful, 0 if there's an error
     */
    @Transactional
    public int bookAppointment(Appointment appointment) {
        try {
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            System.err.println("Error booking appointment: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Updates an existing appointment.
     * 
     * @param appointment The updated appointment object
     * @return ResponseEntity with a success or error message
     */
    @Transactional
    public ResponseEntity<Map<String, String>> updateAppointment(Appointment appointment) {
        Map<String, String> response = new HashMap<>();

        Optional<Appointment> existingAppointment = appointmentRepository.findById(appointment.getId());
        if (existingAppointment.isEmpty()) {
            response.put("message", "Appointment not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // Validate the appointment using the shared service logic
        Map<String, String> validationErrors = validationService.validateAppointment(appointment);
        if (!validationErrors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationErrors);
        }

        try {
            appointmentRepository.save(appointment);
            response.put("message", "Appointment updated successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "An error occurred while updating the appointment.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Cancels an existing appointment.
     * 
     * @param id    The ID of the appointment to cancel
     * @param token The authorization token of the patient
     * @return ResponseEntity with a success or error message
     */
    @Transactional
    public ResponseEntity<Map<String, String>> cancelAppointment(long id, String token) {
        Map<String, String> response = new HashMap<>();

        Optional<Appointment> optionalAppointment = appointmentRepository.findById(id);
        if (optionalAppointment.isEmpty()) {
            response.put("message", "Appointment not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Appointment appointment = optionalAppointment.get();

        // Extract patient ID from the token to verify ownership
        Long patientIdFromToken = tokenService.extractId(token);

        if (!appointment.getPatient().getId().equals(patientIdFromToken)) {
            response.put("message", "Unauthorized. You can only cancel your own appointments.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            appointmentRepository.delete(appointment);
            response.put("message", "Appointment cancelled successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Failed to cancel appointment.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Retrieves a list of appointments for a specific doctor on a specific date.
     * 
     * @param pname Patient name to filter by (optional)
     * @param date  The date for appointments
     * @param token The authorization token of the doctor
     * @return Map containing the list of appointments
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAppointment(String pname, LocalDate date, String token) {
        Map<String, Object> response = new HashMap<>();

        // Extract doctor ID from the token
        Long doctorId = tokenService.extractId(token);

        // Create a time range for the specified date (00:00:00 to 23:59:59)
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Appointment> appointments;

        // Fetch based on whether a patient name filter was provided
        if (pname != null && !pname.trim().isEmpty()) {
            appointments = appointmentRepository
                    .findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                            doctorId, pname, startOfDay, endOfDay);
        } else {
            appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                    doctorId, startOfDay, endOfDay);
        }

        response.put("appointments", appointments);
        return response;
    }

    /**
     * Updates the status of an appointment.
     * 
     * @param status The new status code
     * @param id     The ID of the appointment
     */
    @Transactional
    public void changeStatus(int status, long id) {
        appointmentRepository.updateStatus(status, id);
    }
}