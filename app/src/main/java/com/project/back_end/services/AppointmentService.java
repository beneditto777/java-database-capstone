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
    // Explicitly using our custom Service path to avoid collision with Spring's
    // @Service
    private final com.project.back_end.services.Service validationService;

    public AppointmentService(AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            TokenService tokenService,
            com.project.back_end.services.Service validationService) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.tokenService = tokenService;
        this.validationService = validationService;
    }

    @Transactional
    public int bookAppointment(Appointment appointment) {
        try {
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @Transactional
    public ResponseEntity<Map<String, String>> updateAppointment(Appointment appointment) {
        Map<String, String> response = new HashMap<>();

        Optional<Appointment> existingAppointment = appointmentRepository.findById(appointment.getId());
        if (existingAppointment.isEmpty()) {
            response.put("message", "Appointment not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Map<String, String> validationErrors = validationService.validateAppointment(appointment, true);
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

    @Transactional
    public ResponseEntity<Map<String, String>> cancelAppointment(long id, String token) {
        Map<String, String> response = new HashMap<>();

        Optional<Appointment> optionalAppointment = appointmentRepository.findById(id);
        if (optionalAppointment.isEmpty()) {
            response.put("message", "Appointment not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Appointment appointment = optionalAppointment.get();
        Long patientIdFromToken = tokenService.extractId(token);

        if (!appointment.getPatient().getId().equals(patientIdFromToken)) {
            response.put("message", "Unauthorized.");
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

    @Transactional(readOnly = true)
    public Map<String, Object> getAppointment(String pname, LocalDate date, String token) {
        Map<String, Object> response = new HashMap<>();
        Long doctorId = tokenService.extractId(token);
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Appointment> appointments;
        if (pname != null && !pname.trim().isEmpty()) {
            appointments = appointmentRepository
                    .findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(doctorId, pname,
                            startOfDay, endOfDay);
        } else {
            appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, startOfDay,
                    endOfDay);
        }

        response.put("appointments", appointments);
        return response;
    }

    @Transactional
    public void changeStatus(int status, long id) {
        appointmentRepository.updateStatus(status, id);
    }
}