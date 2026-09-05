package com.rental.management.repository;

import com.rental.management.domain.entity.Locataire;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocataireRepository extends JpaRepository<Locataire, String> {
    Page<Locataire> findByProprietaireIdAndArchiveFalse(String proprietaireId, Pageable pageable);
}
