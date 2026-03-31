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

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@org.springframework.stereotype.Service
public class Service {

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public Service(TokenService tokenService, AdminRepository adminRepository, DoctorRepository doctorRepository,
            PatientRepository patientRepository, DoctorService doctorService, PatientService patientService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    public Map<String, String> validateToken(String token, String userRole) {
        Map<String, String> errors = new HashMap<>();
        if (token == null || token.isEmpty()) {
            errors.put("message", "Token is missing.");
            return errors;
        }
        if (!tokenService.validateToken(token, userRole)) {
            errors.put("message", "Invalid or expired token.");
        }
        return errors;
    }

    public ResponseEntity<Map<String, String>> validateAdmin(Admin receivedAdmin) {
        Map<String, String> response = new HashMap<>();
        try {
            Admin admin = adminRepository.findByUsername(receivedAdmin.getUsername());
            if (admin != null && admin.getPassword().equals(receivedAdmin.getPassword())) {
                String token = tokenService.generateToken(admin.getId(), "admin");
                response.put("token", token);
                return ResponseEntity.ok(response);
            }
            response.put("message", "Invalid admin credentials.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            response.put("message", "An error occurred.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    public Map<String, Object> filterDoctor(String name, String specialty, String time) {
        boolean hasName = (name != null && !name.trim().isEmpty());
        boolean hasSpecialty = (specialty != null && !specialty.trim().isEmpty());
        boolean hasTime = (time != null && !time.trim().isEmpty());

        if (hasName && hasSpecialty && hasTime)
            return doctorService.filterDoctorsByNameSpecilityandTime(name, specialty, time);
        if (hasName && hasTime)
            return doctorService.filterDoctorByNameAndTime(name, time);
        if (hasName && hasSpecialty)
            return doctorService.filterDoctorByNameAndSpecility(name, specialty);
        if (hasSpecialty && hasTime)
            return doctorService.filterDoctorByTimeAndSpecility(specialty, time);
        if (hasName)
            return doctorService.findDoctorByName(name);
        if (hasSpecialty)
            return doctorService.filterDoctorBySpecility(specialty);
        if (hasTime)
            return doctorService.filterDoctorsByTime(time);

        return Map.of("doctors", doctorService.getDoctors());
    }

    public int validateAppointment(Appointment appointment) {
        if (appointment.getDoctor() == null || appointment.getDoctor().getId() == null)
            return -1;
        Long doctorId = appointment.getDoctor().getId();
        Optional<Doctor> optionalDoctor = doctorRepository.findById(doctorId);
        if (optionalDoctor.isEmpty())
            return -1;

        List<String> availableSlots = doctorService.getDoctorAvailability(doctorId,
                appointment.getAppointmentTime().toLocalDate());
        String requestedStartTime = appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        return availableSlots.stream().anyMatch(slot -> slot.startsWith(requestedStartTime)) ? 1 : 0;
    }

    public Map<String, String> validateAppointment(Appointment appointment, boolean isUpdate) {
        Map<String, String> errors = new HashMap<>();
        int status = validateAppointment(appointment);
        if (status == -1)
            errors.put("message", "Invalid Doctor ID.");
        else if (status == 0)
            errors.put("message", "The requested time slot is no longer available.");
        return errors;
    }

    public boolean validatePatient(Patient patient) {
        return patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone()) == null;
    }

    public ResponseEntity<Map<String, String>> validatePatientLogin(Login login) {
        Map<String, String> response = new HashMap<>();
        try {
            Patient patient = patientRepository.findByEmail(login.getEmail());
            if (patient != null && patient.getPassword().equals(login.getPassword())) {
                String token = tokenService.generateToken(patient.getId(), "loggedPatient");
                response.put("token", token);
                return ResponseEntity.ok(response);
            }
            response.put("message", "Invalid email or password.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            response.put("message", "An error occurred.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    public ResponseEntity<Map<String, Object>> filterPatient(String condition, String name, String token) {
        try {
            Long patientId = tokenService.extractId(token);
            boolean hasCondition = (condition != null && !condition.trim().isEmpty()
                    && !condition.equalsIgnoreCase("null"));
            boolean hasName = (name != null && !name.trim().isEmpty() && !name.equalsIgnoreCase("null"));

            if (hasCondition && hasName)
                return patientService.filterByDoctorAndCondition(condition, name, patientId);
            if (hasCondition)
                return patientService.filterByCondition(condition, patientId);
            if (hasName)
                return patientService.filterByDoctor(name, patientId);

            return patientService.getPatientAppointment(patientId, token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}