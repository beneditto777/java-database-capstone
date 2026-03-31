package com.project.back_end.controllers;

import com.project.back_end.models.Admin;
import com.project.back_end.services.Service; // Adjust this import if your Service class is in a different package
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("${api.path}admin")
public class AdminController {

    private final Service service;

    // Constructor Injection for the Service dependency
    public AdminController(Service service) {
        this.service = service;
    }

    /**
     * Handles admin login requests.
     * Expects an Admin object (username and password) in the request body.
     * * @param admin The admin credentials from the frontend
     * 
     * @return ResponseEntity containing the JWT token if successful, or an error
     *         message if unauthorized
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> adminLogin(@RequestBody Admin admin) {
        // Delegate the authentication logic to the central Service class
        return service.validateAdmin(admin);
    }
}