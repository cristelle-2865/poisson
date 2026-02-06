// AuthService.java
package com.example.poisson.service;

import com.example.poisson.dto.AuthRequest;
import com.example.poisson.dto.AuthResponse;
import com.example.poisson.dto.RegisterRequest;
import com.example.poisson.model.User;
import com.example.poisson.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    public AuthResponse login(AuthRequest request) {
        // Vérifier l'utilisateur
        User user = userRepository.findByEmailUtilisateur(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect"));
        
        // Vérifier le mot de passe
        if (!passwordEncoder.matches(request.getPassword(), user.getMotdepasseUtilisateur())) {
            throw new RuntimeException("Email ou mot de passe incorrect");
        }
        
        // Vérifier si le compte est actif
        if (!user.getEstActifUtilisateur()) {
            throw new RuntimeException("Ce compte est désactivé");
        }
        
        // Mettre à jour la dernière connexion
        user.setDateDerniereConnexion(LocalDateTime.now());
        userRepository.save(user);
        
        // Générer le token JWT
        String token = jwtService.generateToken(user.getEmailUtilisateur(), user.getRoleUtilisateur());
        
        return new AuthResponse(
            token,
            user.getEmailUtilisateur(),
            user.getNomUtilisateur(),
            user.getRoleUtilisateur()
        );
    }
    
    public AuthResponse register(RegisterRequest request) {
        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmailUtilisateur(request.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }
        
        // Vérifier la confirmation du mot de passe
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Les mots de passe ne correspondent pas");
        }
        
        // Créer le nouvel utilisateur
        User user = new User();
        user.setNomUtilisateur(request.getNom());
        user.setEmailUtilisateur(request.getEmail());
        user.setMotdepasseUtilisateur(passwordEncoder.encode(request.getPassword()));
        user.setRoleUtilisateur("UTILISATEUR");
        user.setEstActifUtilisateur(true);
        user.setDateCreationUtilisateur(LocalDateTime.now());
        
        userRepository.save(user);
        
        // Générer le token JWT
        String token = jwtService.generateToken(user.getEmailUtilisateur(), user.getRoleUtilisateur());
        
        return new AuthResponse(
            token,
            user.getEmailUtilisateur(),
            user.getNomUtilisateur(),
            user.getRoleUtilisateur()
        );
    }
    
    // Méthode pour l'accès démo
    public AuthResponse demoLogin() {
        // Créer ou récupérer un utilisateur démo
        String demoEmail = "demo@pisciculture.mg";
        User demoUser = userRepository.findByEmailUtilisateur(demoEmail)
            .orElseGet(() -> {
                User newDemoUser = new User();
                newDemoUser.setNomUtilisateur("Utilisateur Démo");
                newDemoUser.setEmailUtilisateur(demoEmail);
                newDemoUser.setMotdepasseUtilisateur(passwordEncoder.encode("demo123"));
                newDemoUser.setRoleUtilisateur("ADMIN");
                newDemoUser.setEstActifUtilisateur(true);
                return userRepository.save(newDemoUser);
            });
        
        // Mettre à jour la dernière connexion
        demoUser.setDateDerniereConnexion(LocalDateTime.now());
        userRepository.save(demoUser);
        
        // Générer le token JWT
        String token = jwtService.generateToken(demoUser.getEmailUtilisateur(), demoUser.getRoleUtilisateur());
        
        return new AuthResponse(
            token,
            demoUser.getEmailUtilisateur(),
            demoUser.getNomUtilisateur(),
            demoUser.getRoleUtilisateur()
        );
    }
}

