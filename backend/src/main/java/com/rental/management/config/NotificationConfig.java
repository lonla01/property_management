package com.rental.management.config;

import com.rental.management.service.NotificationProvider;
import com.rental.management.service.impl.SimulatedNotificationProvider;
import com.rental.management.service.impl.TwilioNotificationProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationConfig {

    @Value("${app.notification.provider}")
    private String provider;

    @Value("${app.notification.twilio.account-sid}")
    private String twilioAccountSid;

    @Value("${app.notification.twilio.auth-token}")
    private String twilioAuthToken;

    @Value("${app.notification.twilio.sms-from}")
    private String twilioSmsFrom;

    @Value("${app.notification.twilio.whatsapp-from}")
    private String twilioWhatsappFrom;

    @Bean
    public NotificationProvider notificationProvider() {
        if ("twilio".equalsIgnoreCase(provider)) {
            return new TwilioNotificationProvider(twilioAccountSid, twilioAuthToken, twilioSmsFrom, twilioWhatsappFrom);
        }
        // Par défaut (et tant qu'aucun compte fournisseur réel n'existe): simulation.
        return new SimulatedNotificationProvider();
    }
}
