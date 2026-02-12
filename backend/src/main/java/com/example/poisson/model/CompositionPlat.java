package com.example.poisson.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "composition_plat")
@Data
@Slf4j
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CompositionPlat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_composition_plat")
    private Long idCompositionPlat;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plat", nullable = false)
    @JsonIgnoreProperties({"compositions", "hibernateLazyInitializer", "handler"})
    private Plat plat;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_aliment", nullable = false)
    @JsonIgnoreProperties({"compositions", "hibernateLazyInitializer", "handler"})
    private Aliment aliment;
    
    @Column(name = "poids_aliment_composition", nullable = false, precision = 10, scale = 2)
    private BigDecimal poidsAlimentComposition;
    
    @Column(name = "cout_aliment_composition", precision = 10, scale = 2)
    private BigDecimal coutAlimentComposition;
    
    @Column(name = "proteines_composition", precision = 8, scale = 2)
    private BigDecimal proteinesComposition;
    
    @Column(name = "glucides_composition", precision = 8, scale = 2)
    private BigDecimal glucidesComposition;
    
    // AJOUT : Champs pour lipides et vitamines
    @Column(name = "lipides_composition", precision = 8, scale = 2)
    private BigDecimal lipidesComposition;
    
    @Column(name = "vitamines_composition", precision = 8, scale = 2)
    private BigDecimal vitaminesComposition;
    
    @Column(name = "date_creation_composition")
    private LocalDateTime dateCreationComposition = LocalDateTime.now();
    
    @PrePersist
    public void prePersist() {
        if (aliment != null && poidsAlimentComposition != null) {
            // Calculer le coût
            this.coutAlimentComposition = aliment.getPrixKgAliment()
                .multiply(poidsAlimentComposition)
                .setScale(2, RoundingMode.HALF_UP);
            
            // Calculer les protéines (en grammes)
            this.proteinesComposition = aliment.getProteinesParKgAliment()
                .multiply(poidsAlimentComposition)
                .setScale(2, RoundingMode.HALF_UP);
            
            // Calculer les glucides (en grammes)
            this.glucidesComposition = aliment.getGlucidesParKgAliment()
                .multiply(poidsAlimentComposition)
                .setScale(2, RoundingMode.HALF_UP);
            
            // AJOUT : Calculer les lipides (en grammes)
            this.lipidesComposition = aliment.getLipidesParKgAliment()
                .multiply(poidsAlimentComposition)
                .setScale(2, RoundingMode.HALF_UP);
            
            // AJOUT : Calculer les vitamines (en grammes)
            this.vitaminesComposition = aliment.getVitaminesParKgAliment()
                .multiply(poidsAlimentComposition)
                .setScale(2, RoundingMode.HALF_UP);
        }
        
        this.dateCreationComposition = LocalDateTime.now();
    }
}


