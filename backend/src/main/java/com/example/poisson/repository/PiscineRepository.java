// PiscineRepository.java
package com.example.poisson.repository;

import com.example.poisson.model.Piscine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PiscineRepository extends JpaRepository<Piscine, Long> {
    
    List<Piscine> findByEstActivePiscineTrue();
    
    List<Piscine> findByCapaciteMaxPiscineGreaterThanEqual(Integer minCapacity);
    
    @Query("SELECT p FROM Piscine p WHERE p.capaciteMaxPiscine - (SELECT COUNT(po) FROM Poisson po WHERE po.piscineActuelle = p) >= :placesNeeded")
    List<Piscine> findPiscinesWithAvailableSpace(@Param("placesNeeded") Integer placesNeeded);
    
    @Query("SELECT p FROM Piscine p WHERE p.capaciteMaxPiscine > (SELECT COUNT(po) FROM Poisson po WHERE po.piscineActuelle = p)")
    List<Piscine> findPiscinesWithAvailableSpace();
    
    @Query("SELECT p FROM Piscine p WHERE NOT EXISTS (SELECT po FROM Poisson po WHERE po.piscineActuelle = p)")
    List<Piscine> findEmptyPiscines();
    
    @Query("SELECT p FROM Piscine p ORDER BY (SELECT COUNT(po) FROM Poisson po WHERE po.piscineActuelle = p) DESC")
    List<Piscine> findAllOrderByOccupation();
    
    @Query("SELECT COUNT(p) FROM Piscine p WHERE p.estActivePiscine = true")
    Long countActivePiscines();
    
    @Query("SELECT SUM(p.capaciteMaxPiscine) FROM Piscine p WHERE p.estActivePiscine = true")
    Integer sumTotalCapacity();
}



