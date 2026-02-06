package com.example.poisson.service;

import com.example.poisson.model.Fisakafoanana;
import com.example.poisson.repository.FisakafoananaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FisakafoananaService {
    
    private final FisakafoananaRepository fisakafoananaRepository;
    
    public List<Fisakafoanana> getHistoriqueByPoisson(Long poissonId) {
        return fisakafoananaRepository.findByPoissonIdPoissonOrderByDateNourrissageFisakafoanana(poissonId);
    }
    
    public List<Fisakafoanana> getHistoriqueByDate(String date) {
        LocalDate localDate = LocalDate.parse(date);
        return fisakafoananaRepository.findByDateNourrissageFisakafoananaBetween(
            localDate, localDate
        );
    }
    
    public Fisakafoanana createHistorique(Fisakafoanana historique) {
        return fisakafoananaRepository.save(historique);
    }
}

