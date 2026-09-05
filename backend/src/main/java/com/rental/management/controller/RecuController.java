package com.rental.management.controller;

import com.rental.management.domain.entity.Paiement;
import com.rental.management.exception.RessourceIntrouvableException;
import com.rental.management.repository.PaiementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

/**
 * Téléchargement du reçu PDF par le locataire via son lien à token unique
 * (non-devinable), sans authentification propriétaire — voir SecurityConfig
 * (/api/public/** permitAll).
 */
@RestController
@RequiredArgsConstructor
public class RecuController {

    private final PaiementRepository paiementRepository;

    @GetMapping("/api/public/recus/{token}")
    public ResponseEntity<FileSystemResource> telecharger(@PathVariable String token) {
        Paiement paiement = paiementRepository.findByTokenRecu(token)
                .orElseThrow(() -> new RessourceIntrouvableException("Reçu introuvable ou lien invalide"));

        if (paiement.getCheminRecuPdf() == null) {
            throw new RessourceIntrouvableException("Le reçu n'est pas encore disponible pour ce paiement");
        }

        File fichier = new File(paiement.getCheminRecuPdf());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"recu.pdf\"")
                .body(new FileSystemResource(fichier));
    }
}
