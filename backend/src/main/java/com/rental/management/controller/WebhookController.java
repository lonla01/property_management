package com.rental.management.controller;

import com.rental.management.service.PaiementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Reçoit le webhook CinetPay. CinetPay notifie en x-www-form-urlencoded avec au
 * minimum cpm_trans_id et cpm_result — voir doc CinetPay pour la liste complète.
 * Le statut n'est JAMAIS déduit uniquement de ce webhook: PaiementService revérifie
 * systématiquement auprès de l'API CinetPay avant de valider un paiement.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final PaiementService paiementService;

    @PostMapping(value = "/api/webhooks/cinetpay", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<String> recevoirWebhookCinetPay(
            @RequestParam(name = "cpm_trans_id") String transactionId,
            @RequestParam(name = "payment_method", required = false) String modePaiement) {

        log.info("Webhook CinetPay reçu pour la transaction {}", transactionId);
        try {
            paiementService.traiterWebhook(transactionId, modePaiement);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Erreur de traitement du webhook CinetPay pour {}", transactionId, e);
            // On répond quand même 200 pour éviter des re-tentatives en boucle de CinetPay
            // sur une erreur qui ne se résoudra pas toute seule; l'échec est journalisé.
            return ResponseEntity.ok("ERREUR_JOURNALISEE");
        }
    }
}
