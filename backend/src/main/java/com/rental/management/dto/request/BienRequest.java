package com.rental.management.dto.request;

import com.rental.management.domain.enums.TypeBien;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BienRequest {
    @NotBlank
    private String adresse;

    @NotNull
    private TypeBien type;
}
