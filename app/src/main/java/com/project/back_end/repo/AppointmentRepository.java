package com.project.back_end.repo;

import com.project.back_end.models.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Retrieves appointments for a specific doctor within a given time range.
     * Uses LEFT JOIN FETCH to eagerly load doctor and availability info.
     */
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.doctor d LEFT JOIN FETCH d.availableTimes WHERE d.id = :doctorId AND a.appointmentTime BETWEEN :start AND :end")
    List<Appointment> findByDoctorIdAndAppointmentTimeBetween(
            @Param("doctorId") Long doctorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Retrieves appointments for a specific doctor and partial patient name within
     * a given time range.
     * Uses LEFT JOIN FETCH to eagerly load both the doctor and patient details.
     */
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.doctor d LEFT JOIN FETCH a.patient p WHERE d.id = :doctorId AND LOWER(p.name) LIKE LOWER(CONCAT('%', :patientName, '%')) AND a.appointmentTime BETWEEN :start AND :end")
    List<Appointment> findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
            @Param("doctorId") Long doctorId,
            @Param("patientName") String patientName,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Deletes all appointments associated with a specific doctor.
     */
    @Modifying
    @Transactional
    void deleteAllByDoctorId(Long doctorId);

    /**
     * Retrieves all appointments for a specific patient.
     * Spring Data JPA automatically writes the SQL query for this.
     */
    List<Appointment> findByPatientId(Long patientId);

    /**
     * Retrieves all appointments for a specific patient with a given status,
     * ordered by time.
     * Spring Data JPA automatically writes the SQL query for this.
     */
    List<Appointment> findByPatient_IdAndStatusOrderByAppointmentTimeAsc(Long patientId, int status);

    /**
     * Retrieves appointments based on a doctor's partial name and the patient's ID.
     */
    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND LOWER(a.doctor.name) LIKE LOWER(CONCAT('%', :doctorName, '%'))")
    List<Appointment> filterByDoctorNameAndPatientId(
            @Param("doctorName") String doctorName,
            @Param("patientId") Long patientId);

    /**
     * Retrieves appointments based on a doctor's partial name, patient's ID, and a
     * specific status.
     */
    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND a.status = :status AND LOWER(a.doctor.name) LIKE LOWER(CONCAT('%', :doctorName, '%'))")
    List<Appointment> filterByDoctorNameAndPatientIdAndStatus(
            @Param("doctorName") String doctorName,
            @Param("patientId") Long patientId,
            @Param("status") int status);

    /**
     * Updates the status of a specific appointment based on its ID.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Appointment a SET a.status = :status WHERE a.id = :id")
    void updateStatus(@Param("status") int status, @Param("id") long id);

}