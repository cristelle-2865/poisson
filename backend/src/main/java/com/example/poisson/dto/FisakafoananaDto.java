package com.example.poisson.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
public class FisakafoananaDto {
    private Long idFisakafoanana;
    
    // Informations du poisson
    private Long poissonId;
    private String poissonNom;
    private Long raceId;
    private String raceNom;
    private Long piscineId;
    private String piscineNom;
    
    // Informations du plat (si utilisé)
    private Long platId;
    private String platNom;
    private String platDescription;
    
    // Données du nourrissage
    private LocalDate dateNourrissage;
    private LocalTime heureNourrissage;
    private BigDecimal ancienPoids;
    private BigDecimal nouveauPoids;
    private BigDecimal quantiteNourriture;
    private BigDecimal proteinesRecues;
    private BigDecimal glucidesRecus;
    private BigDecimal gainPoids;
    private BigDecimal tauxSatisfaction;
    private Boolean besoinsSatisfaits;
    private LocalDateTime dateCreation;
}

