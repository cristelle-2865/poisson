// PoissonDetailDTO.java
package com.example.poisson.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PoissonDetailDTO {
    private Long idPoisson;
    private String nomPoisson;
    private String raceNom;
    private Long raceId;
    private BigDecimal poidsActuelPoisson;
    private BigDecimal poidsMaximalPoisson;
    private BigDecimal poidsInitialPoisson;
    private BigDecimal prixAchatPoisson;
    private BigDecimal prixVentePoisson;
    private LocalDate dateArriveePoisson;
    private LocalDate dateVentePoisson;
    private Boolean estRassasiePoisson;
    private Boolean estVenduPoisson;
    private Boolean estEnViePoisson;
    private BigDecimal capaciteAugmentationPoisson;
    
    // Informations bassin
    private String piscineNom;
    private Integer piscineCapacite;
    private Long piscineId;
    private Boolean estPretAVendre;
    private BigDecimal pourcentageProgression;
    
    public PoissonDetailDTO(com.example.poisson.model.Poisson poisson) {
        this.idPoisson = poisson.getIdPoisson();
        this.nomPoisson = poisson.getNomPoisson();
        
        if (poisson.getRacePoisson() != null) {
            this.raceNom = poisson.getRacePoisson().getNomRacePoisson();
            this.raceId = poisson.getRacePoisson().getIdRacePoisson();
        }
        
        this.poidsActuelPoisson = poisson.getPoidsActuelPoisson();
        this.poidsMaximalPoisson = poisson.getPoidsMaximalPoisson();
        this.poidsInitialPoisson = poisson.getPoidsInitialPoisson();
        this.prixAchatPoisson = poisson.getPrixAchatPoisson();
        this.prixVentePoisson = poisson.getPrixVentePoisson();
        this.dateArriveePoisson = poisson.getDateArriveePoisson();
        this.dateVentePoisson = poisson.getDateVentePoisson();
        this.estRassasiePoisson = poisson.getEstRassasiePoisson();
        this.estVenduPoisson = poisson.getEstVenduPoisson();
        this.estEnViePoisson = poisson.getEstEnViePoisson();
        this.capaciteAugmentationPoisson = poisson.getCapaciteAugmentationPoisson();
        
        if (poisson.getPiscineActuelle() != null) {
            this.piscineNom = poisson.getPiscineActuelle().getNomPiscine();
            this.piscineCapacite = poisson.getPiscineActuelle().getCapaciteMaxPiscine();
            this.piscineId = poisson.getPiscineActuelle().getIdPiscine();
        }
        
        this.estPretAVendre = poisson.estPretAVendre();
        
        // Calculer le pourcentage de progression
        if (poisson.getPoidsMaximalPoisson() != null && 
            poisson.getPoidsMaximalPoisson().compareTo(BigDecimal.ZERO) > 0) {
            this.pourcentageProgression = poisson.getPoidsActuelPoisson()
                .divide(poisson.getPoidsMaximalPoisson(), 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        } else {
            this.pourcentageProgression = BigDecimal.ZERO;
        }
    }
}

