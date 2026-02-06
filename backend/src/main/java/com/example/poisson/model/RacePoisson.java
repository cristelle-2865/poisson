package com.example.poisson.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "racepoisson")
@Data
public class RacePoisson {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_racepoisson")
    private Long idRacePoisson;
    
    @Column(name = "nom_racepoisson", nullable = false, unique = true, length = 100)
    private String nomRacePoisson;
    
    @Column(name = "description_racepoisson", columnDefinition = "TEXT")
    private String descriptionRacePoisson;
    
    @Column(name = "date_creation_racepoisson")
    private LocalDateTime dateCreationRacePoisson = LocalDateTime.now();
    
    @Column(name = "est_actif_racepoisson")
    private Boolean estActifRacePoisson = true;
}

