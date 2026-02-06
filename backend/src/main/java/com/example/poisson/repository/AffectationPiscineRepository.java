// AffectationPiscineRepository.java
package com.example.poisson.repository;

import com.example.poisson.model.AffectationPiscine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AffectationPiscineRepository extends JpaRepository<AffectationPiscine, Long> {
    
    List<AffectationPiscine> findByPoissonIdPoisson(Long idPoisson);
    
    List<AffectationPiscine> findByPiscineIdPiscine(Long idPiscine);
    
    List<AffectationPiscine> findByDateEntreeAffectationBetween(LocalDate start, LocalDate end);
    
    @Query("SELECT a FROM AffectationPiscine a WHERE a.poisson.idPoisson = :idPoisson AND a.dateSortieAffectation IS NULL")
    Optional<AffectationPiscine> findCurrentAffectation(@Param("idPoisson") Long idPoisson);
    
    @Query("SELECT COUNT(a) FROM AffectationPiscine a WHERE a.piscine.idPiscine = :idPiscine AND a.dateSortieAffectation IS NULL")
    Long countCurrentPoissonsInPiscine(@Param("idPiscine") Long idPiscine);
    
    List<AffectationPiscine> findByPoissonIdPoissonOrderByDateEntreeAffectationDesc(Long idPoisson);
}


