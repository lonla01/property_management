package com.rental.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UniteRequest {
    @NotBlank
    private String bienId;

    private String libelle;
}
