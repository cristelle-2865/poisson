// User.java
package com.example.poisson.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "utilisateur")
@Data
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_utilisateur")
    private Long idUtilisateur;
    
    @Column(name = "nom_utilisateur", nullable = false, length = 100)
    private String nomUtilisateur;
    
    @Column(name = "email_utilisateur", nullable = false, unique = true, length = 100)
    private String emailUtilisateur;
    
    @Column(name = "motdepasse_utilisateur", nullable = false, length = 255)
    private String motdepasseUtilisateur;
    
    @Column(name = "role_utilisateur", nullable = false, length = 30)
    private String roleUtilisateur = "UTILISATEUR";
    
    @Column(name = "est_actif_utilisateur")
    private Boolean estActifUtilisateur = true;
    
    @Column(name = "date_derniere_connexion")
    private LocalDateTime dateDerniereConnexion;
    
    @Column(name = "date_creation_utilisateur")
    private LocalDateTime dateCreationUtilisateur = LocalDateTime.now();
}


