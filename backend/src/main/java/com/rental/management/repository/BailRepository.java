package com.rental.management.repository;

import com.rental.management.domain.entity.Bail;
import com.rental.management.domain.enums.StatutBail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BailRepository extends JpaRepository<Bail, String> {
    Page<Bail> findByLocataireProprietaireId(String proprietaireId, Pageable pageable);
    List<Bail> findByStatut(StatutBail statut);
}
