package com.rental.management.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * Client d'intégration CinetPay (couvre Orange Money et MTN MoMo Cameroun via une seule API).
 * Aucun compte marchand CinetPay n'existe encore côté utilisateur au moment de l'écriture de
 * ce module: le mode "sandbox" (app.cinetpay.sandbox=true) simule les réponses sans appel réseau
 * réel, pour permettre de développer et tester le reste de l'application. Basculer sur
 * app.cinetpay.sandbox=false + une vraie clé API une fois le compte CinetPay créé.
 */
@Component
@Slf4j
public class CinetPayClient {

    @Value("${app.cinetpay.api-key}")
    private String apiKey;

    @Value("${app.cinetpay.site-id}")
    private String siteId;

    @Value("${app.cinetpay.sandbox}")
    private boolean sandbox;

    @Value("${app.cinetpay.base-url}")
    private String baseUrl;

    @Value("${app.cinetpay.notify-url}")
    private String notifyUrl;

    @Value("${app.cinetpay.return-url}")
    private String returnUrl;

    private final WebClient webClient = WebClient.builder().build();

    @Data
    public static class InitiationResultat {
        private boolean succes;
        private String urlPaiement;
        private String message;
    }

    public InitiationResultat initierPaiement(String transactionId, long montant, String description) {
        if (sandbox) {
            log.info("[CinetPay][SANDBOX] Initiation simulée — transactionId={}, montant={} XAF", transactionId, montant);
            InitiationResultat resultat = new InitiationResultat();
            resultat.setSucces(true);
            // En sandbox, on renvoie une URL locale factice de paiement (page front à construire),
            // qui simule le parcours et déclenche elle-même le webhook local pour test de bout en bout.
            resultat.setUrlPaiement(returnUrl + "?sandbox=1&transaction_id=" + transactionId);
            resultat.setMessage("Paiement simulé (sandbox) — aucun compte CinetPay requis à ce stade");
            return resultat;
        }

        try {
            Map<String, Object> body = Map.of(
                    "apikey", apiKey,
                    "site_id", siteId,
                    "transaction_id", transactionId,
                    "amount", montant,
                    "currency", "XAF",
                    "description", description,
                    "notify_url", notifyUrl,
                    "return_url", returnUrl,
                    "channels", "MOBILE_MONEY"
            );

            Map<?, ?> response = webClient.post()
                    .uri(baseUrl + "/payment")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            InitiationResultat resultat = new InitiationResultat();
            if (response != null && "ACCEPTED".equalsIgnoreCase(String.valueOf(response.get("code")))) {
                Map<?, ?> data = (Map<?, ?>) response.get("data");
                resultat.setSucces(true);
                resultat.setUrlPaiement(String.valueOf(data.get("payment_url")));
            } else {
                resultat.setSucces(false);
                resultat.setMessage(response != null ? String.valueOf(response.get("message")) : "Réponse vide de CinetPay");
            }
            return resultat;
        } catch (Exception e) {
            log.error("Erreur lors de l'appel à CinetPay", e);
            InitiationResultat resultat = new InitiationResultat();
            resultat.setSucces(false);
            resultat.setMessage("Erreur technique CinetPay: " + e.getMessage());
            return resultat;
        }
    }

    /**
     * En sandbox, vérifie toujours "succès" (simulation). En production, interroge
     * l'API CinetPay /payment/check pour confirmer réellement le statut avant de faire
     * confiance au webhook (bonne pratique recommandée par CinetPay contre les faux webhooks).
     */
    public boolean verifierTransaction(String transactionId) {
        if (sandbox) {
            return true;
        }
        try {
            Map<String, Object> body = Map.of("apikey", apiKey, "site_id", siteId, "transaction_id", transactionId);
            Map<?, ?> response = webClient.post()
                    .uri(baseUrl + "/payment/check")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            if (response == null) return false;
            Map<?, ?> data = (Map<?, ?>) response.get("data");
            return data != null && "ACCEPTED".equalsIgnoreCase(String.valueOf(data.get("status")));
        } catch (Exception e) {
            log.error("Erreur lors de la vérification CinetPay", e);
            return false;
        }
    }
}
