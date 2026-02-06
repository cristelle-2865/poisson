// PlatController.java
package com.example.poisson.controller;

import com.example.poisson.model.Plat;
import com.example.poisson.service.PlatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/plats")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class PlatController {
    
    private final PlatService platService;
    
    @GetMapping
    public ResponseEntity<List<Plat>> getAllPlats() {
        return ResponseEntity.ok(platService.getAllPlats());
    }
    
    @GetMapping("/disponibles")
    public ResponseEntity<List<Plat>> getPlatsDisponibles() {
        return ResponseEntity.ok(platService.getPlatsDisponibles());
    }
    
    @GetMapping("/stock-total")
    public ResponseEntity<BigDecimal> getStockTotalPlats() {
        return ResponseEntity.ok(platService.getStockTotalPlatsDisponibles());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Plat> getPlatById(@PathVariable Long id) {
        return ResponseEntity.ok(platService.getPlatById(id));
    }
    
    @GetMapping("/{id}/statistiques")
    public ResponseEntity<Map<String, Object>> getStatistiquesPlat(@PathVariable Long id) {
        return ResponseEntity.ok(platService.getStatistiquesPlat(id));
    }
    
    @PostMapping
    public ResponseEntity<Plat> createPlat(@RequestBody Plat plat) {
        return ResponseEntity.ok(platService.createPlat(plat));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Plat> updatePlat(@PathVariable Long id, @RequestBody Plat plat) {
        return ResponseEntity.ok(platService.updatePlat(id, plat));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlat(@PathVariable Long id) {
        platService.deletePlat(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{idPlat}/ajouter-aliment/{idAliment}")
    public ResponseEntity<Plat> ajouterAliment(
            @PathVariable Long idPlat,
            @PathVariable Long idAliment,
            @RequestParam BigDecimal poids) {
        return ResponseEntity.ok(platService.ajouterAlimentAuPlat(idPlat, idAliment, poids));
    }
    
    @DeleteMapping("/{idPlat}/retirer-aliment/{idAliment}")
    public ResponseEntity<Plat> retirerAliment(
            @PathVariable Long idPlat,
            @PathVariable Long idAliment) {
        return ResponseEntity.ok(platService.retirerAlimentDuPlat(idPlat, idAliment));
    }
    
    @PostMapping("/{id}/utiliser")
    public ResponseEntity<Plat> utiliserPlat(@PathVariable Long id) {
        return ResponseEntity.ok(platService.utiliserPlat(id));
    }
}

