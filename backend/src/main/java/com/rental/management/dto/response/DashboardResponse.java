package com.rental.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DashboardResponse {
    private long nombreLoyersEnRetard;
    private long montantTotalEnAttente;
    private List<SituationLocataire> situations;

    @Data
    @AllArgsConstructor
    public static class SituationLocataire {
        private String bailId;
        private String locataireNom;
        private String bienAdresse;
        private String statutMoisCourant; // PAYE / EN_ATTENTE / EN_RETARD
        private Long montantDu;
        private String dateDernierPaiement;
    }
}
