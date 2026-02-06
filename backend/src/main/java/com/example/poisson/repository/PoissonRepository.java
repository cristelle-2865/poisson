package com.example.poisson.repository;

import com.example.poisson.model.Poisson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PoissonRepository extends JpaRepository<Poisson, Long> {
    
    List<Poisson> findByEstVenduPoissonFalseAndEstEnViePoissonTrue();
    
    List<Poisson> findByEstRassasiePoissonFalseAndEstVenduPoissonFalseAndEstEnViePoissonTrue();
    
    List<Poisson> findByRacePoissonIdRacePoisson(Long raceId);
    
    List<Poisson> findByDateArriveePoissonBetween(LocalDate start, LocalDate end);
    
    @Query("SELECT p FROM Poisson p WHERE p.estVenduPoisson = false AND p.estEnViePoisson = true " +
           "AND (p.poidsActuelPoisson / p.poidsMaximalPoisson) >= 0.95")
    List<Poisson> findPoissonsPretAVendre();
    
    @Query("SELECT COUNT(p) FROM Poisson p WHERE p.estVenduPoisson = false AND p.estEnViePoisson = true")
    Long countPoissonsEnVie();
    
    @Query("SELECT SUM(p.poidsActuelPoisson) FROM Poisson p WHERE p.estVenduPoisson = false AND p.estEnViePoisson = true")
    BigDecimal sumPoidsTotalPoissons();

     // Méthodes pour la gestion des bassins
    List<Poisson> findByPiscineActuelleIsNullAndEstVenduPoissonFalseAndEstEnViePoissonTrue();
    
    List<Poisson> findByPiscineActuelleIdPiscine(Long idPiscine);
    
    @Query("SELECT p FROM Poisson p WHERE p.piscineActuelle.idPiscine = :idPiscine AND p.estVenduPoisson = false AND p.estEnViePoisson = true")
    List<Poisson> findPoissonsEnVieInPiscine(@Param("idPiscine") Long idPiscine);
    
    @Query("SELECT COUNT(p) FROM Poisson p WHERE p.piscineActuelle.idPiscine = :idPiscine")
    Long countPoissonsInPiscine(@Param("idPiscine") Long idPiscine);
}


