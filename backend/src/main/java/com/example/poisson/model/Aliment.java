package com.example.poisson.model;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore; // AJOUTEZ CET IMPORT
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // AJOUTEZ CET IMPORT
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "aliment")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // AJOUTEZ CETTE LIGNE
public class Aliment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_aliment")
    private Long idAliment;
    
    @Column(name = "nom_aliment", nullable = false, unique = true, length = 100)
    private String nomAliment;
    
    @Column(name = "prix_kg_aliment", nullable = false, precision = 10, scale = 2)
    private BigDecimal prixKgAliment;
    
    @Column(name = "proteines_par_kg_aliment", nullable = false, precision = 8, scale = 2)
    private BigDecimal proteinesParKgAliment;
    
    @Column(name = "glucides_par_kg_aliment", nullable = false, precision = 8, scale = 2)
    private BigDecimal glucidesParKgAliment;
    
    @Column(name = "stock_aliment", precision = 10, scale = 2)
    private BigDecimal stockAliment = BigDecimal.ZERO;
    
    @Column(name = "seuil_minimum_aliment", precision = 10, scale = 2)
    private BigDecimal seuilMinimumAliment = BigDecimal.valueOf(10);
    
    @Column(name = "est_actif_aliment")
    private Boolean estActifAliment = true;
    
    @Column(name = "date_creation_aliment")
    private LocalDateTime dateCreationAliment = LocalDateTime.now();
    
    @Column(name = "date_modification_aliment")
    private LocalDateTime dateModificationAliment = LocalDateTime.now();
    
    // AJOUTEZ CETTE RELATION SI ELLE N'EXISTE PAS
    @OneToMany(mappedBy = "aliment", fetch = FetchType.LAZY)
    @JsonIgnore // IMPORTANT: Éviter la récursion
    private java.util.List<CompositionPlat> compositions = new java.util.ArrayList<>();

    // Aliment.java - AJOUTER ces champs
    @Column(name = "lipides_par_kg_aliment", nullable = false, precision = 8, scale = 2)
    private BigDecimal lipidesParKgAliment = BigDecimal.ZERO;

    @Column(name = "vitamines_par_kg_aliment", nullable = false, precision = 8, scale = 2)
    private BigDecimal vitaminesParKgAliment = BigDecimal.ZERO;

    // Mettre à jour la valeur par défaut dans le constructeur si nécessaire
}

