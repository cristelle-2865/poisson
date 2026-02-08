package com.example.poisson.service;

import com.example.poisson.dto.PlatCreationDto;
import com.example.poisson.dto.PlatResponseDto;
import com.example.poisson.model.*;
import com.example.poisson.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatService {
    
    private final PlatRepository platRepository;
    private final AlimentRepository alimentRepository;
    private final CompositionPlatRepository compositionPlatRepository;
    
    // Méthodes DTO
    
    @Transactional(readOnly = true)
    public List<PlatResponseDto> getAllPlatsAsDto() {
        List<Plat> plats = platRepository.findAll();
        return convertToDtoList(plats);
    }
    
    @Transactional(readOnly = true)
    public List<PlatResponseDto> getPlatsDisponiblesAsDto() {
        List<Plat> plats = platRepository.findPlatsDisponibles();
        return convertToDtoList(plats);
    }
    
    @Transactional(readOnly = true)
    public PlatResponseDto getPlatByIdAsDto(Long id) {
        Plat plat = getPlatById(id);
        return convertToDto(plat);
    }
    
    @Transactional
    public PlatResponseDto createPlatFromDto(PlatCreationDto platDto) {
        log.info("Création d'un plat depuis DTO: {}", platDto.getNomPlat());
        
        // Valider le DTO
        validatePlatDto(platDto);
        
        // Créer le plat
        Plat plat = new Plat();
        plat.setNomPlat(platDto.getNomPlat().trim());
        plat.setDescriptionPlat(platDto.getDescriptionPlat() != null ? platDto.getDescriptionPlat().trim() : null);
        plat.setDatePreparationPlat(platDto.getDatePreparationPlat() != null 
            ? platDto.getDatePreparationPlat() 
            : LocalDate.now());
        plat.setEstUtilisePlat(platDto.getEstUtilisePlat() != null 
            ? platDto.getEstUtilisePlat() 
            : false);
        
        // Sauvegarder d'abord le plat
        Plat savedPlat = platRepository.save(plat);
        log.info("Plat {} créé avec ID: {}", savedPlat.getNomPlat(), savedPlat.getIdPlat());
        
        // Ajouter les compositions
        if (platDto.getCompositions() != null && !platDto.getCompositions().isEmpty()) {
            for (PlatCreationDto.CompositionDto compDto : platDto.getCompositions()) {
                addComposition(savedPlat, compDto);
            }
        }
        
        // Recalculer les totaux
        savedPlat.calculerTotaux();
        platRepository.save(savedPlat);
        
        return convertToDto(savedPlat);
    }
    
    @Transactional
    public PlatResponseDto updatePlatFromDto(Long id, PlatCreationDto platDto) {
        log.info("Mise à jour du plat {} depuis DTO", id);
        
        Plat plat = getPlatById(id);
        
        // Vérifier si le plat a été utilisé
        if (plat.getEstUtilisePlat()) {
            throw new RuntimeException("Impossible de modifier un plat déjà utilisé");
        }
        
        // Mettre à jour les informations de base
        plat.setNomPlat(platDto.getNomPlat().trim());
        plat.setDescriptionPlat(platDto.getDescriptionPlat() != null ? platDto.getDescriptionPlat().trim() : null);
        plat.setDatePreparationPlat(platDto.getDatePreparationPlat() != null 
            ? platDto.getDatePreparationPlat() 
            : plat.getDatePreparationPlat());
        
        // Supprimer les anciennes compositions
        compositionPlatRepository.deleteByPlatIdPlat(id);
        
        // Ajouter les nouvelles compositions
        if (platDto.getCompositions() != null && !platDto.getCompositions().isEmpty()) {
            for (PlatCreationDto.CompositionDto compDto : platDto.getCompositions()) {
                addComposition(plat, compDto);
            }
        }
        
        // Recalculer les totaux
        plat.calculerTotaux();
        Plat updatedPlat = platRepository.save(plat);
        
        return convertToDto(updatedPlat);
    }
    
    @Transactional
    public PlatResponseDto ajouterAlimentAuPlatAsDto(Long idPlat, Long idAliment, BigDecimal poids) {
        Plat plat = getPlatById(idPlat);
        
        if (plat.getEstUtilisePlat()) {
            throw new RuntimeException("Impossible de modifier un plat déjà utilisé");
        }
        
        Aliment aliment = alimentRepository.findById(idAliment)
            .orElseThrow(() -> new RuntimeException("Aliment non trouvé avec l'ID: " + idAliment));
        
        // Vérifier si l'aliment a suffisamment de stock
        if (aliment.getStockAliment().compareTo(poids) < 0) {
            throw new RuntimeException("Stock insuffisant pour l'aliment: " + aliment.getNomAliment());
        }
        
        // Vérifier si l'aliment est déjà dans le plat
        CompositionPlat existingComposition = plat.getCompositions().stream()
            .filter(comp -> comp.getAliment().getIdAliment().equals(idAliment))
            .findFirst()
            .orElse(null);
        
        if (existingComposition != null) {
            // Mettre à jour la quantité
            BigDecimal newPoids = existingComposition.getPoidsAlimentComposition().add(poids);
            existingComposition.setPoidsAlimentComposition(newPoids);
            compositionPlatRepository.save(existingComposition);
        } else {
            // Créer une nouvelle composition
            CompositionPlat composition = new CompositionPlat();
            composition.setPlat(plat);
            composition.setAliment(aliment);
            composition.setPoidsAlimentComposition(poids);
            compositionPlatRepository.save(composition);
        }
        
        // Mettre à jour les totaux du plat
        plat.calculerTotaux();
        Plat updatedPlat = platRepository.save(plat);
        
        return convertToDto(updatedPlat);
    }
    
    @Transactional
    public PlatResponseDto retirerAlimentDuPlatAsDto(Long idPlat, Long idAliment) {
        Plat plat = getPlatById(idPlat);
        
        if (plat.getEstUtilisePlat()) {
            throw new RuntimeException("Impossible de modifier un plat déjà utilisé");
        }
        
        // Trouver et supprimer la composition
        compositionPlatRepository.findByPlatIdPlat(idPlat).stream()
            .filter(comp -> comp.getAliment().getIdAliment().equals(idAliment))
            .findFirst()
            .ifPresent(compositionPlatRepository::delete);
        
        // Mettre à jour les totaux du plat
        plat.calculerTotaux();
        Plat updatedPlat = platRepository.save(plat);
        
        return convertToDto(updatedPlat);
    }
    
    @Transactional
    public PlatResponseDto utiliserPlatAsDto(Long idPlat) {
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
        Plat updatedPlat = platRepository.save(plat);
        
        return convertToDto(updatedPlat);
    }
    
    // Méthodes privées d'aide
    
    private void validatePlatDto(PlatCreationDto platDto) {
        if (platDto.getNomPlat() == null || platDto.getNomPlat().trim().isEmpty()) {
            throw new RuntimeException("Le nom du plat est requis");
        }
        
        if (platDto.getCompositions() == null || platDto.getCompositions().isEmpty()) {
            throw new RuntimeException("Le plat doit contenir au moins un aliment");
        }
        
        // Valider chaque composition
        for (PlatCreationDto.CompositionDto compDto : platDto.getCompositions()) {
            if (compDto.getIdAliment() == null) {
                throw new RuntimeException("L'ID de l'aliment est requis pour chaque composition");
            }
            
            if (compDto.getPoidsAlimentComposition() == null || 
                compDto.getPoidsAlimentComposition().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Le poids doit être positif pour chaque aliment");
            }
        }
    }
    
    private void addComposition(Plat plat, PlatCreationDto.CompositionDto compDto) {
        Aliment aliment = alimentRepository.findById(compDto.getIdAliment())
            .orElseThrow(() -> new RuntimeException(
                "Aliment non trouvé avec l'ID: " + compDto.getIdAliment()));
        
        // Vérifier le stock
        if (aliment.getStockAliment().compareTo(compDto.getPoidsAlimentComposition()) < 0) {
            throw new RuntimeException(String.format(
                "Stock insuffisant pour l'aliment '%s'. Stock disponible: %s kg, requis: %s kg",
                aliment.getNomAliment(),
                aliment.getStockAliment(),
                compDto.getPoidsAlimentComposition()
            ));
        }
        
        // Vérifier si l'aliment est actif
        if (aliment.getEstActifAliment() != null && !aliment.getEstActifAliment()) {
            throw new RuntimeException("L'aliment " + aliment.getNomAliment() + " n'est pas actif");
        }
        
        // Créer la composition
        CompositionPlat composition = new CompositionPlat();
        composition.setPlat(plat);
        composition.setAliment(aliment);
        composition.setPoidsAlimentComposition(compDto.getPoidsAlimentComposition());
        
        compositionPlatRepository.save(composition);
    }
    
    private PlatResponseDto convertToDto(Plat plat) {
        PlatResponseDto dto = new PlatResponseDto();
        dto.setIdPlat(plat.getIdPlat());
        dto.setNomPlat(plat.getNomPlat());
        dto.setDescriptionPlat(plat.getDescriptionPlat());
        dto.setPoidsTotalPlat(plat.getPoidsTotalPlat());
        dto.setCoutTotalPlat(plat.getCoutTotalPlat());
        dto.setProteinesTotalPlat(plat.getProteinesTotalPlat());
        dto.setGlucidesTotalPlat(plat.getGlucidesTotalPlat());
        dto.setProteinesParKgPlat(plat.getProteinesParKgPlat());
        dto.setGlucidesParKgPlat(plat.getGlucidesParKgPlat());
        dto.setDatePreparationPlat(plat.getDatePreparationPlat());
        dto.setEstUtilisePlat(plat.getEstUtilisePlat());
        dto.setDateCreationPlat(plat.getDateCreationPlat());
        
        // Convertir les compositions
        if (plat.getCompositions() != null && !plat.getCompositions().isEmpty()) {
            List<PlatResponseDto.CompositionResponseDto> compositionDtos = plat.getCompositions().stream()
                .map(this::convertCompositionToDto)
                .collect(Collectors.toList());
            dto.setCompositions(compositionDtos);
        } else {
            dto.setCompositions(new ArrayList<>());
        }
        
        return dto;
    }
    
    private PlatResponseDto.CompositionResponseDto convertCompositionToDto(CompositionPlat composition) {
        PlatResponseDto.CompositionResponseDto compDto = new PlatResponseDto.CompositionResponseDto();
        compDto.setIdCompositionPlat(composition.getIdCompositionPlat());
        
        Aliment aliment = composition.getAliment();
        compDto.setIdAliment(aliment.getIdAliment());
        compDto.setNomAliment(aliment.getNomAliment());
        compDto.setPrixKgAliment(aliment.getPrixKgAliment());
        compDto.setProteinesParKgAliment(aliment.getProteinesParKgAliment());
        compDto.setGlucidesParKgAliment(aliment.getGlucidesParKgAliment());
        compDto.setStockAliment(aliment.getStockAliment());
        
        compDto.setPoidsAlimentComposition(composition.getPoidsAlimentComposition());
        compDto.setCoutAlimentComposition(composition.getCoutAlimentComposition());
        compDto.setProteinesComposition(composition.getProteinesComposition());
        compDto.setGlucidesComposition(composition.getGlucidesComposition());
        compDto.setDateCreationComposition(composition.getDateCreationComposition());
        
        return compDto;
    }
    
    private List<PlatResponseDto> convertToDtoList(List<Plat> plats) {
        return plats.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    // Méthodes originales (pour compatibilité)
    
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
        // Convertir l'entité en DTO
        PlatCreationDto platDto = new PlatCreationDto();
        platDto.setNomPlat(plat.getNomPlat());
        platDto.setDescriptionPlat(plat.getDescriptionPlat());
        platDto.setDatePreparationPlat(plat.getDatePreparationPlat());
        platDto.setEstUtilisePlat(plat.getEstUtilisePlat());
        
        if (plat.getCompositions() != null && !plat.getCompositions().isEmpty()) {
            plat.getCompositions().forEach(comp -> {
                PlatCreationDto.CompositionDto compDto = new PlatCreationDto.CompositionDto();
                compDto.setIdAliment(comp.getAliment().getIdAliment());
                compDto.setPoidsAlimentComposition(comp.getPoidsAlimentComposition());
                platDto.getCompositions().add(compDto);
            });
        }
        
        createPlatFromDto(platDto);
        return plat;
    }
    
    public Map<String, Object> getStatistiquesPlat(Long idPlat) {
        Plat plat = getPlatById(idPlat);
        List<CompositionPlat> compositions = compositionPlatRepository.findByPlatIdPlat(idPlat);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("plat", convertToDto(plat));
        stats.put("nombreIngredients", compositions.size());
        stats.put("compositions", compositions.stream()
            .map(this::convertCompositionToDto)
            .collect(Collectors.toList()));
        
        return stats;
    }
    
    public BigDecimal getStockTotalPlatsDisponibles() {
        return platRepository.sumPoidsTotalPlatsDisponibles();
    }
    
    public void deletePlat(Long id) {
        Plat plat = getPlatById(id);
        
        if (plat.getEstUtilisePlat()) {
            throw new RuntimeException("Impossible de supprimer un plat déjà utilisé");
        }
        
        platRepository.delete(plat);
    }
    
    public Plat ajouterAlimentAuPlat(Long idPlat, Long idAliment, BigDecimal poids) {
        PlatResponseDto dto = ajouterAlimentAuPlatAsDto(idPlat, idAliment, poids);
        return getPlatById(dto.getIdPlat());
    }
    
    public Plat retirerAlimentDuPlat(Long idPlat, Long idAliment) {
        PlatResponseDto dto = retirerAlimentDuPlatAsDto(idPlat, idAliment);
        return getPlatById(dto.getIdPlat());
    }
    
    public Plat utiliserPlat(Long idPlat) {
        PlatResponseDto dto = utiliserPlatAsDto(idPlat);
        return getPlatById(dto.getIdPlat());
    }
}



