package com.example.poisson.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j; // AJOUTEZ CET IMPORT
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plat")
@Data
@Slf4j // AJOUTEZ CETTE ANNOTATION
public class Plat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_plat")
    private Long idPlat;
    
    @Column(name = "nom_plat", nullable = false, length = 100)
    private String nomPlat;
    
    @Column(name = "description_plat", columnDefinition = "TEXT")
    private String descriptionPlat;
    
    @Column(name = "poids_total_plat", precision = 10, scale = 2)
    private BigDecimal poidsTotalPlat = BigDecimal.ZERO;
    
    @Column(name = "cout_total_plat", precision = 10, scale = 2)
    private BigDecimal coutTotalPlat = BigDecimal.ZERO;
    
    @Column(name = "proteines_total_plat", precision = 8, scale = 2)
    private BigDecimal proteinesTotalPlat = BigDecimal.ZERO;
    
    @Column(name = "glucides_total_plat", precision = 8, scale = 2)
    private BigDecimal glucidesTotalPlat = BigDecimal.ZERO;
    
    @Column(name = "proteines_par_kg_plat", precision = 8, scale = 2)
    private BigDecimal proteinesParKgPlat = BigDecimal.ZERO;
    
    @Column(name = "glucides_par_kg_plat", precision = 8, scale = 2)
    private BigDecimal glucidesParKgPlat = BigDecimal.ZERO;
    
    @Column(name = "date_preparation_plat")
    private LocalDate datePreparationPlat = LocalDate.now();
    
    @Column(name = "est_utilise_plat")
    private Boolean estUtilisePlat = false;
    
    @Column(name = "date_creation_plat")
    private LocalDateTime dateCreationPlat = LocalDateTime.now();
    
    // IMPORTANT : Ajoutez cascade = CascadeType.ALL pour que les compositions soient sauvegardées avec le plat
    @OneToMany(mappedBy = "plat", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<CompositionPlat> compositions = new ArrayList<>();
    
    // NOUVELLE MÉTHODE : Appelée après chaque chargement de l'entité
    @PostLoad
    @PostPersist
    @PostUpdate
    public void calculerTotaux() {
        log.info("🔄 DEBUT calculTotaux() pour plat ID: {}, Nom: {}", 
            this.idPlat, this.nomPlat);
        
        BigDecimal totalPoids = BigDecimal.ZERO;
        BigDecimal totalCout = BigDecimal.ZERO;
        BigDecimal totalProteines = BigDecimal.ZERO;
        BigDecimal totalGlucides = BigDecimal.ZERO;
        
        if (compositions != null) {
            log.info("  Nombre de compositions: {}", compositions.size());
            
            for (CompositionPlat comp : compositions) {
                if (comp.getPoidsAlimentComposition() != null) {
                    totalPoids = totalPoids.add(comp.getPoidsAlimentComposition());
                    log.info("  + Composition: {}kg", comp.getPoidsAlimentComposition());
                }
                if (comp.getCoutAlimentComposition() != null) {
                    totalCout = totalCout.add(comp.getCoutAlimentComposition());
                }
                if (comp.getProteinesComposition() != null) {
                    totalProteines = totalProteines.add(comp.getProteinesComposition());
                }
                if (comp.getGlucidesComposition() != null) {
                    totalGlucides = totalGlucides.add(comp.getGlucidesComposition());
                }
            }
        } else {
            log.warn("  ⚠️ List compositions est null!");
        }
        
        this.poidsTotalPlat = totalPoids;
        this.coutTotalPlat = totalCout;
        this.proteinesTotalPlat = totalProteines;
        this.glucidesTotalPlat = totalGlucides;
        
        log.info("  = Poids total calculé: {}kg", totalPoids);
        
        // Calculer par kg
        if (this.poidsTotalPlat.compareTo(BigDecimal.ZERO) > 0) {
            this.proteinesParKgPlat = this.proteinesTotalPlat
                .multiply(new BigDecimal("1000"))
                .divide(this.poidsTotalPlat, 2, RoundingMode.HALF_UP);
            
            this.glucidesParKgPlat = this.glucidesTotalPlat
                .multiply(new BigDecimal("1000"))
                .divide(this.poidsTotalPlat, 2, RoundingMode.HALF_UP);
        } else {
            this.proteinesParKgPlat = BigDecimal.ZERO;
            this.glucidesParKgPlat = BigDecimal.ZERO;
            log.warn("  ⚠️ Poids total = 0, impossible de calculer par kg");
        }
        
        log.info("✅ FIN calculTotaux() - Poids: {}kg, Coût: {}MGA", 
            this.poidsTotalPlat, this.coutTotalPlat);
    }
    
    // Méthode pour ajouter une composition (utile pour l'interface)
    public void ajouterComposition(Aliment aliment, BigDecimal poids) {
        CompositionPlat composition = new CompositionPlat();
        composition.setPlat(this);
        composition.setAliment(aliment);
        composition.setPoidsAlimentComposition(poids);
        
        // La méthode @PrePersist de CompositionPlat calculera automatiquement
        // le coût et les nutriments
        
        if (this.compositions == null) {
            this.compositions = new ArrayList<>();
        }
        
        this.compositions.add(composition);
        
        // Recalculer les totaux immédiatement
        calculerTotaux();
    }
    
    // Méthode pour retirer une composition
    public void retirerComposition(Long idAliment) {
        if (this.compositions != null) {
            this.compositions.removeIf(comp -> 
                comp.getAliment() != null && 
                comp.getAliment().getIdAliment().equals(idAliment)
            );
            calculerTotaux();
        }
    }
}


