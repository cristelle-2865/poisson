// PlatRepository.java
package com.example.poisson.repository;

import com.example.poisson.model.Plat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;


@Repository
public interface PlatRepository extends JpaRepository<Plat, Long> {
    
    List<Plat> findByEstUtilisePlatFalse();
    
    List<Plat> findByDatePreparationPlat(LocalDate date);
    
    List<Plat> findByDatePreparationPlatBetween(LocalDate start, LocalDate end);
    
    @Query("SELECT p FROM Plat p WHERE p.estUtilisePlat = false AND p.poidsTotalPlat > 0")
    List<Plat> findPlatsDisponibles();
    
    @Query("SELECT SUM(p.poidsTotalPlat) FROM Plat p WHERE p.estUtilisePlat = false")
    BigDecimal sumPoidsTotalPlatsDisponibles();
}

