package com.example.poisson.controller;

import com.example.poisson.model.Aliment;
import com.example.poisson.service.AlimentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;


@RestController
@RequestMapping("/aliments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AlimentController {
    
    private final AlimentService alimentService;
    
    @GetMapping
    public ResponseEntity<List<Aliment>> getAllAliments() {
        return ResponseEntity.ok(alimentService.getAllAliments());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Aliment> getAlimentById(@PathVariable Long id) {
        return ResponseEntity.ok(alimentService.getAlimentById(id));
    }
    
    @PostMapping
    public ResponseEntity<Aliment> createAliment(@RequestBody Aliment aliment) {
        return ResponseEntity.ok(alimentService.createAliment(aliment));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Aliment> updateAliment(@PathVariable Long id, @RequestBody Aliment aliment) {
        return ResponseEntity.ok(alimentService.updateAliment(id, aliment));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAliment(@PathVariable Long id) {
        alimentService.deleteAliment(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}/stock")
    public ResponseEntity<Aliment> updateStock(@PathVariable Long id, @RequestParam BigDecimal quantite) {
        return ResponseEntity.ok(alimentService.updateStock(id, quantite));
    }
}

