package com.example.poisson.service;

import com.example.poisson.model.*;
import com.example.poisson.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class NourrissageService {
    
    private final PoissonRepository poissonRepository;
    private final FisakafoananaRepository fisakafoananaRepository;
    private final PlatRepository platRepository; // AJOUTER CETTE LIGNE
    
    // Constantes selon le cahier des charges
    private static final BigDecimal BESOIN_PROTEINES = new BigDecimal("2.0");
    private static final BigDecimal BESOIN_GLUCIDES = new BigDecimal("4.0");
    private static final BigDecimal GAIN_MAX = new BigDecimal("20.0");
    
    @Transactional
    public Map<String, Object> nourrirPoissons(BigDecimal quantitePlatTotal, BigDecimal proteinesParKg, BigDecimal glucidesParKg) {
        
        // Convertir kg en grammes pour les calculs
        BigDecimal platTotalGrammes = quantitePlatTotal.multiply(new BigDecimal("1000"));
        
        // Calcul des nutriments totaux dans le plat
        BigDecimal proteinesTotales = proteinesParKg.multiply(quantitePlatTotal);
        BigDecimal glucidesTotales = glucidesParKg.multiply(quantitePlatTotal);
        
        // Récupérer les poissons affamés
        List<Poisson> poissonsAffames = poissonRepository
            .findByEstRassasiePoissonFalseAndEstVenduPoissonFalseAndEstEnViePoissonTrue();
        
        if (poissonsAffames.isEmpty()) {
            return Map.of(
                "message", "Aucun poisson affamé",
                "poissonsNourris", 0,
                "nourritureRestante", platTotalGrammes
            );
        }
        
        // Calcul du besoin total (règle de trois)
        // Pour 5g de plat : 2g protéines + 4g glucides
        BigDecimal besoinPlatParPoisson = new BigDecimal("5.0");
        BigDecimal besoinTotal = besoinPlatParPoisson.multiply(new BigDecimal(poissonsAffames.size()));
        
        // Vérifier si assez de nourriture
        if (platTotalGrammes.compareTo(besoinTotal) < 0) {
            // Répartir proportionnellement
            return repartirNourritureLimitee(poissonsAffames, platTotalGrammes, proteinesParKg, glucidesParKg);
        }
        
        // Assez de nourriture pour tous
        return nourrirTousPoissons(poissonsAffames, besoinPlatParPoisson, proteinesParKg, glucidesParKg);
    }
    
    private Map<String, Object> nourrirTousPoissons(List<Poisson> poissons, 
                                                   BigDecimal quantiteParPoisson,
                                                   BigDecimal proteinesParKg, 
                                                   BigDecimal glucidesParKg) {
        
        List<Fisakafoanana> historiques = new ArrayList<>();
        int poissonsNourris = 0;
        
        for (Poisson poisson : poissons) {
            Fisakafoanana historique = nourrirUnPoisson(
                poisson, 
                quantiteParPoisson, 
                proteinesParKg, 
                glucidesParKg
            );
            
            historiques.add(historique);
            poissonsNourris++;
            
            // Marquer comme rassasié
            poisson.setEstRassasiePoisson(true);
            poissonRepository.save(poisson);
        }
        
        fisakafoananaRepository.saveAll(historiques);
        
        return Map.of(
            "message", "Tous les poissons ont été nourris",
            "poissonsNourris", poissonsNourris,
            "nourritureUtilisee", quantiteParPoisson.multiply(new BigDecimal(poissons.size())),
            "date", LocalDate.now()
        );
    }
    
    private Fisakafoanana nourrirUnPoisson(Poisson poisson, BigDecimal quantitePlat,
                                          BigDecimal proteinesParKg, BigDecimal glucidesParKg) {
        
        // Calcul des nutriments reçus (en grammes)
        BigDecimal proteinesRecues = quantitePlat
            .multiply(proteinesParKg)
            .divide(new BigDecimal("1000"), 4, RoundingMode.HALF_UP);
        
        BigDecimal glucidesRecus = quantitePlat
            .multiply(glucidesParKg)
            .divide(new BigDecimal("1000"), 4, RoundingMode.HALF_UP);
        
        // Calcul du gain de poids selon les règles
        BigDecimal gainPoids = calculerGainPoids(proteinesRecues, glucidesRecus);
        
        // Limiter par capacité d'augmentation et poids max
        gainPoids = gainPoids.min(poisson.getCapaciteAugmentationPoisson());
        
        // CORRECTION ICI : Utiliser la nouvelle méthode ou calculer directement
        BigDecimal gainMaximumPossible = poisson.getPoidsMaximalPoisson()
            .subtract(poisson.getPoidsActuelPoisson())
            .min(poisson.getCapaciteAugmentationPoisson());
        
        gainPoids = gainPoids.min(gainMaximumPossible);
        
        BigDecimal nouveauPoids = poisson.getPoidsActuelPoisson().add(gainPoids);
        
        // Calcul du taux de satisfaction
        BigDecimal tauxProteines = proteinesRecues.divide(BESOIN_PROTEINES, 4, RoundingMode.HALF_UP);
        BigDecimal tauxGlucides = glucidesRecus.divide(BESOIN_GLUCIDES, 4, RoundingMode.HALF_UP);
        BigDecimal tauxSatisfaction = tauxProteines.add(tauxGlucides)
            .divide(new BigDecimal("2"), 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"));
        
        // Créer l'historique
        Fisakafoanana historique = new Fisakafoanana();
        historique.setPoisson(poisson);
        historique.setDateNourrissageFisakafoanana(LocalDate.now());
        historique.setAncienPoidsFisakafoanana(poisson.getPoidsActuelPoisson());
        historique.setNouveauPoidsFisakafoanana(nouveauPoids);
        historique.setQuantiteNourritureFisakafoanana(quantitePlat);
        historique.setProteinesRecuesFisakafoanana(proteinesRecues);
        historique.setGlucidesRecusFisakafoanana(glucidesRecus);
        historique.setGainPoidsFisakafoanana(gainPoids);
        historique.setTauxSatisfactionFisakafoanana(tauxSatisfaction);
        historique.setBesoinsSatisfaitsFisakafoanana(
            tauxProteines.compareTo(BigDecimal.ONE) >= 0 && 
            tauxGlucides.compareTo(BigDecimal.ONE) >= 0
        );
        
        // Mettre à jour le poisson
        poisson.setPoidsActuelPoisson(nouveauPoids);
        poisson.setDateModificationPoisson(java.time.LocalDateTime.now());
        
        return historique;
    }
    
    private BigDecimal calculerGainPoids(BigDecimal proteines, BigDecimal glucides) {
        // Règles exactes du cahier des charges
        
        // Cas 1: 2g protéines + 4g glucides = 10g gain
        if (proteines.compareTo(BESOIN_PROTEINES) == 0 && 
            glucides.compareTo(BESOIN_GLUCIDES) == 0) {
            return new BigDecimal("10.0");
        }
        
        // Cas 2: 2g protéines + 0g glucides = 5g gain
        if (proteines.compareTo(BESOIN_PROTEINES) == 0 && 
            glucides.compareTo(BigDecimal.ZERO) == 0) {
            return new BigDecimal("5.0");
        }
        
        // Cas 3: 0g protéines + 4g glucides = 5g gain
        if (proteines.compareTo(BigDecimal.ZERO) == 0 && 
            glucides.compareTo(BESOIN_GLUCIDES) == 0) {
            return new BigDecimal("5.0");
        }
        
        // Cas 4: 4g protéines + 4g glucides = 15g gain
        if (proteines.compareTo(new BigDecimal("4.0")) == 0 && 
            glucides.compareTo(BESOIN_GLUCIDES) == 0) {
            return new BigDecimal("15.0");
        }
        
        // Cas général: calcul proportionnel
        BigDecimal tauxProteines = proteines.divide(BESOIN_PROTEINES, 4, RoundingMode.HALF_UP);
        BigDecimal tauxGlucides = glucides.divide(BESOIN_GLUCIDES, 4, RoundingMode.HALF_UP);
        BigDecimal tauxMoyen = tauxProteines.add(tauxGlucides).divide(new BigDecimal("2"), 4, RoundingMode.HALF_UP);
        
        return tauxMoyen.multiply(new BigDecimal("10.0"));
    }
    
    private Map<String, Object> repartirNourritureLimitee(List<Poisson> poissons, 
                                                         BigDecimal platTotal,
                                                         BigDecimal proteinesParKg,
                                                         BigDecimal glucidesParKg) {
        
        BigDecimal besoinParPoisson = new BigDecimal("5.0");
        int nombrePoissonsNourris = platTotal.divide(besoinParPoisson, 0, RoundingMode.FLOOR).intValue();
        
        List<Fisakafoanana> historiques = new ArrayList<>();
        
        for (int i = 0; i < Math.min(nombrePoissonsNourris, poissons.size()); i++) {
            Poisson poisson = poissons.get(i);
            
            Fisakafoanana historique = nourrirUnPoisson(
                poisson, 
                besoinParPoisson, 
                proteinesParKg, 
                glucidesParKg
            );
            
            historiques.add(historique);
            poisson.setEstRassasiePoisson(true);
            poissonRepository.save(poisson);
        }
        
        fisakafoananaRepository.saveAll(historiques);
        
        BigDecimal nourritureRestante = platTotal.subtract(
            besoinParPoisson.multiply(new BigDecimal(nombrePoissonsNourris))
        );
        
        return Map.of(
            "message", "Nourriture limitée, certains poissons nourris",
            "poissonsNourris", nombrePoissonsNourris,
            "poissonsAffamesRestants", poissons.size() - nombrePoissonsNourris,
            "nourritureRestante", nourritureRestante,
            "date", LocalDate.now()
        );
    }
    
    // Nouvelle méthode simplifiée pour le contrôleur
    @Transactional
    public Map<String, Object> nourrirPoissonsSimple(BigDecimal quantitePlat) {
        // Valeurs par défaut pour les larves
        BigDecimal proteinesParKg = new BigDecimal("10.0");
        BigDecimal glucidesParKg = new BigDecimal("10.0");
        
        return nourrirPoissons(quantitePlat, proteinesParKg, glucidesParKg);
    }

    // NourrissageService.java - Ajouter cette méthode
    @Transactional
    public Map<String, Object> nourrirAvecPlat(Long idPlat) {
        Plat plat = platRepository.findById(idPlat)
            .orElseThrow(() -> new RuntimeException("Plat non trouvé avec l'ID: " + idPlat));
        
        if (plat.getEstUtilisePlat()) {
            throw new RuntimeException("Ce plat a déjà été utilisé");
        }
        
        if (plat.getPoidsTotalPlat().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Le plat est vide");
        }
        
        // Utiliser les valeurs nutritives du plat
        BigDecimal proteinesParKg = plat.getProteinesParKgPlat();
        BigDecimal glucidesParKg = plat.getGlucidesParKgPlat();
        
        // Convertir kg en grammes
        BigDecimal quantitePlatKg = plat.getPoidsTotalPlat().divide(new BigDecimal("1000"), 4, RoundingMode.HALF_UP);
        
        // Nourrir les poissons avec ce plat
        Map<String, Object> result = nourrirPoissons(quantitePlatKg, proteinesParKg, glucidesParKg);
        
        // Marquer le plat comme utilisé
        plat.setEstUtilisePlat(true);
        platRepository.save(plat);
        
        // Ajouter l'information du plat au résultat
        result.put("platUtilise", plat.getNomPlat());
        result.put("idPlat", plat.getIdPlat());
        
        return result;
    }
}

