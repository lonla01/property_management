package com.rental.management.repository;

import com.rental.management.domain.entity.Paiement;
import com.rental.management.domain.enums.StatutPaiement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaiementRepository extends JpaRepository<Paiement, String> {
    Page<Paiement> findByBailLocataireProprietaireId(String proprietaireId, Pageable pageable);
    Optional<Paiement> findByTokenLienPaiement(String token);
    Optional<Paiement> findByTokenRecu(String token);
    Optional<Paiement> findByReferenceTransaction(String reference);
    Optional<Paiement> findByBailIdAndPeriodeCouverteAndStatut(String bailId, String periode, StatutPaiement statut);
}
