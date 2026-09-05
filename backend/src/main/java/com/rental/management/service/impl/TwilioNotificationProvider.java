package com.rental.management.service.impl;

import com.rental.management.service.NotificationProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.LinkedMultiValueMap;
import java.util.MultiValueMap;

/**
 * Implémentation réelle basée sur Twilio (SMS + WhatsApp Business API).
 * Activer avec app.notification.provider=twilio une fois le compte Twilio créé
 * et les variables TWILIO_ACCOUNT_SID / TWILIO_AUTH_TOKEN / TWILIO_WHATSAPP_FROM /
 * TWILIO_SMS_FROM renseignées. Instancié par NotificationConfig, pas un @Component.
 */
@Slf4j
public class TwilioNotificationProvider implements NotificationProvider {

    private final String accountSid;
    private final String authToken;
    private final String smsFrom;
    private final String whatsappFrom;

    public TwilioNotificationProvider(String accountSid, String authToken, String smsFrom, String whatsappFrom) {
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.smsFrom = smsFrom;
        this.whatsappFrom = whatsappFrom;
    }

    private WebClient client() {
        return WebClient.builder()
                .baseUrl("https://api.twilio.com/2010-04-01/Accounts/" + accountSid)
                .defaultHeaders(headers -> headers.setBasicAuth(accountSid, authToken))
                .build();
    }

    @Override
    public ResultatEnvoi envoyerSms(String telephone, String message) {
        return envoyer(smsFrom, telephone, message);
    }

    @Override
    public ResultatEnvoi envoyerWhatsApp(String telephone, String message) {
        return envoyer("whatsapp:" + whatsappFrom, "whatsapp:" + telephone, message);
    }

    private ResultatEnvoi envoyer(String from, String to, String message) {
        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("From", from);
            form.add("To", to);
            form.add("Body", message);

            client().post()
                    .uri("/Messages.json")
                    .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(form)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            return ResultatEnvoi.ok();
        } catch (Exception e) {
            log.error("Échec d'envoi Twilio vers {}", to, e);
            return ResultatEnvoi.echec(e.getMessage());
        }
    }
}
