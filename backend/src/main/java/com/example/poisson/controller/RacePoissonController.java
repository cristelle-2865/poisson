package com.example.poisson.controller;

import com.example.poisson.model.RacePoisson;
import com.example.poisson.repository.RacePoissonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/races-poisson")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class RacePoissonController {
    
    private final RacePoissonRepository racePoissonRepository;
    
    @GetMapping
    public ResponseEntity<List<RacePoisson>> getAllRaces() {
        return ResponseEntity.ok(racePoissonRepository.findAll());
    }
    
    @PostMapping
    public ResponseEntity<RacePoisson> createRace(@RequestBody RacePoisson racePoisson) {
        return ResponseEntity.ok(racePoissonRepository.save(racePoisson));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<RacePoisson> updateRace(@PathVariable Long id, @RequestBody RacePoisson raceDetails) {
        RacePoisson race = racePoissonRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Race non trouvée avec l'ID: " + id));
        
        race.setNomRacePoisson(raceDetails.getNomRacePoisson());
        race.setDescriptionRacePoisson(raceDetails.getDescriptionRacePoisson());
        race.setEstActifRacePoisson(raceDetails.getEstActifRacePoisson());
        
        return ResponseEntity.ok(racePoissonRepository.save(race));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRace(@PathVariable Long id) {
        RacePoisson race = racePoissonRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Race non trouvée avec l'ID: " + id));
        
        racePoissonRepository.delete(race);
        return ResponseEntity.noContent().build();
    }
}
