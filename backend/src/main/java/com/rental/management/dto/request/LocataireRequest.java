package com.rental.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LocataireRequest {
    @NotBlank
    private String nom;

    @NotBlank
    private String telephone;

    private String email;

    private String uniteId;
}
