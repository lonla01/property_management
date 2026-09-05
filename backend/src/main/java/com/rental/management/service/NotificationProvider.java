package com.rental.management.service;

/**
 * Abstraction générique pour l'envoi de SMS/WhatsApp — indépendante du fournisseur.
 * Permet de brancher n'importe quel fournisseur plus tard (Twilio, un fournisseur SMS
 * local camerounais, etc.) sans toucher au reste de l'application.
 */
public interface NotificationProvider {

    ResultatEnvoi envoyerSms(String telephone, String message);

    ResultatEnvoi envoyerWhatsApp(String telephone, String message);

    class ResultatEnvoi {
        public final boolean succes;
        public final String erreur;

        private ResultatEnvoi(boolean succes, String erreur) {
            this.succes = succes;
            this.erreur = erreur;
        }

        public static ResultatEnvoi ok() {
            return new ResultatEnvoi(true, null);
        }

        public static ResultatEnvoi echec(String erreur) {
            return new ResultatEnvoi(false, erreur);
        }
    }
}
