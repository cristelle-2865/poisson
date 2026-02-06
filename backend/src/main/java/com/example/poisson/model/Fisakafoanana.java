package com.example.poisson.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "fisakafoanana")
@Data
public class Fisakafoanana {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_fisakafoanana")
    private Long idFisakafoanana;
    
    @ManyToOne
    @JoinColumn(name = "id_poisson", nullable = false)
    private Poisson poisson;
    
    @Column(name = "date_nourrissage_fisakafoanana", nullable = false)
    private LocalDate dateNourrissageFisakafoanana;
    
    @Column(name = "heure_nourrissage_fisakafoanana")
    private LocalTime heureNourrissageFisakafoanana = LocalTime.now();
    
    @Column(name = "ancien_poids_fisakafoanana", nullable = false, precision = 8, scale = 2)
    private BigDecimal ancienPoidsFisakafoanana;
    
    @Column(name = "nouveau_poids_fisakafoanana", nullable = false, precision = 8, scale = 2)
    private BigDecimal nouveauPoidsFisakafoanana;
    
    @Column(name = "quantite_nourriture_fisakafoanana", precision = 8, scale = 2)
    private BigDecimal quantiteNourritureFisakafoanana = BigDecimal.ZERO;
    
    @Column(name = "proteines_recues_fisakafoanana", precision = 8, scale = 2)
    private BigDecimal proteinesRecuesFisakafoanana = BigDecimal.ZERO;
    
    @Column(name = "glucides_recus_fisakafoanana", precision = 8, scale = 2)
    private BigDecimal glucidesRecusFisakafoanana = BigDecimal.ZERO;
    
    @Column(name = "gain_poids_fisakafoanana", precision = 8, scale = 2)
    private BigDecimal gainPoidsFisakafoanana = BigDecimal.ZERO;
    
    @Column(name = "taux_satisfaction_fisakafoanana", precision = 5, scale = 2)
    private BigDecimal tauxSatisfactionFisakafoanana = BigDecimal.ZERO;
    
    @Column(name = "besoins_satisfaits_fisakafoanana")
    private Boolean besoinsSatisfaitsFisakafoanana = false;
    
    @Column(name = "date_creation_fisakafoanana")
    private LocalDateTime dateCreationFisakafoanana = LocalDateTime.now();
}


