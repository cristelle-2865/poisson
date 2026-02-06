// PlatService.java
package com.example.poisson.service;

import com.example.poisson.model.*;
import com.example.poisson.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlatService {
    
    private final PlatRepository platRepository;
    private final AlimentRepository alimentRepository;
    private final CompositionPlatRepository compositionPlatRepository;
    
    public List<Plat> getAllPlats() {
        return platRepository.findAll();
    }
    
    public List<Plat> getPlatsDisponibles() {
        return platRepository.findPlatsDisponibles();
    }
    
    public Plat getPlatById(Long id) {
        return platRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Plat non trouvé avec l'ID: " + id));
    }
    
    @Transactional
    public Plat createPlat(Plat plat) {
        if (plat.getNomPlat() == null || plat.getNomPlat().trim().isEmpty()) {
            throw new RuntimeException("Le nom du plat est requis");
        }
        
        Plat savedPlat = platRepository.save(plat);
        
        // Sauvegarder les compositions si elles existent
        if (plat.getCompositions() != null && !plat.getCompositions().isEmpty()) {
            for (CompositionPlat composition : plat.getCompositions()) {
                composition.setPlat(savedPlat);
                compositionPlatRepository.save(composition);
            }
            savedPlat.calculerTotaux();
            platRepository.save(savedPlat);
        }
        
        return savedPlat;
    }
    
    @Transactional
    public Plat updatePlat(Long id, Plat platDetails) {
        Plat plat = getPlatById(id);
        
        plat.setNomPlat(platDetails.getNomPlat());
        plat.setDescriptionPlat(platDetails.getDescriptionPlat());
        plat.setDatePreparationPlat(platDetails.getDatePreparationPlat());
        
        // Mettre à jour les compositions si fournies
        if (platDetails.getCompositions() != null) {
            // Supprimer les anciennes compositions
            compositionPlatRepository.deleteByPlatIdPlat(id);
            
            // Ajouter les nouvelles compositions
            for (CompositionPlat composition : platDetails.getCompositions()) {
                composition.setPlat(plat);
                compositionPlatRepository.save(composition);
            }
            
            plat.calculerTotaux();
        }
        
        return platRepository.save(plat);
    }
    
    public void deletePlat(Long id) {
        Plat plat = getPlatById(id);
        
        // Vérifier si le plat a été utilisé
        if (plat.getEstUtilisePlat()) {
            throw new RuntimeException("Impossible de supprimer un plat déjà utilisé");
        }
        
        platRepository.delete(plat);
    }
    
    @Transactional
    public Plat ajouterAlimentAuPlat(Long idPlat, Long idAliment, BigDecimal poids) {
        Plat plat = getPlatById(idPlat);
        Aliment aliment = alimentRepository.findById(idAliment)
            .orElseThrow(() -> new RuntimeException("Aliment non trouvé avec l'ID: " + idAliment));
        
        // Vérifier si l'aliment a suffisamment de stock
        if (aliment.getStockAliment().compareTo(poids) < 0) {
            throw new RuntimeException("Stock insuffisant pour l'aliment: " + aliment.getNomAliment());
        }
        
        // Créer la composition
        CompositionPlat composition = new CompositionPlat();
        composition.setPlat(plat);
        composition.setAliment(aliment);
        composition.setPoidsAlimentComposition(poids);
        
        compositionPlatRepository.save(composition);
        
        // Mettre à jour les totaux du plat
        plat.calculerTotaux();
        
        return platRepository.save(plat);
    }
    
    @Transactional
    public Plat retirerAlimentDuPlat(Long idPlat, Long idAliment) {
        Plat plat = getPlatById(idPlat);
        
        // Trouver et supprimer la composition
        compositionPlatRepository.findByPlatIdPlat(idPlat).stream()
            .filter(comp -> comp.getAliment().getIdAliment().equals(idAliment))
            .findFirst()
            .ifPresent(compositionPlatRepository::delete);
        
        // Mettre à jour les totaux du plat
        plat.calculerTotaux();
        
        return platRepository.save(plat);
    }
    
    @Transactional
    public Plat utiliserPlat(Long idPlat) {
        Plat plat = getPlatById(idPlat);
        
        if (plat.getEstUtilisePlat()) {
            throw new RuntimeException("Ce plat a déjà été utilisé");
        }
        
        if (plat.getPoidsTotalPlat().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Le plat est vide");
        }
        
        // Diminuer le stock des aliments
        List<CompositionPlat> compositions = compositionPlatRepository.findByPlatIdPlat(idPlat);
        for (CompositionPlat composition : compositions) {
            Aliment aliment = composition.getAliment();
            BigDecimal nouveauStock = aliment.getStockAliment()
                .subtract(composition.getPoidsAlimentComposition());
            
            if (nouveauStock.compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("Stock insuffisant pour l'aliment: " + aliment.getNomAliment());
            }
            
            aliment.setStockAliment(nouveauStock);
            alimentRepository.save(aliment);
        }
        
        // Marquer le plat comme utilisé
        plat.setEstUtilisePlat(true);
        
        return platRepository.save(plat);
    }
    
    public Map<String, Object> getStatistiquesPlat(Long idPlat) {
        Plat plat = getPlatById(idPlat);
        List<CompositionPlat> compositions = compositionPlatRepository.findByPlatIdPlat(idPlat);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("plat", plat);
        stats.put("nombreIngredients", compositions.size());
        stats.put("compositions", compositions);
        
        return stats;
    }
    
    public BigDecimal getStockTotalPlatsDisponibles() {
        return platRepository.sumPoidsTotalPlatsDisponibles();
    }
}

