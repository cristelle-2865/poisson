package com.example.poisson.service;

import com.example.poisson.model.Aliment;
import com.example.poisson.repository.AlimentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal; // IMPORT AJOUTÉ ICI
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
        return alimentRepository.save(aliment);
    }
    
    public Aliment updateAliment(Long id, Aliment alimentDetails) {
        Aliment aliment = getAlimentById(id);
        
        aliment.setNomAliment(alimentDetails.getNomAliment());
        aliment.setPrixKgAliment(alimentDetails.getPrixKgAliment());
        aliment.setProteinesParKgAliment(alimentDetails.getProteinesParKgAliment());
        aliment.setGlucidesParKgAliment(alimentDetails.getGlucidesParKgAliment());
        aliment.setStockAliment(alimentDetails.getStockAliment());
        aliment.setEstActifAliment(alimentDetails.getEstActifAliment());
        
        return alimentRepository.save(aliment);
    }
    
    public void deleteAliment(Long id) {
        Aliment aliment = getAlimentById(id);
        alimentRepository.delete(aliment);
    }
    
    public Aliment updateStock(Long id, BigDecimal nouvelleQuantite) {
        Aliment aliment = getAlimentById(id);
        aliment.setStockAliment(nouvelleQuantite);
        return alimentRepository.save(aliment);
    }
}

