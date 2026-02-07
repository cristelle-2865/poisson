package com.example.poisson.dto;


import com.example.poisson.model.Piscine;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PiscineDTO {
    private Long idPiscine;
    private String nomPiscine;
    private Integer capaciteMaxPiscine;
    private String descriptionPiscine;
    private Boolean estActivePiscine;
    private LocalDateTime dateCreationPiscine;
    private BigDecimal volumePiscine;
    private BigDecimal temperaturePiscine;
    private BigDecimal phPiscine;
    private Integer nombrePoissonsActuels;
    private Integer capaciteRestante;
    private BigDecimal tauxOccupation;
    
    public PiscineDTO(Piscine piscine) {
        this.idPiscine = piscine.getIdPiscine();
        this.nomPiscine = piscine.getNomPiscine();
        this.capaciteMaxPiscine = piscine.getCapaciteMaxPiscine();
        this.descriptionPiscine = piscine.getDescriptionPiscine();
        this.estActivePiscine = piscine.getEstActivePiscine();
        this.dateCreationPiscine = piscine.getDateCreationPiscine();
        this.volumePiscine = piscine.getVolumePiscine();
        this.temperaturePiscine = piscine.getTemperaturePiscine();
        this.phPiscine = piscine.getPhPiscine();
        this.nombrePoissonsActuels = piscine.getNombrePoissonsActuels();
        this.capaciteRestante = piscine.getCapaciteRestante();
        this.tauxOccupation = piscine.getTauxOccupation();
    }
}
