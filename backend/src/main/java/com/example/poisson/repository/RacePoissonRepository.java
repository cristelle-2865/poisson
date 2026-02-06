package com.example.poisson.repository;

import com.example.poisson.model.RacePoisson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RacePoissonRepository extends JpaRepository<RacePoisson, Long> {
    
}

