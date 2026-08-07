package com.tcgm.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DossierPointageRequest {

    @NotNull(message = "La date est obligatoire")
    private LocalDate date;

    @NotNull(message = "Le site est obligatoire")
    private Long siteId;

    private String notes;
}