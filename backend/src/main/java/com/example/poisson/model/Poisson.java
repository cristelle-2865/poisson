package com.example.poisson.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.math.RoundingMode;  // AJOUTER CET IMPORT
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "poisson")
@Data
public class Poisson {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_poisson")
    private Long idPoisson;
    
    @ManyToOne
    @JoinColumn(name = "id_racepoisson", nullable = false)
    private RacePoisson racePoisson;
    
    @Column(name = "nom_poisson", nullable = false, length = 100)
    private String nomPoisson;
    
    @Column(name = "prix_achat_poisson", nullable = false, precision = 10, scale = 2)
    private BigDecimal prixAchatPoisson;
    
    @Column(name = "prix_vente_poisson", nullable = false, precision = 10, scale = 2)
    private BigDecimal prixVentePoisson;
    
    @Column(name = "poids_maximal_poisson", nullable = false, precision = 8, scale = 2)
    private BigDecimal poidsMaximalPoisson;
    
    @Column(name = "capacite_augmentation_poisson", nullable = false, precision = 8, scale = 2)
    private BigDecimal capaciteAugmentationPoisson;
    
    @Column(name = "poids_initial_poisson", nullable = false, precision = 8, scale = 2)
    private BigDecimal poidsInitialPoisson;
    
    @Column(name = "poids_actuel_poisson", nullable = false, precision = 8, scale = 2)
    private BigDecimal poidsActuelPoisson;
    
    @Column(name = "date_arrivee_poisson", nullable = false)
    private LocalDate dateArriveePoisson;
    
    @Column(name = "date_vente_poisson")
    private LocalDate dateVentePoisson;
    
    @Column(name = "est_rassasie_poisson")
    private Boolean estRassasiePoisson = false;
    
    @Column(name = "est_vendu_poisson")
    private Boolean estVenduPoisson = false;
    
    @Column(name = "est_envie_poisson")
    private Boolean estEnViePoisson = true;
    
    @Column(name = "date_creation_poisson")
    private LocalDateTime dateCreationPoisson = LocalDateTime.now();
    
    @Column(name = "date_modification_poisson")
    private LocalDateTime dateModificationPoisson = LocalDateTime.now();
    
    // Relation avec le bassin actuel
    @ManyToOne
    @JoinColumn(name = "id_piscine_actuel")
    private Piscine piscineActuelle;
    
    // Relation avec l'historique des bassins
    @OneToMany(mappedBy = "poisson", fetch = FetchType.LAZY)
    private List<AffectationPiscine> affectationsPiscine = new ArrayList<>();
    
    // AJOUTER CETTE MÉTHODE :
    public BigDecimal getGainMaximumPossible() {
        BigDecimal poidsRestant = poidsMaximalPoisson.subtract(poidsActuelPoisson);
        return poidsRestant.min(capaciteAugmentationPoisson);
    }
    
    // Méthode existante pour vérifier si prêt à vendre
    public boolean estPretAVendre() {
        if (estVenduPoisson || !estEnViePoisson) {
            return false;
        }
        
        BigDecimal pourcentage = poidsActuelPoisson
            .divide(poidsMaximalPoisson, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
        
        return pourcentage.compareTo(BigDecimal.valueOf(95)) >= 0;
    }


    // Ajoutez ces méthodes utilitaires
    @Transient
    public void assignToPiscine(Piscine piscine) {
        this.piscineActuelle = piscine;
    }

    @Transient
    public void removeFromPiscine() {
        this.piscineActuelle = null;
    }
}

