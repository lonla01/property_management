package com.rental.management.controller;

import com.rental.management.domain.entity.Rappel;
import com.rental.management.service.RappelService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint dédié appelé par un planificateur externe (worker Railway + cron) pour
 * déclencher les rappels/relances du jour. Protégé par une clé partagée simple
 * (header X-Scheduler-Key) car ce n'est ni un propriétaire authentifié, ni un accès public.
 */
@RestController
@RequestMapping("/api/rappels")
@RequiredArgsConstructor
public class RappelController {

    private final RappelService rappelService;

    @Value("${app.scheduler.key:}")
    private String cleAttendue;

    @GetMapping
    public Page<Rappel> historique(@PageableDefault(size = 50) Pageable pageable) {
        return rappelService.listerHistorique(pageable);
    }

    @PostMapping("/executer-du-jour")
    public ResponseEntity<?> executerDuJour(@RequestHeader("X-Scheduler-Key") String cle) {
        if (cleAttendue == null || cleAttendue.isBlank() || !cleAttendue.equals(cle)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Clé de planificateur invalide");
        }
        return ResponseEntity.ok(rappelService.executerRappelsEtRelancesDuJour());
    }

    @PostMapping("/reessayer-echecs")
    public ResponseEntity<?> reessayerEchecs(@RequestHeader("X-Scheduler-Key") String cle) {
        if (cleAttendue == null || cleAttendue.isBlank() || !cleAttendue.equals(cle)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Clé de planificateur invalide");
        }
        return ResponseEntity.ok(rappelService.reessayerEnvoisEchoues());
    }
}
