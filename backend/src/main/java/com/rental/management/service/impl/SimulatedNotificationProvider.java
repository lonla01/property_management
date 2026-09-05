package com.rental.management.service.impl;

import com.rental.management.service.NotificationProvider;
import lombok.extern.slf4j.Slf4j;

/**
 * Fournisseur par défaut: ne fait aucun appel réseau réel, journalise simplement
 * l'envoi simulé. Actif tant que app.notification.provider=simulated (valeur par
 * défaut), en attendant la création des comptes Twilio/fournisseur SMS local.
 * Sélectionné explicitement par NotificationConfig (pas un @Component Spring,
 * pour éviter toute ambiguïté de bean avec TwilioNotificationProvider).
 */
@Slf4j
public class SimulatedNotificationProvider implements NotificationProvider {

    @Override
    public ResultatEnvoi envoyerSms(String telephone, String message) {
        log.info("[SMS][SIMULÉ] -> {} : {}", telephone, message);
        return ResultatEnvoi.ok();
    }

    @Override
    public ResultatEnvoi envoyerWhatsApp(String telephone, String message) {
        log.info("[WHATSAPP][SIMULÉ] -> {} : {}", telephone, message);
        return ResultatEnvoi.ok();
    }
}
