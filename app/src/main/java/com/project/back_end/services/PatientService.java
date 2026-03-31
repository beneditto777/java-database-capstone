package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    // Constructor Injection for Dependencies
    public DoctorService(DoctorRepository doctorRepository,
            AppointmentRepository appointmentRepository,
            TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    /**
     * Retrieves available time slots by filtering out booked appointments.
     */
    @Transactional(readOnly = true)
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        Optional<Doctor> optionalDoctor = doctorRepository.findById(doctorId);
        if (optionalDoctor.isEmpty()) {
            return new ArrayList<>();
        }

        Doctor doctor = optionalDoctor.get();
        List<String> allSlots = new ArrayList<>(doctor.getAvailableTimes());

        // Define start and end of the requested day
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // Fetch booked appointments for this doctor on this day
        List<Appointment> bookedAppointments = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctorId, startOfDay, endOfDay);

        // Map booked appointments to "HH:mm-HH:mm" slot format to remove them from
        // allSlots
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        for (Appointment appt : bookedAppointments) {
            String startTime = appt.getAppointmentTime().format(timeFormatter);
            String endTime = appt.getAppointmentTime().plusHours(1).format(timeFormatter);
            String bookedSlot = startTime + "-" + endTime;

            allSlots.remove(bookedSlot);
        }

        return allSlots;
    }

    /**
     * Saves a new doctor if the email doesn't already exist.
     */
    @Transactional
    public int saveDoctor(Doctor doctor) {
        try {
            if (doctorRepository.findByEmail(doctor.getEmail()) != null) {
                return -1; // Doctor already exists
            }
            doctorRepository.save(doctor);
            return 1; // Success
        } catch (Exception e) {
            System.err.println("Error saving doctor: " + e.getMessage());
            return 0; // Internal error
        }
    }

    /**
     * Updates an existing doctor's details.
     */
    @Transactional
    public int updateDoctor(Doctor doctor) {
        try {
            if (!doctorRepository.existsById(doctor.getId())) {
                return -1; // Doctor not found
            }
            doctorRepository.save(doctor);
            return 1; // Success
        } catch (Exception e) {
            System.err.println("Error updating doctor: " + e.getMessage());
            return 0; // Internal error
        }
    }

    /**
     * Retrieves all doctors.
     */
    @Transactional(readOnly = true)
    public List<Doctor> getDoctors() {
        return doctorRepository.findAll();
    }

    /**
     * Deletes a doctor and all of their associated appointments.
     */
    @Transactional
    public int deleteDoctor(long id) {
        try {
            if (!doctorRepository.existsById(id)) {
                return -1; // Doctor not found
            }
            // Delete associated appointments first to maintain referential integrity
            appointmentRepository.deleteAllByDoctorId(id);
            doctorRepository.deleteById(id);
            return 1; // Success
        } catch (Exception e) {
            System.err.println("Error deleting doctor: " + e.getMessage());
            return 0; // Internal error
        }
    }

    /**
     * Validates a doctor's login credentials.
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, String>> validateDoctor(Login login) {
        Map<String, String> response = new HashMap<>();
        Doctor doctor = doctorRepository.findByEmail(login.getEmail());

        // In a real production app, use PasswordEncoder (e.g., BCrypt) instead of plain
        // text equals
        if (doctor != null && doctor.getPassword().equals(login.getPassword())) {
            // Assuming your TokenService generates a token from the ID and Role
            String token = tokenService.generateToken(doctor.getId(), "doctor");
            response.put("token", token);
            return ResponseEntity.ok(response);
        }

        response.put("message", "Invalid credentials");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Finds doctors by partial name match.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> findDoctorByName(String name) {
        List<Doctor> doctors = doctorRepository.findByNameLike(name);
        return Map.of("doctors", doctors);
    }

    /**
     * Filters doctors by name, specialty, and AM/PM availability.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorsByNameSpecilityandTime(String name, String specialty, String amOrPm) {
        List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        List<Doctor> filteredDoctors = filterDoctorByTime(doctors, amOrPm);
        return Map.of("doctors", filteredDoctors);
    }

    /**
     * Filters doctors by name and AM/PM availability.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorByNameAndTime(String name, String amOrPm) {
        List<Doctor> doctors = doctorRepository.findByNameLike(name);
        List<Doctor> filteredDoctors = filterDoctorByTime(doctors, amOrPm);
        return Map.of("doctors", filteredDoctors);
    }

    /**
     * Filters doctors by name and specialty.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorByNameAndSpecility(String name, String specialty) {
        List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        return Map.of("doctors", doctors);
    }

    /**
     * Filters doctors by specialty and AM/PM availability.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorByTimeAndSpecility(String specialty, String amOrPm) {
        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);
        List<Doctor> filteredDoctors = filterDoctorByTime(doctors, amOrPm);
        return Map.of("doctors", filteredDoctors);
    }

    /**
     * Filters doctors by specialty only.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorBySpecility(String specialty) {
        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);
        return Map.of("doctors", doctors);
    }

    /**
     * Filters all doctors by AM/PM availability.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorsByTime(String amOrPm) {
        List<Doctor> doctors = doctorRepository.findAll();
        List<Doctor> filteredDoctors = filterDoctorByTime(doctors, amOrPm);
        return Map.of("doctors", filteredDoctors);
    }

    /**
     * Private helper method to filter a list of doctors by their available times
     * (AM or PM).
     * Parses the start hour of their slots (e.g., "09:00-10:00" -> 9).
     * AM is < 12, PM is >= 12.
     */
    private List<Doctor> filterDoctorByTime(List<Doctor> doctors, String amOrPm) {
        if (amOrPm == null || amOrPm.isEmpty()) {
            return doctors;
        }

        return doctors.stream().filter(doctor -> doctor.getAvailableTimes().stream().anyMatch(timeSlot -> {
            try {
                // Extract the first two characters (the start hour)
                int hour = Integer.parseInt(timeSlot.split(":")[0]);
                if (amOrPm.equalsIgnoreCase("AM")) {
                    return hour < 12;
                } else if (amOrPm.equalsIgnoreCase("PM")) {
                    return hour >= 12;
                }
                return true;
            } catch (Exception e) {
                return false; // Skip malformed time strings
            }
        })).collect(Collectors.toList());
    }
}