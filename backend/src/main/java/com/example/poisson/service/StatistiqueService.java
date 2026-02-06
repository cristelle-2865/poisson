// StatistiqueService.java - Mise à jour
package com.example.poisson.service;

import com.example.poisson.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatistiqueService {
    
    private final AlimentRepository alimentRepository;
    private final PoissonRepository poissonRepository;
    private final PiscineRepository piscineRepository;
    private final FisakafoananaRepository fisakafoananaRepository;
    
    public Map<String, Object> getStatistiquesCompletes() {
        Map<String, Object> stats = new HashMap<>();
        
        // Statistiques des poissons
        long poissonsEnVie = poissonRepository.findByEstVenduPoissonFalseAndEstEnViePoissonTrue().size();
        long poissonsVendus = poissonRepository.findAll().stream()
            .filter(p -> p.getEstVenduPoisson() != null && p.getEstVenduPoisson())
            .count();
        
        // Statistiques des bassins
        long bassinsActifs = piscineRepository.findByEstActivePiscineTrue().size();
        long bassinsPleins = piscineRepository.findAll().stream()
            .filter(p -> p.getNombrePoissonsActuels() >= p.getCapaciteMaxPiscine())
            .count();
        
        // Statistiques des aliments
        BigDecimal stockTotal = alimentRepository.findAll().stream()
            .map(aliment -> aliment.getStockAliment() != null ? aliment.getStockAliment() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long alimentsStockBas = alimentRepository.findAll().stream()
            .filter(aliment -> aliment.getStockAliment() != null && 
                    aliment.getStockAliment().compareTo(BigDecimal.valueOf(10)) < 0)
            .count();
        
        // Gain de poids total
        BigDecimal gainPoidsTotal = fisakafoananaRepository.findAll().stream()
            .map(f -> f.getGainPoidsFisakafoanana() != null ? f.getGainPoidsFisakafoanana() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        stats.put("poissonsEnVie", poissonsEnVie);
        stats.put("poissonsVendus", poissonsVendus);
        stats.put("bassinsActifs", bassinsActifs);
        stats.put("bassinsPleins", bassinsPleins);
        stats.put("stockAlimentTotal", stockTotal);
        stats.put("alimentsStockBas", alimentsStockBas);
        stats.put("gainPoidsTotal", gainPoidsTotal);
        stats.put("dateCalcul", LocalDate.now());
        
        return stats;
    }
    
    public Map<String, Object> getStatistiquesMensuelles(int mois, int annee) {
        Map<String, Object> stats = new HashMap<>();
        
        // Implémentez les statistiques mensuelles ici
        // ...
        
        return stats;
    }
}

