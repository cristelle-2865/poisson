package com.example.poisson.service;

import com.example.poisson.model.AffectationPiscine;
import com.example.poisson.dto.PoissonDetailDTO; 
import com.example.poisson.model.Piscine;
import com.example.poisson.model.Poisson;
import com.example.poisson.repository.AffectationPiscineRepository;
import com.example.poisson.repository.PiscineRepository;
import com.example.poisson.repository.PoissonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PoissonService {
    
    private final PoissonRepository poissonRepository;
    private final PiscineRepository piscineRepository;
    private final AffectationPiscineRepository affectationPiscineRepository;
    
    public List<Poisson> getAllPoissons() {
        return poissonRepository.findAll();
    }
    
    public List<Poisson> getPoissonsEnVie() {
        return poissonRepository.findByEstVenduPoissonFalseAndEstEnViePoissonTrue();
    }
    
    public List<Poisson> getPoissonsAffames() {
        return poissonRepository.findByEstRassasiePoissonFalseAndEstVenduPoissonFalseAndEstEnViePoissonTrue();
    }
    
    public List<Poisson> getPoissonsPretAVendre() {
        return poissonRepository.findByEstVenduPoissonFalseAndEstEnViePoissonTrue()
            .stream()
            .filter(Poisson::estPretAVendre)
            .toList();
    }
    
    public Poisson getPoissonById(Long id) {
        return poissonRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Poisson non trouvé avec l'ID: " + id));
    }
    
    public Poisson getPoissonByIdWithRelations(Long id) {
        Poisson poisson = poissonRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Poisson non trouvé avec l'ID: " + id));
        
        // Initialiser les relations si nécessaire (pour éviter LazyInitializationException)
        // Pour les relations ManyToOne, JPA les charge généralement automatiquement
        // Cette méthode est utile pour forcer le chargement des collections si nécessaire
        
        return poisson;
    }

    public PoissonDetailDTO getPoissonDetail(Long id) {
        Poisson poisson = getPoissonByIdWithRelations(id);
        return new PoissonDetailDTO(poisson);
    }

    public Poisson createPoisson(Poisson poisson) {
        // S'assurer que le poids actuel = poids initial
        poisson.setPoidsActuelPoisson(poisson.getPoidsInitialPoisson());
        return poissonRepository.save(poisson);
    }
    
    public Poisson updatePoisson(Long id, Poisson poissonDetails) {
        try {
            Poisson poisson = getPoissonById(id);
            
            // Ne pas mettre à jour l'ID
            if (poissonDetails.getNomPoisson() != null) {
                poisson.setNomPoisson(poissonDetails.getNomPoisson());
            }
            
            if (poissonDetails.getRacePoisson() != null) {
                poisson.setRacePoisson(poissonDetails.getRacePoisson());
            }
            
            if (poissonDetails.getPrixAchatPoisson() != null) {
                poisson.setPrixAchatPoisson(poissonDetails.getPrixAchatPoisson());
            }
            
            if (poissonDetails.getPrixVentePoisson() != null) {
                poisson.setPrixVentePoisson(poissonDetails.getPrixVentePoisson());
            }
            
            if (poissonDetails.getPoidsMaximalPoisson() != null) {
                poisson.setPoidsMaximalPoisson(poissonDetails.getPoidsMaximalPoisson());
            }
            
            if (poissonDetails.getCapaciteAugmentationPoisson() != null) {
                poisson.setCapaciteAugmentationPoisson(poissonDetails.getCapaciteAugmentationPoisson());
            }
            
            if (poissonDetails.getPoidsActuelPoisson() != null) {
                poisson.setPoidsActuelPoisson(poissonDetails.getPoidsActuelPoisson());
            }
            
            if (poissonDetails.getDateArriveePoisson() != null) {
                poisson.setDateArriveePoisson(poissonDetails.getDateArriveePoisson());
            }
            
            if (poissonDetails.getEstRassasiePoisson() != null) {
                poisson.setEstRassasiePoisson(poissonDetails.getEstRassasiePoisson());
            }
            
            if (poissonDetails.getEstVenduPoisson() != null) {
                poisson.setEstVenduPoisson(poissonDetails.getEstVenduPoisson());
            }
            
            if (poissonDetails.getEstEnViePoisson() != null) {
                poisson.setEstEnViePoisson(poissonDetails.getEstEnViePoisson());
            }
            
            // Mettre à jour la date de modification
            poisson.setDateModificationPoisson(LocalDateTime.now());
            
            return poissonRepository.save(poisson);
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la mise à jour du poisson: " + e.getMessage());
        }
    }
    
    public void deletePoisson(Long id) {
        Poisson poisson = getPoissonById(id);
        poissonRepository.delete(poisson);
    }
    
    public Poisson vendrePoisson(Long id) {
        Poisson poisson = getPoissonById(id);
        poisson.setEstVenduPoisson(true);
        poisson.setDateVentePoisson(LocalDate.now());
        return poissonRepository.save(poisson);
    }
    
    public Map<String, Object> getStatistiques() {
        List<Poisson> poissonsEnVie = getPoissonsEnVie();
        List<Poisson> poissonsAffames = getPoissonsAffames();
        List<Poisson> poissonsPretAVendre = getPoissonsPretAVendre();
        
        // Calculer le poids total
        BigDecimal poidsTotal = poissonsEnVie.stream()
            .map(Poisson::getPoidsActuelPoisson)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculer le bénéfice potentiel
        BigDecimal beneficePotentiel = poissonsPretAVendre.stream()
            .map(Poisson::getPrixVentePoisson)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculer le taux de croissance moyen
        double tauxCroissanceMoyen = poissonsEnVie.stream()
            .mapToDouble(p -> p.getPoidsActuelPoisson()
                .divide(p.getPoidsMaximalPoisson(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue())
            .average()
            .orElse(0.0);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("poissonsEnVie", poissonsEnVie.size());
        stats.put("poissonsAffames", poissonsAffames.size());
        stats.put("poissonsPretAVendre", poissonsPretAVendre.size());
        stats.put("poidsTotal", poidsTotal);
        stats.put("beneficePotentiel", beneficePotentiel);
        stats.put("tauxCroissanceMoyen", tauxCroissanceMoyen);
        
        return stats;
    }
    
    // NOUVELLES MÉTHODES POUR LES BASSINS
    
    public List<Poisson> getPoissonsWithoutPiscine() {
        return poissonRepository.findByPiscineActuelleIsNullAndEstVenduPoissonFalseAndEstEnViePoissonTrue();
    }
    
    public Poisson updatePoissonWithBassin(Long id, Poisson poissonDetails) {
        Poisson poisson = getPoissonById(id);
        
        // Mettre à jour les champs de base
        if (poissonDetails.getNomPoisson() != null) {
            poisson.setNomPoisson(poissonDetails.getNomPoisson());
        }
        
        if (poissonDetails.getRacePoisson() != null) {
            poisson.setRacePoisson(poissonDetails.getRacePoisson());
        }
        
        if (poissonDetails.getPrixAchatPoisson() != null) {
            poisson.setPrixAchatPoisson(poissonDetails.getPrixAchatPoisson());
        }
        
        if (poissonDetails.getPrixVentePoisson() != null) {
            poisson.setPrixVentePoisson(poissonDetails.getPrixVentePoisson());
        }
        
        if (poissonDetails.getPoidsMaximalPoisson() != null) {
            poisson.setPoidsMaximalPoisson(poissonDetails.getPoidsMaximalPoisson());
        }
        
        if (poissonDetails.getCapaciteAugmentationPoisson() != null) {
            poisson.setCapaciteAugmentationPoisson(poissonDetails.getCapaciteAugmentationPoisson());
        }
        
        if (poissonDetails.getPoidsActuelPoisson() != null) {
            poisson.setPoidsActuelPoisson(poissonDetails.getPoidsActuelPoisson());
        }
        
        if (poissonDetails.getDateArriveePoisson() != null) {
            poisson.setDateArriveePoisson(poissonDetails.getDateArriveePoisson());
        }
        
        if (poissonDetails.getEstRassasiePoisson() != null) {
            poisson.setEstRassasiePoisson(poissonDetails.getEstRassasiePoisson());
        }
        
        // Gérer le bassin si fourni
        if (poissonDetails.getPiscineActuelle() != null && 
            poissonDetails.getPiscineActuelle().getIdPiscine() != null) {
            
            // Trouver le nouveau bassin
            Piscine nouvellePiscine = piscineRepository.findById(
                poissonDetails.getPiscineActuelle().getIdPiscine()
            ).orElseThrow(() -> new RuntimeException("Bassin non trouvé avec l'ID: " + 
                poissonDetails.getPiscineActuelle().getIdPiscine()));
            
            // Vérifier si c'est un changement de bassin
            boolean changementBassin = poisson.getPiscineActuelle() == null || 
                !poisson.getPiscineActuelle().getIdPiscine().equals(nouvellePiscine.getIdPiscine());
            
            if (changementBassin) {
                // Retirer de l'ancien bassin si présent
                if (poisson.getPiscineActuelle() != null) {
                    affectationPiscineRepository.findCurrentAffectation(id)
                        .ifPresent(affectation -> {
                            affectation.setDateSortieAffectation(LocalDate.now());
                            affectation.setRaisonSortieAffectation("Transfert");
                            affectationPiscineRepository.save(affectation);
                        });
                }
                
                // Créer la nouvelle affectation
                AffectationPiscine nouvelleAffectation = new AffectationPiscine();
                nouvelleAffectation.setPiscine(nouvellePiscine);
                nouvelleAffectation.setPoisson(poisson);
                nouvelleAffectation.setDateEntreeAffectation(LocalDate.now());
                affectationPiscineRepository.save(nouvelleAffectation);
                
                // Mettre à jour le poisson
                poisson.setPiscineActuelle(nouvellePiscine);
            }
        }
        
        // Mettre à jour la date de modification
        poisson.setDateModificationPoisson(LocalDateTime.now());
        
        return poissonRepository.save(poisson);
    }
    
    // Cette méthode est un doublon de updatePoissonWithBassin, vous pouvez la supprimer
    // public Poisson updatePoissonWithPiscine(Long id, Poisson poissonDetails) {
    //     // Cette méthode fait la même chose que updatePoissonWithBassin
    //     // Vous pouvez la supprimer ou la garder comme alias
    //     return updatePoissonWithBassin(id, poissonDetails);
    // }

    public Poisson createPoissonWithPiscine(Poisson poisson) {
        // Créer le poisson d'abord
        poisson.setPoidsActuelPoisson(poisson.getPoidsInitialPoisson());
        Poisson savedPoisson = poissonRepository.save(poisson);
        
        // Si un bassin est spécifié, l'affecter
        if (poisson.getPiscineActuelle() != null && poisson.getPiscineActuelle().getIdPiscine() != null) {
            return updatePoissonWithBassin(savedPoisson.getIdPoisson(), poisson);
        }
        
        return savedPoisson;
    }
    
}

