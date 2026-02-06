// CompositionPlat.java
package com.example.poisson.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "compositionplat")
@Data
public class CompositionPlat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_compositionplat")
    private Long idCompositionPlat;
    
    @ManyToOne
    @JoinColumn(name = "id_plat", nullable = false)
    private Plat plat;
    
    @ManyToOne
    @JoinColumn(name = "id_aliment", nullable = false)
    private Aliment aliment;
    
    @Column(name = "poids_aliment_composition", nullable = false, precision = 8, scale = 2)
    private BigDecimal poidsAlimentComposition;
    
    @Column(name = "cout_aliment_composition", precision = 10, scale = 2)
    private BigDecimal coutAlimentComposition;
    
    @Column(name = "proteines_composition", precision = 8, scale = 2)
    private BigDecimal proteinesComposition;
    
    @Column(name = "glucides_composition", precision = 8, scale = 2)
    private BigDecimal glucidesComposition;
    
    @Column(name = "date_creation_composition")
    private LocalDateTime dateCreationComposition = LocalDateTime.now();
    
    @Column(name = "date_modification_composition")
    private LocalDateTime dateModificationComposition = LocalDateTime.now();
    
    // Méthode pour calculer automatiquement les valeurs
    @PrePersist
    @PreUpdate
    private void calculerValeurs() {
        if (aliment != null && poidsAlimentComposition != null) {
            // Calcul du coût
            coutAlimentComposition = poidsAlimentComposition
                .multiply(aliment.getPrixKgAliment())
                .divide(new BigDecimal("1000"), 2, BigDecimal.ROUND_HALF_UP);
            
            // Calcul des nutriments
            proteinesComposition = poidsAlimentComposition
                .multiply(aliment.getProteinesParKgAliment())
                .divide(new BigDecimal("1000"), 2, BigDecimal.ROUND_HALF_UP);
            
            glucidesComposition = poidsAlimentComposition
                .multiply(aliment.getGlucidesParKgAliment())
                .divide(new BigDecimal("1000"), 2, BigDecimal.ROUND_HALF_UP);
        }
    }
}

