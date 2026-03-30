package com.project.back_end.mvc;

import com.project.back_end.services.Service; // Adjust this import based on your actual service package
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
public class DashboardController {

    @Autowired
    private Service service;

    /**
     * Serves the Admin Dashboard view after token validation.
     * Path: /adminDashboard/{token}
     */
    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable String token) {
        // validateToken returns a Map of errors. If empty, token is valid.
        Map<String, String> errors = service.validateToken(token, "admin");

        if (errors.isEmpty()) {
            return "admin/adminDashboard";
        } else {
            // Redirect to home/login if token is invalid
            return "redirect:http://localhost:8080";
        }
    }

    /**
     * Serves the Doctor Dashboard view after token validation.
     * Path: /doctorDashboard/{token}
     */
    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable String token) {
        // validateToken returns a Map of errors. If empty, token is valid.
        Map<String, String> errors = service.validateToken(token, "doctor");

        if (errors.isEmpty()) {
            return "doctor/doctorDashboard";
        } else {
            // Redirect to home/login if token is invalid
            return "redirect:http://localhost:8080";
        }
    }
}