package com.project.back_end.DTO;

public class Login {

    private String email;
    private String password;

    // --- Constructor ---
    // A default no-argument constructor is required for Spring to deserialize the
    // incoming JSON
    public Login() {
    }

    public Login(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // --- Getters and Setters ---

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}