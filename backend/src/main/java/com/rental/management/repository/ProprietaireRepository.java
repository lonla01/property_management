package com.rental.management.repository;

import com.rental.management.domain.entity.Proprietaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProprietaireRepository extends JpaRepository<Proprietaire, String> {
    Optional<Proprietaire> findByEmail(String email);
    boolean existsByEmail(String email);
}
