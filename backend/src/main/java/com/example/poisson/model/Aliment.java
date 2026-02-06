package com.example.poisson.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal; // IMPORT
import java.time.LocalDateTime;

@Entity
@Table(name = "aliment")
@Data
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
}

