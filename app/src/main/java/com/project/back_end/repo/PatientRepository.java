package com.project.back_end.repo;

import com.project.back_end.models.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    /**
     * Retrieves a Patient by their exact email address.
     * Spring Data JPA automatically writes the SQL query for this.
     * * @param email the patient's email
     * 
     * @return the Patient object if found, otherwise null
     */
    Patient findByEmail(String email);

    /**
     * Retrieves a Patient by either their email or phone number.
     * This is especially useful during registration to check if an account already
     * exists.
     * * @param email the patient's email
     * 
     * @param phone the patient's phone number
     * @return the Patient object if a match is found, otherwise null
     */
    Patient findByEmailOrPhone(String email, String phone);

}