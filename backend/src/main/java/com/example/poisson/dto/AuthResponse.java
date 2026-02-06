// AuthResponse.java
package com.example.poisson.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String email;
    private String nom;
    private String role;
    private String message;
    
    public AuthResponse(String token, String email, String nom, String role) {
        this.token = token;
        this.email = email;
        this.nom = nom;
        this.role = role;
        this.message = "Authentication successful";
    }
}

