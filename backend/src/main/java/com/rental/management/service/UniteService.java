package com.rental.management.service;

import com.rental.management.domain.entity.Bien;
import com.rental.management.domain.entity.Unite;
import com.rental.management.dto.request.UniteRequest;
import com.rental.management.exception.RessourceIntrouvableException;
import com.rental.management.repository.UniteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UniteService {

    private final UniteRepository uniteRepository;
    private final BienService bienService;

    public List<Unite> listerParBien(String bienId) {
        // obtenir() vérifie déjà que le bien appartient au propriétaire courant
        bienService.obtenir(bienId);
        return uniteRepository.findByBienId(bienId);
    }

    public Unite creer(UniteRequest request) {
        Bien bien = bienService.obtenir(request.getBienId());
        Unite unite = Unite.builder()
                .bien(bien)
                .libelle(request.getLibelle())
                .build();
        return uniteRepository.save(unite);
    }

    public Unite obtenir(String id) {
        Unite unite = uniteRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Unité introuvable: " + id));
        // Vérifie l'isolation via le bien parent
        bienService.obtenir(unite.getBien().getId());
        return unite;
    }

    public void archiver(String id) {
        Unite unite = obtenir(id);
        unite.setArchive(true);
        uniteRepository.save(unite);
    }
}
