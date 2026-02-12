package com.example.poisson.service;

import com.example.poisson.dto.PiscineDTO;
import com.example.poisson.dto.PiscineWithStatsDTO;
import com.example.poisson.model.AffectationPiscine;
import com.example.poisson.model.Piscine;
import com.example.poisson.model.Poisson;
import com.example.poisson.repository.AffectationPiscineRepository;
import com.example.poisson.repository.PiscineRepository;
import com.example.poisson.repository.PoissonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class PiscineService {
    
    private final PiscineRepository piscineRepository;
    private final PoissonRepository poissonRepository;
    private final AffectationPiscineRepository affectationPiscineRepository;

    public List<PiscineDTO> getAllPiscinesAsDTO() {
        return piscineRepository.findAll().stream()
                .map(PiscineDTO::new)
                .collect(Collectors.toList());
    }
    
    public List<Piscine> getAllPiscines() {
        return piscineRepository.findAll();
    }
    
    public List<Piscine> getActivePiscines() {
        return piscineRepository.findByEstActivePiscineTrue();
    }
    
    public Piscine getPiscineById(Long id) {
        return piscineRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Piscine non trouvée avec l'ID: " + id));
    }
    
    public Piscine createPiscine(Piscine piscine) {
        if (piscine.getCapaciteMaxPiscine() <= 0) {
            throw new RuntimeException("La capacité doit être supérieure à 0");
        }
        return piscineRepository.save(piscine);
    }
    
    // Dans la méthode updatePiscine() de PiscineService.java
    public Piscine updatePiscine(Long id, Piscine piscineDetails) {
        Piscine piscine = getPiscineById(id);
        
        piscine.setNomPiscine(piscineDetails.getNomPiscine());
        piscine.setCapaciteMaxPiscine(piscineDetails.getCapaciteMaxPiscine());
        piscine.setDescriptionPiscine(piscineDetails.getDescriptionPiscine());
        
        // Mettre à jour les champs optionnels si présents
        if (piscineDetails.getVolumePiscine() != null) {
            piscine.setVolumePiscine(piscineDetails.getVolumePiscine());
        }
        if (piscineDetails.getTemperaturePiscine() != null) {
            piscine.setTemperaturePiscine(piscineDetails.getTemperaturePiscine());
        }
        if (piscineDetails.getPhPiscine() != null) {
            piscine.setPhPiscine(piscineDetails.getPhPiscine());
        }
        
        // Mettre à jour la date de modification
        piscine.setDateModificationPiscine(LocalDateTime.now());
        
        return piscineRepository.save(piscine);
    }
    
    public void deletePiscine(Long id) {
        Piscine piscine = getPiscineById(id);
        
        // Vérifier si le bassin contient des poissons
        if (piscine.getPoissons() != null && !piscine.getPoissons().isEmpty()) {
            throw new RuntimeException("Impossible de supprimer un bassin contenant des poissons");
        }
        
        piscineRepository.delete(piscine);
    }
    
    public Piscine togglePiscineStatus(Long id) {
        Piscine piscine = getPiscineById(id);
        piscine.setEstActivePiscine(!piscine.getEstActivePiscine());
        return piscineRepository.save(piscine);
    }
    
    @Transactional
    public AffectationPiscine affecterPoisson(Long idPiscine, Long idPoisson) {
        Piscine piscine = getPiscineById(idPiscine);
        Poisson poisson = poissonRepository.findById(idPoisson)
            .orElseThrow(() -> new RuntimeException("Poisson non trouvé avec l'ID: " + idPoisson));
        
        // Vérifier si le bassin est actif
        if (!piscine.getEstActivePiscine()) {
            throw new RuntimeException("Le bassin n'est pas actif");
        }
        
        // Vérifier la capacité
        Long nombrePoissonsActuel = affectationPiscineRepository.countCurrentPoissonsInPiscine(idPiscine);
        if (nombrePoissonsActuel >= piscine.getCapaciteMaxPiscine()) {
            throw new RuntimeException("Le bassin a atteint sa capacité maximale");
        }
        
        // Vérifier si le poisson est en vie et non vendu
        if (poisson.getEstVenduPoisson() || !poisson.getEstEnViePoisson()) {
            throw new RuntimeException("Le poisson n'est pas disponible pour l'affectation");
        }
        
        // Terminer l'affectation actuelle si elle existe
        Optional<AffectationPiscine> currentAffectation = affectationPiscineRepository
            .findCurrentAffectation(idPoisson);
        
        if (currentAffectation.isPresent()) {
            AffectationPiscine affectation = currentAffectation.get();
            affectation.setDateSortieAffectation(LocalDate.now());
            affectation.setRaisonSortieAffectation("Transfert");
            affectationPiscineRepository.save(affectation);
        }
        
        // Créer la nouvelle affectation
        AffectationPiscine nouvelleAffectation = new AffectationPiscine();
        nouvelleAffectation.setPiscine(piscine);
        nouvelleAffectation.setPoisson(poisson);
        nouvelleAffectation.setDateEntreeAffectation(LocalDate.now());
        
        // Mettre à jour le poisson avec le bassin actuel
        poisson.setPiscineActuelle(piscine);
        poissonRepository.save(poisson);
        
        return affectationPiscineRepository.save(nouvelleAffectation);
    }
    
    @Transactional
public AffectationPiscine retirerPoisson(Long idPoisson, String raison) {
    Poisson poisson = poissonRepository.findById(idPoisson)
        .orElseThrow(() -> new RuntimeException("Poisson non trouvé avec l'ID: " + idPoisson));
    
    // Vérifier si le poisson est dans un bassin
    if (poisson.getPiscineActuelle() == null) {
        throw new RuntimeException("Le poisson n'est pas dans un bassin");
    }
    
    // Récupérer l'affectation actuelle
    Optional<AffectationPiscine> currentAffectation = affectationPiscineRepository
        .findCurrentAffectation(idPoisson);
    
    if (currentAffectation.isPresent()) {
        AffectationPiscine affectation = currentAffectation.get();
        affectation.setDateSortieAffectation(LocalDate.now());
        affectation.setRaisonSortieAffectation(raison);
        affectationPiscineRepository.save(affectation);
    }
    
    // Retirer le poisson du bassin
    poisson.setPiscineActuelle(null);
    poissonRepository.save(poisson);
    
    // Retourner l'affectation mise à jour ou une nouvelle si pas trouvée
    return currentAffectation.orElse(null);
}

/**
 * Affecter un poisson à un nouveau bassin (avec retrait automatique de l'ancien)
 */
@Transactional
public AffectationPiscine affecterPoissonANouveauBassin(Long idPoisson, Long idNouveauBassin, String raison) {
    // Retirer du bassin actuel si présent
    Poisson poisson = poissonRepository.findById(idPoisson)
        .orElseThrow(() -> new RuntimeException("Poisson non trouvé avec l'ID: " + idPoisson));
    
    if (poisson.getPiscineActuelle() != null) {
        retirerPoisson(idPoisson, raison != null ? raison : "Transfert vers autre bassin");
    }
    
    // Affecter au nouveau bassin
    return affecterPoisson(idNouveauBassin, idPoisson);
}

/**
 * Vérifier si un poisson doit changer de bassin (poids > 700g)
 */
public boolean doitChangerDeBassin(Poisson poisson) {
    if (poisson == null || poisson.getEstVenduPoisson() || !poisson.getEstEnViePoisson()) {
        return false;
    }
    return poisson.getPoidsActuelPoisson() != null && 
           poisson.getPoidsActuelPoisson().compareTo(new BigDecimal("700")) > 0;
}

/**
 * Obtenir tous les poissons qui doivent changer de bassin dans une piscine
 */
public List<Poisson> getPoissonsADeplacer(Long idPiscine) {
    List<Poisson> poissons = poissonRepository.findByPiscineActuelleIdPiscine(idPiscine);
    return poissons.stream()
        .filter(p -> !p.getEstVenduPoisson() && p.getEstEnViePoisson())
        .filter(p -> p.getPoidsActuelPoisson() != null && 
                    p.getPoidsActuelPoisson().compareTo(new BigDecimal("700")) > 0)
        .collect(Collectors.toList());
}

    public List<Poisson> getPoissonsInPiscine(Long idPiscine) {
        Piscine piscine = getPiscineById(idPiscine);
        return piscine.getPoissons();
    }
    
    public List<AffectationPiscine> getHistoriquePiscine(Long idPiscine) {
        return affectationPiscineRepository.findHistoriqueWithPoissonDetails(idPiscine);
    }
    
    public List<AffectationPiscine> getHistoriquePoisson(Long idPoisson) {
        return affectationPiscineRepository.findByPoissonIdPoissonOrderByDateEntreeAffectationDesc(idPoisson);
    }
    
    public Map<String, Object> getStatistiquesPiscine(Long idPiscine) {
        Piscine piscine = getPiscineById(idPiscine);
        List<Poisson> poissons = piscine.getPoissons();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("piscine", piscine);
        stats.put("nombrePoissons", poissons.size());
        stats.put("capaciteRestante", piscine.getCapaciteRestante());
        stats.put("tauxOccupation", piscine.getTauxOccupation());
        
        // Statistiques sur les poissons
        if (!poissons.isEmpty()) {
            long poissonsAffames = poissons.stream()
                .filter(p -> !p.getEstRassasiePoisson())
                .count();
            
            long poissonsPretAVendre = poissons.stream()
                .filter(Poisson::estPretAVendre)
                .count();
            
            stats.put("poissonsAffames", poissonsAffames);
            stats.put("poissonsPretAVendre", poissonsPretAVendre);
            
            // Calculer le poids total
            double poidsTotal = poissons.stream()
                .mapToDouble(p -> p.getPoidsActuelPoisson().doubleValue())
                .sum();
            stats.put("poidsTotal", poidsTotal);
        }
        
        return stats;
    }
    
    @Transactional
    public Piscine viderPiscine(Long idPiscine, String raison) {
        Piscine piscine = getPiscineById(idPiscine);
        
        for (Poisson poisson : piscine.getPoissons()) {
            retirerPoisson(poisson.getIdPoisson(), raison);
        }
        
        return piscine;
    }
    
    public List<Piscine> findPiscinesAvailableForTransfer() {
        return piscineRepository.findPiscinesWithAvailableSpace();
    }

    // Méthode simplifiée sans Hibernate
    public Map<String, Object> getPiscineWithStats(Long idPiscine) {
        Piscine piscine = getPiscineById(idPiscine);
        
        // Utiliser une requête spécifique pour charger les poissons
        List<Poisson> poissons = poissonRepository.findByPiscineActuelleIdPiscine(idPiscine);
        piscine.setPoissons(poissons); // Définir la collection
        
        Map<String, Object> result = new HashMap<>();
        result.put("bassin", piscine);
        result.put("statistiques", getStatistiquesPiscine(idPiscine));
        
        return result;
    }

    // Version alternative sans Hibernate.initialize
    public Piscine getPiscineByIdWithRelations(Long id) {
        Piscine piscine = getPiscineById(id);
        
        // Charger les poissons via une requête spécifique
        List<Poisson> poissons = poissonRepository.findByPiscineActuelleIdPiscine(id);
        piscine.setPoissons(poissons);
        
        return piscine;
    }

    // PiscineService.java - Ajoutez cette méthode
    public List<PiscineWithStatsDTO> getAllPiscinesWithStats() {
        List<Piscine> piscines = getAllPiscines();
        return piscines.stream()
            .map(p -> {
                // Charger les relations pour chaque piscine
                Piscine piscineComplet = getPiscineByIdWithRelations(p.getIdPiscine());
                return new PiscineWithStatsDTO(piscineComplet);
            })
            .collect(Collectors.toList());
    }
}



