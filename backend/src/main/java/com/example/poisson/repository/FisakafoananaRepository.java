package com.example.poisson.repository;

import com.example.poisson.model.Fisakafoanana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FisakafoananaRepository extends JpaRepository<Fisakafoanana, Long> {
    
    List<Fisakafoanana> findByPoissonIdPoissonOrderByDateNourrissageFisakafoanana(Long idPoisson);
    
    List<Fisakafoanana> findByDateNourrissageFisakafoananaBetween(LocalDate start, LocalDate end);
    
    @Query("SELECT SUM(f.gainPoidsFisakafoanana) FROM Fisakafoanana f WHERE f.poisson.idPoisson = :idPoisson")
    Double sumGainPoidsByPoisson(@Param("idPoisson") Long idPoisson);
}

