package com.rental.management.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BailRequest {
    @NotBlank
    private String locataireId;

    @NotBlank
    private String uniteId;

    @NotNull @Min(1)
    private Long montantLoyer;

    @NotNull
    private LocalDate dateDebut;

    @NotNull @Min(1)
    private Integer jourEcheance;
}
