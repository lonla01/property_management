package com.rental.management.controller;

import com.rental.management.domain.entity.Paiement;
import com.rental.management.dto.request.InitierPaiementRequest;
import com.rental.management.dto.response.LienPaiementResponse;
import com.rental.management.service.PaiementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PaiementController {

    private final PaiementService paiementService;

    @GetMapping("/api/paiements")
    public Page<Paiement> lister(@PageableDefault(size = 50) Pageable pageable) {
        return paiementService.lister(pageable);
    }

    @PostMapping("/api/paiements/initier")
    public LienPaiementResponse initier(@Valid @RequestBody InitierPaiementRequest request) {
        return paiementService.initierPaiement(request);
    }

    // Endpoint public consulté par le locataire via son lien à token unique (pas d'auth requise,
    // le token fait office de clé d'accès non-devinable — voir SecurityConfig: /api/public/** permitAll).
    @GetMapping("/api/public/paiements/{token}")
    public Paiement consulterParToken(@PathVariable String token) {
        return paiementService.obtenirParToken(token);
    }
}
