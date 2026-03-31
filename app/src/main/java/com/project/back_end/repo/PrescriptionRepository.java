package com.project.back_end.repo;

import com.project.back_end.models.Prescription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends MongoRepository<Prescription, String> {

    /**
     * Retrieves a list of prescriptions associated with a specific appointment.
     * Spring Data MongoDB automatically writes the NoSQL query for this behind the scenes.
     *
     * @param appointmentId the ID of the appointment
     * @return a list of prescriptions for the given appointment
     */
    List<Prescription> findByAppointmentId(Long appointmentId);

}