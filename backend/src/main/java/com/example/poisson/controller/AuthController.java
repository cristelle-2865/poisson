// AuthController.java
package com.example.poisson.controller;

import com.example.poisson.dto.AuthRequest;
import com.example.poisson.dto.AuthResponse;
import com.example.poisson.dto.RegisterRequest;
import com.example.poisson.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")  // Changer de "/api/auth" à "/auth"
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/demo")
    public ResponseEntity<AuthResponse> demoLogin() {
        AuthResponse response = authService.demoLogin();
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String token) {
        String cleanToken = token.replace("Bearer ", "");
        boolean isValid = true; // Simplifié pour la démo
        return ResponseEntity.ok(isValid);
    }
}

