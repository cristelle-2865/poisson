// PoissonDTO.java (Optionnel, mais recommandé)
package com.example.poisson.dto;

import com.example.poisson.model.Piscine;
import com.example.poisson.model.RacePoisson;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PoissonDTO {
    private Long idPoisson;
    private RacePoisson racePoisson;
    private String nomPoisson;
    private BigDecimal prixAchatPoisson;
    private BigDecimal prixVentePoisson;
    private BigDecimal poidsMaximalPoisson;
    private BigDecimal capaciteAugmentationPoisson;
    private BigDecimal poidsInitialPoisson;
    private BigDecimal poidsActuelPoisson;
    private LocalDate dateArriveePoisson;
    private Boolean estRassasiePoisson;
    private Boolean estVenduPoisson;
    private Boolean estEnViePoisson;
    
    // Informations sur le bassin
    private Long piscineId;
    private String piscineNom;
    private Integer piscineCapacite;
    
    // Calculs
    private BigDecimal pourcentageCroissance;
    private Boolean estPretAVendre;
    
    public PoissonDTO(com.example.poisson.model.Poisson poisson) {
        this.idPoisson = poisson.getIdPoisson();
        this.racePoisson = poisson.getRacePoisson();
        this.nomPoisson = poisson.getNomPoisson();
        this.prixAchatPoisson = poisson.getPrixAchatPoisson();
        this.prixVentePoisson = poisson.getPrixVentePoisson();
        this.poidsMaximalPoisson = poisson.getPoidsMaximalPoisson();
        this.capaciteAugmentationPoisson = poisson.getCapaciteAugmentationPoisson();
        this.poidsInitialPoisson = poisson.getPoidsInitialPoisson();
        this.poidsActuelPoisson = poisson.getPoidsActuelPoisson();
        this.dateArriveePoisson = poisson.getDateArriveePoisson();
        this.estRassasiePoisson = poisson.getEstRassasiePoisson();
        this.estVenduPoisson = poisson.getEstVenduPoisson();
        this.estEnViePoisson = poisson.getEstEnViePoisson();
        
        if (poisson.getPiscineActuelle() != null) {
            this.piscineId = poisson.getPiscineActuelle().getIdPiscine();
            this.piscineNom = poisson.getPiscineActuelle().getNomPiscine();
            this.piscineCapacite = poisson.getPiscineActuelle().getCapaciteMaxPiscine();
        }
        
        // Calculs
        this.pourcentageCroissance = poisson.getPoidsActuelPoisson()
            .divide(poisson.getPoidsMaximalPoisson(), 4, BigDecimal.ROUND_HALF_UP)
            .multiply(BigDecimal.valueOf(100));
        
        this.estPretAVendre = poisson.estPretAVendre();
    }
}

