package com.rental.management.service;

import com.rental.management.domain.entity.Bail;
import com.rental.management.domain.entity.Locataire;
import com.rental.management.domain.entity.Unite;
import com.rental.management.domain.enums.StatutBail;
import com.rental.management.dto.request.BailRequest;
import com.rental.management.exception.AccesRefuseException;
import com.rental.management.exception.RessourceIntrouvableException;
import com.rental.management.repository.BailRepository;
import com.rental.management.security.CurrentProprietaire;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BailService {

    private final BailRepository bailRepository;
    private final CurrentProprietaire currentProprietaire;
    private final LocataireService locataireService;
    private final UniteService uniteService;

    // Pagination obligatoire.
    public Page<Bail> lister(Pageable pageable) {
        return bailRepository.findByLocataireProprietaireId(currentProprietaire.id(), pageable);
    }

    public Bail creer(BailRequest request) {
        Locataire locataire = locataireService.obtenir(request.getLocataireId());
        Unite unite = uniteService.obtenir(request.getUniteId());

        Bail bail = Bail.builder()
                .locataire(locataire)
                .unite(unite)
                .montantLoyer(request.getMontantLoyer())
                .dateDebut(request.getDateDebut())
                .jourEcheance(request.getJourEcheance())
                .build();
        return bailRepository.save(bail);
    }

    public Bail obtenir(String id) {
        Bail bail = bailRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Bail introuvable: " + id));
        verifierProprietaire(bail);
        return bail;
    }

    public Bail resilier(String id) {
        Bail bail = obtenir(id);
        bail.setStatut(StatutBail.RESILIE);
        return bailRepository.save(bail);
    }

    private void verifierProprietaire(Bail bail) {
        if (!bail.getLocataire().getProprietaire().getId().equals(currentProprietaire.id())) {
            throw new AccesRefuseException("Ce bail n'appartient pas à votre compte");
        }
    }
}
