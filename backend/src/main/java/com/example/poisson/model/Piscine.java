package com.example.poisson.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.math.RoundingMode;  // AJOUTER CET IMPORT
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "piscine")
@Data
public class Piscine {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_piscine")
    private Long idPiscine;
    
    @Column(name = "nom_piscine", nullable = false, length = 100)
    private String nomPiscine;
    
    @Column(name = "capacite_max_piscine", nullable = false)
    private Integer capaciteMaxPiscine;
    
    @Column(name = "description_piscine", columnDefinition = "TEXT")
    private String descriptionPiscine;
    
    @Column(name = "est_active_piscine")
    private Boolean estActivePiscine = true;
    
    @Column(name = "date_creation_piscine")
    private LocalDateTime dateCreationPiscine = LocalDateTime.now();
    
    @Column(name = "volume_piscine", precision = 10, scale = 2)
    private BigDecimal volumePiscine;
    
    @Column(name = "temperature_piscine", precision = 5, scale = 2)
    private BigDecimal temperaturePiscine;
    
    @Column(name = "ph_piscine", precision = 4, scale = 2)
    private BigDecimal phPiscine;
    
    // Relation avec les poissons
    @OneToMany(mappedBy = "piscineActuelle", fetch = FetchType.LAZY)
    private List<Poisson> poissons = new ArrayList<>();
    
    @Transient
    public Integer getNombrePoissonsActuels() {
        return poissons != null ? poissons.size() : 0;
    }
    
    @Transient
    public Integer getCapaciteRestante() {
        if (capaciteMaxPiscine == null) return 0;
        return capaciteMaxPiscine - getNombrePoissonsActuels();
    }
    
    @Transient
    public BigDecimal getTauxOccupation() {
        if (capaciteMaxPiscine == null || capaciteMaxPiscine == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(getNombrePoissonsActuels())
            .divide(BigDecimal.valueOf(capaciteMaxPiscine), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }
}

