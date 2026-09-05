package com.rental.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InitierPaiementRequest {
    @NotBlank
    private String bailId;

    // Période à payer, ex. "2026-09". Si absent, on prend le mois courant.
    private String periode;
}
