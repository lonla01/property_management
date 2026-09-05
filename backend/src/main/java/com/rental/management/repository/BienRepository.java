package com.rental.management.repository;

import com.rental.management.domain.entity.Bien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BienRepository extends JpaRepository<Bien, String> {
    Page<Bien> findByProprietaireIdAndArchiveFalse(String proprietaireId, Pageable pageable);
}
