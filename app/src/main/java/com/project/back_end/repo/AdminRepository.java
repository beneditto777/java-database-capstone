package com.project.back_end.repo;

import com.project.back_end.models.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    // Custom query method to find an Admin by their username.
    // Spring Data JPA automatically writes the SQL query for this behind the
    // scenes!
    Admin findByUsername(String username);

}