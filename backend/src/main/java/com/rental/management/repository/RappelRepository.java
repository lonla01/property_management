package com.rental.management.repository;

import com.rental.management.domain.entity.Rappel;
import com.rental.management.domain.enums.StatutEnvoi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RappelRepository extends JpaRepository<Rappel, String> {
    Page<Rappel> findByBailLocataireProprietaireId(String proprietaireId, Pageable pageable);
    List<Rappel> findByStatutEnvoi(StatutEnvoi statutEnvoi);
    boolean existsByBailIdAndPeriodeConcerneeAndTypeAndCanal(
            String bailId, String periode,
            com.rental.management.domain.enums.TypeRappel type,
            com.rental.management.domain.enums.CanalEnvoi canal);
}
