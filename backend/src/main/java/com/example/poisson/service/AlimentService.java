package com.example.poisson.service;

import com.example.poisson.model.Aliment;
import com.example.poisson.repository.AlimentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlimentService {
    
    private final AlimentRepository alimentRepository;
    
    public List<Aliment> getAllAliments() {
        return alimentRepository.findAll();
    }
    
    public Aliment getAlimentById(Long id) {
        return alimentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Aliment non trouvé avec l'ID: " + id));
    }
    
    public Aliment createAliment(Aliment aliment) {
        // Initialiser les dates
        aliment.setDateCreationAliment(LocalDateTime.now());
        aliment.setDateModificationAliment(LocalDateTime.now());
        
        // Valeurs par défaut pour lipides et vitamines
        if (aliment.getLipidesParKgAliment() == null) {
            aliment.setLipidesParKgAliment(BigDecimal.valueOf(5.0));
        }
        if (aliment.getVitaminesParKgAliment() == null) {
            aliment.setVitaminesParKgAliment(BigDecimal.valueOf(0.5));
        }
        if (aliment.getStockAliment() == null) {
            aliment.setStockAliment(BigDecimal.ZERO);
        }
        if (aliment.getSeuilMinimumAliment() == null) {
            aliment.setSeuilMinimumAliment(BigDecimal.valueOf(10));
        }
        if (aliment.getEstActifAliment() == null) {
            aliment.setEstActifAliment(true);
        }
        
        return alimentRepository.save(aliment);
    }
    
    public Aliment updateAliment(Long id, Aliment alimentDetails) {
        Aliment aliment = getAlimentById(id);
        
        aliment.setNomAliment(alimentDetails.getNomAliment());
        aliment.setPrixKgAliment(alimentDetails.getPrixKgAliment());
        aliment.setProteinesParKgAliment(alimentDetails.getProteinesParKgAliment());
        aliment.setGlucidesParKgAliment(alimentDetails.getGlucidesParKgAliment());
        
        // AJOUT : Mettre à jour lipides et vitamines
        if (alimentDetails.getLipidesParKgAliment() != null) {
            aliment.setLipidesParKgAliment(alimentDetails.getLipidesParKgAliment());
        }
        if (alimentDetails.getVitaminesParKgAliment() != null) {
            aliment.setVitaminesParKgAliment(alimentDetails.getVitaminesParKgAliment());
        }
        
        aliment.setStockAliment(alimentDetails.getStockAliment());
        aliment.setSeuilMinimumAliment(alimentDetails.getSeuilMinimumAliment());
        aliment.setEstActifAliment(alimentDetails.getEstActifAliment());
        aliment.setDateModificationAliment(LocalDateTime.now());
        
        return alimentRepository.save(aliment);
    }
    
    public void deleteAliment(Long id) {
        Aliment aliment = getAlimentById(id);
        alimentRepository.delete(aliment);
    }
    
    public Aliment updateStock(Long id, BigDecimal nouvelleQuantite) {
        Aliment aliment = getAlimentById(id);
        aliment.setStockAliment(nouvelleQuantite);
        aliment.setDateModificationAliment(LocalDateTime.now());
        return alimentRepository.save(aliment);
    }
}

