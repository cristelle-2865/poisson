package com.example.poisson.controller;

import com.example.poisson.service.NourrissageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/nourrissage")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class NourrissageController {
    
    private final NourrissageService nourrissageService;
    
    @PostMapping("/nourrir")
    public ResponseEntity<Map<String, Object>> nourrirPoissons(
            @RequestParam BigDecimal quantitePlat, // en kg
            @RequestParam BigDecimal proteinesParKg, // g/kg
            @RequestParam BigDecimal glucidesParKg) { // g/kg
        
        Map<String, Object> result = nourrissageService.nourrirPoissons(
            quantitePlat, 
            proteinesParKg, 
            glucidesParKg
        );
        
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/nourrir-simple")
    public ResponseEntity<Map<String, Object>> nourrirPoissonsSimple(
            @RequestParam BigDecimal quantitePlat) {
        
        Map<String, Object> result = nourrissageService.nourrirPoissonsSimple(quantitePlat);
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/nourrir-avec-plat/{idPlat}")
    public ResponseEntity<Map<String, Object>> nourrirAvecPlat(@PathVariable Long idPlat) {
        try {
            Map<String, Object> result = nourrissageService.nourrirAvecPlat(idPlat);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "message", e.getMessage(),
                "error", true
            ));
        }
    }

    @PostMapping("/nourrir-bassin")
    public ResponseEntity<Map<String, Object>> nourrirPoissonsDansBassin(
            @RequestParam BigDecimal quantitePlat, // en kg
            @RequestParam BigDecimal proteinesParKg, // g/kg
            @RequestParam BigDecimal glucidesParKg, // g/kg
            @RequestParam(required = false) Long bassinId) { // ID du bassin (optionnel)
        
        Map<String, Object> result = nourrissageService.nourrirPoissonsDansBassin(
            quantitePlat, 
            proteinesParKg, 
            glucidesParKg,
            bassinId
        );
        
        return ResponseEntity.ok(result);
    }
}


