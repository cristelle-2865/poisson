// AffectationPiscine.java
package com.example.poisson.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "affectationpiscine")
@Data
public class AffectationPiscine {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_affectationpiscine")
    private Long idAffectationPiscine;
    
    @ManyToOne
    @JoinColumn(name = "id_piscine", nullable = false)
    private Piscine piscine;
    
    @ManyToOne
    @JoinColumn(name = "id_poisson", nullable = false)
    private Poisson poisson;
    
    @Column(name = "date_entree_affectation", nullable = false)
    private LocalDate dateEntreeAffectation;
    
    @Column(name = "date_sortie_affectation")
    private LocalDate dateSortieAffectation;
    
    @Column(name = "raison_sortie_affectation", length = 50)
    private String raisonSortieAffectation;
    
    @Column(name = "date_creation_affectation")
    private LocalDateTime dateCreationAffectation = LocalDateTime.now();
    
    @Column(name = "commentaires_affectation", columnDefinition = "TEXT")
    private String commentairesAffectation;
}

