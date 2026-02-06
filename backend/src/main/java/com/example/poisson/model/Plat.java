// Plat.java
package com.example.poisson.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plat")
@Data
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
    
    @OneToMany(mappedBy = "plat", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<CompositionPlat> compositions = new ArrayList<>();
    
    // Méthodes pour calculer les totaux
    @PostPersist
    @PostUpdate
    @PostLoad
    public void calculerTotaux() {
        if (compositions != null && !compositions.isEmpty()) {
            poidsTotalPlat = compositions.stream()
                .map(CompositionPlat::getPoidsAlimentComposition)
                .filter(poids -> poids != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            coutTotalPlat = compositions.stream()
                .map(CompositionPlat::getCoutAlimentComposition)
                .filter(cout -> cout != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            proteinesTotalPlat = compositions.stream()
                .map(CompositionPlat::getProteinesComposition)
                .filter(proteines -> proteines != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            glucidesTotalPlat = compositions.stream()
                .map(CompositionPlat::getGlucidesComposition)
                .filter(glucides -> glucides != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Calculer par kg (grammes par kg)
            if (poidsTotalPlat.compareTo(BigDecimal.ZERO) > 0) {
                proteinesParKgPlat = proteinesTotalPlat
                    .multiply(new BigDecimal("1000"))
                    .divide(poidsTotalPlat, 2, RoundingMode.HALF_UP);
                
                glucidesParKgPlat = glucidesTotalPlat
                    .multiply(new BigDecimal("1000"))
                    .divide(poidsTotalPlat, 2, RoundingMode.HALF_UP);
            }
        }
    }
    
    // Méthode pour ajouter une composition
    public void ajouterComposition(Aliment aliment, BigDecimal poids) {
        CompositionPlat composition = new CompositionPlat();
        composition.setPlat(this);
        composition.setAliment(aliment);
        composition.setPoidsAlimentComposition(poids);
        compositions.add(composition);
        calculerTotaux();
    }
    
    // Méthode pour retirer une composition
    public void retirerComposition(Long idAliment) {
        compositions.removeIf(comp -> comp.getAliment().getIdAliment().equals(idAliment));
        calculerTotaux();
    }
}

