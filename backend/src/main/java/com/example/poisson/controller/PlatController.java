package com.example.poisson.controller;

import com.example.poisson.dto.PlatCreationDto;
import com.example.poisson.dto.PlatResponseDto;
import com.example.poisson.model.Plat;
import com.example.poisson.service.PlatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/plats")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class PlatController {
    
    private final PlatService platService;
    
    @GetMapping
    public ResponseEntity<List<PlatResponseDto>> getAllPlats() {
        log.info("📥 GET /api/plats appelé");
        return ResponseEntity.ok(platService.getAllPlatsAsDto());
    }
    
    @GetMapping("/disponibles")
    public ResponseEntity<List<PlatResponseDto>> getPlatsDisponibles() {
        log.info("📥 GET /api/plats/disponibles appelé");
        return ResponseEntity.ok(platService.getPlatsDisponiblesAsDto());
    }
    
    @GetMapping("/stock-total")
    public ResponseEntity<BigDecimal> getStockTotalPlats() {
        log.info("📥 GET /api/plats/stock-total appelé");
        return ResponseEntity.ok(platService.getStockTotalPlatsDisponibles());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PlatResponseDto> getPlatById(@PathVariable Long id) {
        log.info("📥 GET /api/plats/{} appelé", id);
        return ResponseEntity.ok(platService.getPlatByIdAsDto(id));
    }
    
    @GetMapping("/{id}/statistiques")
    public ResponseEntity<Map<String, Object>> getStatistiquesPlat(@PathVariable Long id) {
        log.info("📥 GET /api/plats/{}/statistiques appelé", id);
        return ResponseEntity.ok(platService.getStatistiquesPlat(id));
    }
    
    @PostMapping
    public ResponseEntity<PlatResponseDto> createPlat(@Valid @RequestBody PlatCreationDto platDto) {
        log.info("📥 POST /api/plats appelé");
        log.info("📦 DTO reçu - Nom: {}, {} compositions", 
            platDto.getNomPlat(), 
            platDto.getCompositions() != null ? platDto.getCompositions().size() : 0);
        
        PlatResponseDto createdPlat = platService.createPlatFromDto(platDto);
        log.info("✅ Plat créé avec ID: {}", createdPlat.getIdPlat());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPlat);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<PlatResponseDto> updatePlat(@PathVariable Long id, @Valid @RequestBody PlatCreationDto platDto) {
        log.info("📥 PUT /api/plats/{} appelé", id);
        return ResponseEntity.ok(platService.updatePlatFromDto(id, platDto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlat(@PathVariable Long id) {
        log.info("📥 DELETE /api/plats/{} appelé", id);
        platService.deletePlat(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{idPlat}/ajouter-aliment/{idAliment}")
    public ResponseEntity<PlatResponseDto> ajouterAliment(
            @PathVariable Long idPlat,
            @PathVariable Long idAliment,
            @RequestParam BigDecimal poids) {
        log.info("📥 POST /api/plats/{}/ajouter-aliment/{} appelé", idPlat, idAliment);
        return ResponseEntity.ok(platService.ajouterAlimentAuPlatAsDto(idPlat, idAliment, poids));
    }
    
    @DeleteMapping("/{idPlat}/retirer-aliment/{idAliment}")
    public ResponseEntity<PlatResponseDto> retirerAliment(
            @PathVariable Long idPlat,
            @PathVariable Long idAliment) {
        log.info("📥 DELETE /api/plats/{}/retirer-aliment/{} appelé", idPlat, idAliment);
        return ResponseEntity.ok(platService.retirerAlimentDuPlatAsDto(idPlat, idAliment));
    }
    
    @PostMapping("/{id}/utiliser")
    public ResponseEntity<PlatResponseDto> utiliserPlat(@PathVariable Long id) {
        log.info("📥 POST /api/plats/{}/utiliser appelé", id);
        return ResponseEntity.ok(platService.utiliserPlatAsDto(id));
    }
}

