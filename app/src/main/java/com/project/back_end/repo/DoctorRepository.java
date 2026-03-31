package com.project.back_end.repo;

import com.project.back_end.models.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@RepositoryÏ
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    /**
     * Retrieves a Doctor by their exact email address.
     * Spring Data JPA automatically writes the SQL query for this.
     */
    Doctor findByEmail(String email);

    /**
     * Retrieves a list of Doctors whose name contains the provided search string.
     * Uses a custom query with CONCAT and LIKE for flexible pattern matching.
     */
    @Query("SELECT d FROM Doctor d WHERE d.name LIKE CONCAT('%', :name, '%')")
    List<Doctor> findByNameLike(@Param("name") String name);

    /**
     * Filters doctors by partial name and exact specialty (both case-insensitive).
     * Uses LOWER, CONCAT, and LIKE to ensure robust search regardless of user input casing.
     */
    @Query("SELECT d FROM Doctor d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%')) AND LOWER(d.specialty) = LOWER(:specialty)")
    List<Doctor> findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(
            @Param("name") String name, 
            @Param("specialty") String specialty
    );

    /**
     * Retrieves a list of Doctors with the specified specialty, ignoring case sensitivity.
     * Spring Data JPA automatically writes the SQL query for this.
     */
    List<Doctor> findBySpecialtyIgnoreCase(String specialty);

}