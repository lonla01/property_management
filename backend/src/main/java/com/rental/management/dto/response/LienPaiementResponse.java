package com.rental.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LienPaiementResponse {
    private String paiementId;
    private String urlPaiement;
    private String statut;
}
