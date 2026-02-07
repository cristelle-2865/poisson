// PiscineController.java
package com.example.poisson.controller;

import com.example.poisson.dto.PiscineDTO;
import com.example.poisson.model.AffectationPiscine;
import com.example.poisson.model.Piscine;
import com.example.poisson.model.Poisson;
import com.example.poisson.service.PiscineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/piscines")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class PiscineController {
    
    private final PiscineService piscineService;
    
    @GetMapping
    public ResponseEntity<List<PiscineDTO>> getAllPiscines() {
        return ResponseEntity.ok(piscineService.getAllPiscinesAsDTO());
    }
    
    @GetMapping("/actives")
    public ResponseEntity<List<Piscine>> getActivePiscines() {
        return ResponseEntity.ok(piscineService.getActivePiscines());
    }
    
    
    @GetMapping("/disponibles")
    public ResponseEntity<List<Piscine>> getAvailablePiscines() {
        return ResponseEntity.ok(piscineService.findPiscinesAvailableForTransfer());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Piscine> getPiscineById(@PathVariable Long id) {
        return ResponseEntity.ok(piscineService.getPiscineById(id));
    }
    
    @GetMapping("/{id}/statistiques")
    public ResponseEntity<Map<String, Object>> getStatistiquesPiscine(@PathVariable Long id) {
        return ResponseEntity.ok(piscineService.getStatistiquesPiscine(id));
    }
    
    @GetMapping("/{id}/poissons")
    public ResponseEntity<List<Poisson>> getPoissonsInPiscine(@PathVariable Long id) {
        return ResponseEntity.ok(piscineService.getPoissonsInPiscine(id));
    }
    
    @GetMapping("/{id}/historique")
    public ResponseEntity<List<AffectationPiscine>> getHistoriquePiscine(@PathVariable Long id) {
        return ResponseEntity.ok(piscineService.getHistoriquePiscine(id));
    }
    
    @PostMapping
    public ResponseEntity<Piscine> createPiscine(@RequestBody Piscine piscine) {
        return ResponseEntity.ok(piscineService.createPiscine(piscine));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Piscine> updatePiscine(@PathVariable Long id, @RequestBody Piscine piscine) {
        return ResponseEntity.ok(piscineService.updatePiscine(id, piscine));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePiscine(@PathVariable Long id) {
        piscineService.deletePiscine(id);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Piscine> togglePiscineStatus(@PathVariable Long id) {
        return ResponseEntity.ok(piscineService.togglePiscineStatus(id));
    }
    
    @PostMapping("/{idPiscine}/affecter/{idPoisson}")
    public ResponseEntity<AffectationPiscine> affecterPoisson(
            @PathVariable Long idPiscine,
            @PathVariable Long idPoisson) {
        return ResponseEntity.ok(piscineService.affecterPoisson(idPiscine, idPoisson));
    }
    
    @PostMapping("/retirer/{idPoisson}")
    public ResponseEntity<AffectationPiscine> retirerPoisson(
            @PathVariable Long idPoisson,
            @RequestParam String raison) {
        return ResponseEntity.ok(piscineService.retirerPoisson(idPoisson, raison));
    }
    
    @PostMapping("/{id}/vider")
    public ResponseEntity<Piscine> viderPiscine(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Transfert") String raison) {
        return ResponseEntity.ok(piscineService.viderPiscine(id, raison));
    }
    
    @GetMapping("/poisson/{idPoisson}/historique")
    public ResponseEntity<List<AffectationPiscine>> getHistoriquePoisson(@PathVariable Long idPoisson) {
        return ResponseEntity.ok(piscineService.getHistoriquePoisson(idPoisson));
    }
}

