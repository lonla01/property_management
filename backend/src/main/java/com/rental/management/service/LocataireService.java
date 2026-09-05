package com.rental.management.service;

import com.rental.management.domain.entity.Locataire;
import com.rental.management.domain.entity.Unite;
import com.rental.management.dto.request.LocataireRequest;
import com.rental.management.exception.AccesRefuseException;
import com.rental.management.exception.RessourceIntrouvableException;
import com.rental.management.repository.LocataireRepository;
import com.rental.management.security.CurrentProprietaire;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocataireService {

    private final LocataireRepository locataireRepository;
    private final CurrentProprietaire currentProprietaire;
    private final UniteService uniteService;

    // Pagination obligatoire.
    public Page<Locataire> lister(Pageable pageable) {
        return locataireRepository.findByProprietaireIdAndArchiveFalse(currentProprietaire.id(), pageable);
    }

    public Locataire creer(LocataireRequest request) {
        Locataire.LocataireBuilder builder = Locataire.builder()
                .nom(request.getNom())
                .telephone(request.getTelephone())
                .email(request.getEmail())
                .proprietaire(currentProprietaire.get());

        if (request.getUniteId() != null) {
            Unite unite = uniteService.obtenir(request.getUniteId());
            builder.unite(unite);
        }
        return locataireRepository.save(builder.build());
    }

    public Locataire obtenir(String id) {
        Locataire locataire = locataireRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Locataire introuvable: " + id));
        verifierProprietaire(locataire);
        return locataire;
    }

    public Locataire modifier(String id, LocataireRequest request) {
        Locataire locataire = obtenir(id);
        locataire.setNom(request.getNom());
        locataire.setTelephone(request.getTelephone());
        locataire.setEmail(request.getEmail());
        if (request.getUniteId() != null) {
            locataire.setUnite(uniteService.obtenir(request.getUniteId()));
        }
        return locataireRepository.save(locataire);
    }

    public void archiver(String id) {
        Locataire locataire = obtenir(id);
        locataire.setArchive(true);
        locataireRepository.save(locataire);
    }

    private void verifierProprietaire(Locataire locataire) {
        if (!locataire.getProprietaire().getId().equals(currentProprietaire.id())) {
            throw new AccesRefuseException("Ce locataire n'appartient pas à votre compte");
        }
    }
}
