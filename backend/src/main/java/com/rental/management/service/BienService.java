package com.rental.management.service;

import com.rental.management.domain.entity.Bien;
import com.rental.management.dto.request.BienRequest;
import com.rental.management.exception.AccesRefuseException;
import com.rental.management.exception.RessourceIntrouvableException;
import com.rental.management.repository.BienRepository;
import com.rental.management.security.CurrentProprietaire;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BienService {

    private final BienRepository bienRepository;
    private final CurrentProprietaire currentProprietaire;

    // Pagination obligatoire: jamais de findAll() sans Pageable sur une liste de biens.
    public Page<Bien> lister(Pageable pageable) {
        return bienRepository.findByProprietaireIdAndArchiveFalse(currentProprietaire.id(), pageable);
    }

    public Bien creer(BienRequest request) {
        Bien bien = Bien.builder()
                .adresse(request.getAdresse())
                .type(request.getType())
                .proprietaire(currentProprietaire.get())
                .build();
        return bienRepository.save(bien);
    }

    public Bien obtenir(String id) {
        Bien bien = bienRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Bien introuvable: " + id));
        verifierProprietaire(bien);
        return bien;
    }

    public Bien modifier(String id, BienRequest request) {
        Bien bien = obtenir(id);
        bien.setAdresse(request.getAdresse());
        bien.setType(request.getType());
        return bienRepository.save(bien);
    }

    public void archiver(String id) {
        Bien bien = obtenir(id);
        bien.setArchive(true);
        bienRepository.save(bien);
    }

    private void verifierProprietaire(Bien bien) {
        if (!bien.getProprietaire().getId().equals(currentProprietaire.id())) {
            throw new AccesRefuseException("Ce bien n'appartient pas à votre compte");
        }
    }
}
