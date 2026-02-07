package com.example.poisson.controller;

import com.example.poisson.dto.PoissonDetailDTO; // AJOUTER CET IMPORT
import com.example.poisson.model.Poisson;
import com.example.poisson.service.PoissonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/poissons")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class PoissonController {
    
    private final PoissonService poissonService;
    
    @GetMapping
    public ResponseEntity<List<Poisson>> getAllPoissons() {
        List<Poisson> poissons = poissonService.getAllPoissons();
        System.out.println("Nombre de poissons retournés: " + poissons.size());
        poissons.forEach(p -> System.out.println("Poisson ID: " + p.getIdPoisson() + ", Nom: " + p.getNomPoisson()));
        return ResponseEntity.ok(poissons);
    }
    
    @GetMapping("/en-vie")
    public ResponseEntity<List<Poisson>> getPoissonsEnVie() {
        return ResponseEntity.ok(poissonService.getPoissonsEnVie());
    }
    
    @GetMapping("/affames")
    public ResponseEntity<List<Poisson>> getPoissonsAffames() {
        return ResponseEntity.ok(poissonService.getPoissonsAffames());
    }
    
    @GetMapping("/pret-a-vendre")
    public ResponseEntity<List<Poisson>> getPoissonsPretAVendre() {
        return ResponseEntity.ok(poissonService.getPoissonsPretAVendre());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Poisson> getPoissonById(@PathVariable Long id) {
        return ResponseEntity.ok(poissonService.getPoissonById(id));
    }
    
    @PostMapping
    public ResponseEntity<Poisson> createPoisson(@RequestBody Poisson poisson) {
        return ResponseEntity.ok(poissonService.createPoisson(poisson));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Poisson> updatePoisson(@PathVariable Long id, @RequestBody Poisson poisson) {
        return ResponseEntity.ok(poissonService.updatePoisson(id, poisson));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePoisson(@PathVariable Long id) {
        poissonService.deletePoisson(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}/vendre")
    public ResponseEntity<Poisson> vendrePoisson(@PathVariable Long id) {
        return ResponseEntity.ok(poissonService.vendrePoisson(id));
    }
    
    @GetMapping("/statistiques")
    public ResponseEntity<Map<String, Object>> getStatistiques() {
        return ResponseEntity.ok(poissonService.getStatistiques());
    }

    // PoissonController.java - Ajouter ces endpoints
    @GetMapping("/sans-bassin")
    public ResponseEntity<List<Poisson>> getPoissonsSansBassin() {
        return ResponseEntity.ok(poissonService.getPoissonsWithoutPiscine());
    }

    @PutMapping("/{id}/with-bassin")
    public ResponseEntity<Poisson> updatePoissonWithBassin(
            @PathVariable Long id,
            @RequestBody Poisson poisson) {
        return ResponseEntity.ok(poissonService.updatePoissonWithBassin(id, poisson));
    }

    @PostMapping("/with-bassin")
    public ResponseEntity<Poisson> createPoissonWithBassin(@RequestBody Poisson poisson) {
        // Si un bassin est spécifié, l'affecter
        if (poisson.getPiscineActuelle() != null && poisson.getPiscineActuelle().getIdPiscine() != null) {
            return ResponseEntity.ok(poissonService.updatePoissonWithBassin(poisson.getIdPoisson(), poisson));
        }
        return ResponseEntity.ok(poissonService.createPoisson(poisson));
    }

    // Optionnel - garder pour compatibilité
    @GetMapping("/{id}/avec-relations")
    public ResponseEntity<Poisson> getPoissonAvecRelations(@PathVariable Long id) {
        return ResponseEntity.ok(poissonService.getPoissonByIdWithRelations(id));
    }

    // Nouvel endpoint pour les détails avec DTO
    @GetMapping("/{id}/detail")
    public ResponseEntity<PoissonDetailDTO> getPoissonDetail(@PathVariable Long id) {
        return ResponseEntity.ok(poissonService.getPoissonDetail(id));
    }
}


