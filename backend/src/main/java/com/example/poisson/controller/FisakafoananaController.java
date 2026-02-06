package com.example.poisson.controller;

import com.example.poisson.model.Fisakafoanana;
import com.example.poisson.service.FisakafoananaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fisakafoanana")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class FisakafoananaController {
    
    
    private final FisakafoananaService fisakafoananaService;
    
    @GetMapping("/poisson/{poissonId}")
    public ResponseEntity<List<Fisakafoanana>> getHistoriqueByPoisson(@PathVariable Long poissonId) {
        return ResponseEntity.ok(fisakafoananaService.getHistoriqueByPoisson(poissonId));
    }
    
    @GetMapping("/date/{date}")
    public ResponseEntity<List<Fisakafoanana>> getHistoriqueByDate(@PathVariable String date) {
        return ResponseEntity.ok(fisakafoananaService.getHistoriqueByDate(date));
    }
}

