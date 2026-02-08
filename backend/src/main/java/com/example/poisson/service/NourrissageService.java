package com.example.poisson.service;

import com.example.poisson.model.*;
import com.example.poisson.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
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
    
    // IMPORTANT: quantitePlat est en GRAMMES ici (5g par poisson)
    // proteinesParKg et glucidesParKg sont en g/kg
    
    // Convertir quantitePlat en kg pour les calculs
    BigDecimal quantitePlatKg = quantitePlat.divide(new BigDecimal("1000"), 4, RoundingMode.HALF_UP);
    
    // Calcul des nutriments reçus (en grammes)
    // Formule: (quantité en kg) × (nutriments en g/kg) = nutriments en g
    BigDecimal proteinesRecues = quantitePlatKg.multiply(proteinesParKg);
    BigDecimal glucidesRecus = quantitePlatKg.multiply(glucidesParKg);
    
    // Calcul du gain de poids selon les règles
    BigDecimal gainPoids = calculerGainPoids(proteinesRecues, glucidesRecus);
    
    // Limiter par capacité d'augmentation et poids max
    BigDecimal gainMaximumPossible = poisson.getPoidsMaximalPoisson()
        .subtract(poisson.getPoidsActuelPoisson())
        .min(poisson.getCapaciteAugmentationPoisson());
    
    gainPoids = gainPoids.min(gainMaximumPossible);
    
    // S'assurer que le gain n'est pas négatif
    if (gainPoids.compareTo(BigDecimal.ZERO) < 0) {
        gainPoids = BigDecimal.ZERO;
    }
    
    BigDecimal nouveauPoids = poisson.getPoidsActuelPoisson().add(gainPoids);
    
    // Calcul du taux de satisfaction
    // Besoins: 2g protéines + 4g glucides = 100%
    BigDecimal tauxProteines = proteinesRecues
        .divide(BESOIN_PROTEINES, 4, RoundingMode.HALF_UP)
        .min(BigDecimal.ONE); // Limiter à 100%
    
    BigDecimal tauxGlucides = glucidesRecus
        .divide(BESOIN_GLUCIDES, 4, RoundingMode.HALF_UP)
        .min(BigDecimal.ONE); // Limiter à 100%
    
    BigDecimal tauxSatisfaction = tauxProteines.add(tauxGlucides)
        .divide(new BigDecimal("2"), 4, RoundingMode.HALF_UP)
        .multiply(new BigDecimal("100"));
    
    // Créer l'historique
    Fisakafoanana historique = new Fisakafoanana();
    historique.setPoisson(poisson);
    historique.setDateNourrissageFisakafoanana(LocalDate.now());
    historique.setAncienPoidsFisakafoanana(poisson.getPoidsActuelPoisson());
    historique.setNouveauPoidsFisakafoanana(nouveauPoids);
    historique.setQuantiteNourritureFisakafoanana(quantitePlat); // En grammes
    historique.setProteinesRecuesFisakafoanana(proteinesRecues); // En grammes
    historique.setGlucidesRecusFisakafoanana(glucidesRecus); // En grammes
    historique.setGainPoidsFisakafoanana(gainPoids); // En grammes
    historique.setTauxSatisfactionFisakafoanana(tauxSatisfaction);
    historique.setBesoinsSatisfaitsFisakafoanana(
        tauxProteines.compareTo(BigDecimal.ONE) >= 0 && 
        tauxGlucides.compareTo(BigDecimal.ONE) >= 0
    );
    
    // Mettre à jour le poisson
    poisson.setPoidsActuelPoisson(nouveauPoids);
    poisson.setDateModificationPoisson(LocalDateTime.now());
    
    return historique;
}

    
  private BigDecimal calculerGainPoids(BigDecimal proteines, BigDecimal glucides) {
    // Les paramètres sont en GRAMMES (venant de nourrirUnPoisson)
    
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
    
    // Cas 5: 4g protéines + 8g glucides = 20g gain (maximum)
    if (proteines.compareTo(new BigDecimal("4.0")) == 0 && 
        glucides.compareTo(new BigDecimal("8.0")) == 0) {
        return new BigDecimal("20.0");
    }
    
    // Cas général: interpolation linéaire
    BigDecimal tauxProteines = proteines.divide(new BigDecimal("4.0"), 4, RoundingMode.HALF_UP);
    BigDecimal tauxGlucides = glucides.divide(new BigDecimal("8.0"), 4, RoundingMode.HALF_UP);
    
    // Gain de base (minimum) = 0g
    // Gain maximum = 20g
    // On calcule la moyenne des taux, limitée à 1.0
    BigDecimal tauxMoyen = tauxProteines.add(tauxGlucides)
        .divide(new BigDecimal("2"), 4, RoundingMode.HALF_UP)
        .min(BigDecimal.ONE); // Limiter à 100%
    
    return tauxMoyen.multiply(new BigDecimal("20.0"));
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

   
  @Transactional
public Map<String, Object> nourrirAvecPlat(Long idPlat) {
    log.info("🍽️ DEBUT nourrirAvecPlat - ID Plat: {}", idPlat);
    
    try {
        // 1. Récupérer le plat
        Plat plat = platRepository.findById(idPlat)
            .orElseThrow(() -> {
                log.error("❌ Plat non trouvé avec l'ID: {}", idPlat);
                return new RuntimeException("Plat non trouvé avec l'ID: " + idPlat);
            });
        
        log.info("✅ Plat trouvé: {} (ID: {})", plat.getNomPlat(), plat.getIdPlat());
        log.info("   Poids total: {} kg", plat.getPoidsTotalPlat());
        log.info("   Utilisé: {}", plat.getEstUtilisePlat());
        log.info("   Protéines par kg: {} g/kg", plat.getProteinesParKgPlat());
        log.info("   Glucides par kg: {} g/kg", plat.getGlucidesParKgPlat());
        
        // 2. Vérifications
        if (Boolean.TRUE.equals(plat.getEstUtilisePlat())) {
            log.error("❌ Plat déjà utilisé");
            throw new RuntimeException("Ce plat a déjà été utilisé");
        }
        
        if (plat.getPoidsTotalPlat() == null || 
            plat.getPoidsTotalPlat().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("❌ Plat vide: poids = {}", plat.getPoidsTotalPlat());
            throw new RuntimeException("Le plat est vide");
        }
        
        if (plat.getProteinesParKgPlat() == null || plat.getGlucidesParKgPlat() == null) {
            log.error("❌ Valeurs nutritionnelles manquantes");
            throw new RuntimeException("Valeurs nutritionnelles manquantes dans le plat");
        }
        
        // 3. Récupérer les poissons affamés
        List<Poisson> poissonsAffames = poissonRepository
            .findByEstRassasiePoissonFalseAndEstVenduPoissonFalseAndEstEnViePoissonTrue();
        
        log.info("🐟 Poissons affamés trouvés: {}", poissonsAffames.size());
        
        if (poissonsAffames.isEmpty()) {
            log.warn("⚠️ Aucun poisson affamé");
            return Map.of(
                "message", "Aucun poisson affamé à nourrir",
                "platUtilise", plat.getNomPlat(),
                "idPlat", plat.getIdPlat(),
                "quantitePlat", plat.getPoidsTotalPlat(),
                "poissonsNourris", 0,
                "nourritureUtilisee", BigDecimal.ZERO
            );
        }
        
        // 4. Convertir le poids du plat (kg → grammes pour les calculs)
        BigDecimal poidsPlatGrammes = plat.getPoidsTotalPlat()
            .multiply(new BigDecimal("1000"));
        
        log.info("📊 Conversion: {} kg = {} g", 
            plat.getPoidsTotalPlat(), poidsPlatGrammes);
        
        // 5. Calculer la quantité par poisson (5g minimum par poisson)
        BigDecimal besoinParPoisson = new BigDecimal("5.0"); // 5g par poisson
        BigDecimal besoinTotal = besoinParPoisson.multiply(
            new BigDecimal(poissonsAffames.size())
        );
        
        log.info("🧮 Besoins: {} poissons × {} g = {} g total",
            poissonsAffames.size(), besoinParPoisson, besoinTotal);
        
        // 6. Vérifier si assez de nourriture
        BigDecimal quantiteParPoisson;
        int poissonsANourrir;
        
        if (poidsPlatGrammes.compareTo(besoinTotal) >= 0) {
            // Assez pour tous les poissons
            quantiteParPoisson = besoinParPoisson;
            poissonsANourrir = poissonsAffames.size();
            log.info("✅ Assez de nourriture pour tous les poissons ({} g/poisson)", 
                quantiteParPoisson);
        } else {
            // Nourriture limitée, on nourrit proportionnellement
            poissonsANourrir = poidsPlatGrammes
                .divide(besoinParPoisson, 0, RoundingMode.FLOOR)
                .intValue();
            quantiteParPoisson = besoinParPoisson;
            log.info("⚠️ Nourriture limitée: {} poissons nourris sur {}",
                poissonsANourrir, poissonsAffames.size());
        }
        
        // 7. Nourrir les poissons
        List<Fisakafoanana> historiques = new ArrayList<>();
        BigDecimal gainTotal = BigDecimal.ZERO;
        
        for (int i = 0; i < Math.min(poissonsANourrir, poissonsAffames.size()); i++) {
            Poisson poisson = poissonsAffames.get(i);
            
            log.info("🍴 Nourrir poisson {}: {} (ID: {})", 
                i + 1, poisson.getNomPoisson(), poisson.getIdPoisson());
            
            Fisakafoanana historique = nourrirUnPoisson(
                poisson, 
                quantiteParPoisson, // en grammes
                plat.getProteinesParKgPlat(), 
                plat.getGlucidesParKgPlat()
            );
            
            historiques.add(historique);
            gainTotal = gainTotal.add(historique.getGainPoidsFisakafoanana());
            
            // Marquer comme rassasié
            poisson.setEstRassasiePoisson(true);
            poissonRepository.save(poisson);
            
            log.info("   Gain: {} g", historique.getGainPoidsFisakafoanana());
        }
        
        // 8. Sauvegarder l'historique
        fisakafoananaRepository.saveAll(historiques);
        
        // 9. Marquer le plat comme utilisé
        plat.setEstUtilisePlat(true);
        platRepository.save(plat);
        
        // 10. Calculer la nourriture utilisée
        BigDecimal nourritureUtilisee = quantiteParPoisson
            .multiply(new BigDecimal(poissonsANourrir))
            .divide(new BigDecimal("1000"), 4, RoundingMode.HALF_UP); // g → kg
        
        log.info("📈 Résumé:");
        log.info("   Poissons nourris: {}", poissonsANourrir);
        log.info("   Nourriture utilisée: {} kg ({} g)", 
            nourritureUtilisee, nourritureUtilisee.multiply(new BigDecimal("1000")));
        log.info("   Gain total: {} g", gainTotal);
        
        // 11. Retourner le résultat
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Nourrissage réussi avec le plat " + plat.getNomPlat());
        result.put("platUtilise", plat.getNomPlat());
        result.put("idPlat", plat.getIdPlat());
        result.put("quantitePlat", plat.getPoidsTotalPlat());
        result.put("poissonsNourris", poissonsANourrir);
        result.put("nourritureUtilisee", nourritureUtilisee);
        result.put("gainTotal", gainTotal);
        result.put("date", LocalDate.now());
        
        log.info("✅ FIN nourrirAvecPlat - SUCCÈS");
        return result;
        
    } catch (Exception e) {
        log.error("❌ ERREUR dans nourrirAvecPlat: {}", e.getMessage(), e);
        e.printStackTrace();
        throw new RuntimeException("Erreur lors du nourrissage avec plat: " + e.getMessage());
    }
}
    // Dans NourrissageService.java
    @Transactional(readOnly = true)
    public List<Poisson> getPoissonsAffamesAvecDetails() {
        List<Poisson> poissons = poissonRepository
            .findByEstRassasiePoissonFalseAndEstVenduPoissonFalseAndEstEnViePoissonTrue();
        
        // Charger les relations si nécessaire
        for (Poisson poisson : poissons) {
            if (poisson.getPiscineActuelle() != null) {
                // Initialiser la piscine pour éviter LazyInitializationException
                poisson.getPiscineActuelle().getNomPiscine();
            }
            if (poisson.getRacePoisson() != null) {
                poisson.getRacePoisson().getNomRacePoisson();
            }
        }
        
        return poissons;
    }
}

