package com.example.poisson.controller;

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
        return ResponseEntity.ok(poissonService.getAllPoissons());
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
}

