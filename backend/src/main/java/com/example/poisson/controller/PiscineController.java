package com.example.poisson.controller;

import com.example.poisson.dto.PiscineDTO;
import com.example.poisson.dto.PiscineWithStatsDTO;
import com.example.poisson.model.AffectationPiscine;
import com.example.poisson.model.Piscine;
import com.example.poisson.model.Poisson;
import com.example.poisson.service.PiscineService;
import com.example.poisson.service.PoissonService; // AJOUTER CET IMPORT
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/piscines")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class PiscineController {
    
    private final PiscineService piscineService;
    private final PoissonService poissonService; // AJOUTER CE SERVICE
    
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
    
    @GetMapping("/{id}/complet")
    public ResponseEntity<Map<String, Object>> getPiscineComplet(@PathVariable Long id) {
        return ResponseEntity.ok(piscineService.getPiscineWithStats(id));
    }
    
    @GetMapping("/{id}/with-stats")
    public ResponseEntity<PiscineWithStatsDTO> getPiscineWithStats(@PathVariable Long id) {
        Piscine piscine = piscineService.getPiscineByIdWithRelations(id);
        return ResponseEntity.ok(new PiscineWithStatsDTO(piscine));
    }

    @GetMapping("/with-stats")
    public ResponseEntity<List<PiscineWithStatsDTO>> getAllPiscinesWithStats() {
        List<Piscine> piscines = piscineService.getAllPiscines();
        List<PiscineWithStatsDTO> dtos = piscines.stream()
            .map(p -> {
                // Charger les poissons pour chaque piscine
                Piscine piscineComplet = piscineService.getPiscineByIdWithRelations(p.getIdPiscine());
                return new PiscineWithStatsDTO(piscineComplet);
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // ============ NOUVEAUX ENDPOINTS POUR LA GESTION DU TRANSFERT ============
    
    /**
     * Vérifier si un poisson doit changer de bassin (poids > 700g)
     */
    @GetMapping("/poisson/{idPoisson}/doit-changer")
    public ResponseEntity<Map<String, Object>> doitChangerDeBassin(@PathVariable Long idPoisson) {
        // Utiliser PoissonService au lieu de PiscineService
        Poisson poisson = poissonService.getPoissonById(idPoisson);
        boolean doitChanger = piscineService.doitChangerDeBassin(poisson);
        
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("poissonId", idPoisson);
        response.put("doitChanger", doitChanger);
        response.put("poidsActuel", poisson.getPoidsActuelPoisson());
        response.put("seuil", 700);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Obtenir tous les poissons à déplacer dans un bassin
     */
    @GetMapping("/{id}/poissons-a-deplacer")
    public ResponseEntity<List<Poisson>> getPoissonsADeplacer(@PathVariable Long id) {
        return ResponseEntity.ok(piscineService.getPoissonsADeplacer(id));
    }
    
    /**
     * Affecter un poisson à un nouveau bassin
     */
    @PostMapping("/affecter-nouveau-bassin")
    public ResponseEntity<AffectationPiscine> affecterPoissonANouveauBassin(
            @RequestParam Long idPoisson,
            @RequestParam Long idNouveauBassin,
            @RequestParam(required = false, defaultValue = "Transfert vers autre bassin") String raison) {
        return ResponseEntity.ok(piscineService.affecterPoissonANouveauBassin(idPoisson, idNouveauBassin, raison));
    }
    
    /**
     * Obtenir les bassins disponibles pour un transfert
     */
    @GetMapping("/disponibles-pour-transfert")
    public ResponseEntity<List<Piscine>> getBassinsDisponiblesPourTransfert(
            @RequestParam(required = false) Long idPoisson) {
        List<Piscine> bassinsDisponibles = piscineService.findPiscinesAvailableForTransfer();
        
        // Exclure le bassin actuel du poisson si spécifié
        if (idPoisson != null) {
            try {
                Poisson poisson = poissonService.getPoissonById(idPoisson);
                if (poisson.getPiscineActuelle() != null) {
                    Long idBassinActuel = poisson.getPiscineActuelle().getIdPiscine();
                    bassinsDisponibles = bassinsDisponibles.stream()
                        .filter(p -> !p.getIdPiscine().equals(idBassinActuel))
                        .collect(Collectors.toList());
                }
            } catch (Exception e) {
                // Ignorer l'erreur si le poisson n'existe pas
            }
        }
        
        return ResponseEntity.ok(bassinsDisponibles);
    }
}