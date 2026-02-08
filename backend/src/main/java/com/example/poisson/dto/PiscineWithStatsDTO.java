// PiscineWithStatsDTO.java
package com.example.poisson.dto;

import com.example.poisson.model.Piscine; // AJOUTER CET IMPORT
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.math.RoundingMode; // AJOUTER CET IMPORT

@Data
public class PiscineWithStatsDTO {
    private Long idPiscine;
    private String nomPiscine;
    private Integer capaciteMaxPiscine;
    private String descriptionPiscine;
    private Boolean estActivePiscine;
    private LocalDateTime dateCreationPiscine;
    private LocalDateTime dateModificationPiscine;
    private BigDecimal volumePiscine;
    private BigDecimal temperaturePiscine;
    private BigDecimal phPiscine;
    
    // Statistiques
    private Integer nombrePoissonsActuels;
    private Integer capaciteRestante;
    private BigDecimal tauxOccupation;
    
    // Constructeur
    public PiscineWithStatsDTO(Piscine piscine) {
        this.idPiscine = piscine.getIdPiscine();
        this.nomPiscine = piscine.getNomPiscine();
        this.capaciteMaxPiscine = piscine.getCapaciteMaxPiscine();
        this.descriptionPiscine = piscine.getDescriptionPiscine();
        this.estActivePiscine = piscine.getEstActivePiscine();
        this.dateCreationPiscine = piscine.getDateCreationPiscine();
        // Si votre Piscine n'a pas dateModificationPiscine, commentez cette ligne
        // this.dateModificationPiscine = piscine.getDateModificationPiscine();
        this.volumePiscine = piscine.getVolumePiscine();
        this.temperaturePiscine = piscine.getTemperaturePiscine();
        this.phPiscine = piscine.getPhPiscine();
        
        // Calculer les statistiques
        this.nombrePoissonsActuels = piscine.getPoissons() != null ? piscine.getPoissons().size() : 0;
        this.capaciteRestante = Math.max(0, this.capaciteMaxPiscine - this.nombrePoissonsActuels);
        
        if (this.capaciteMaxPiscine != null && this.capaciteMaxPiscine > 0) {
            this.tauxOccupation = BigDecimal.valueOf(this.nombrePoissonsActuels)
                .divide(BigDecimal.valueOf(this.capaciteMaxPiscine), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        } else {
            this.tauxOccupation = BigDecimal.ZERO;
        }
    }
}

