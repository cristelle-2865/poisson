// CompositionPlatRepository.java
package com.example.poisson.repository;

import com.example.poisson.model.CompositionPlat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompositionPlatRepository extends JpaRepository<CompositionPlat, Long> {
    
    List<CompositionPlat> findByPlatIdPlat(Long idPlat);
    
    List<CompositionPlat> findByAlimentIdAliment(Long idAliment);
    
    void deleteByPlatIdPlat(Long idPlat);
}

