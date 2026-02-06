// UserRepository.java
package com.example.poisson.repository;

import com.example.poisson.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailUtilisateur(String email);
    boolean existsByEmailUtilisateur(String email);
}


