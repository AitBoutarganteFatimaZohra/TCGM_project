package com.tcgm.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AffectationRequest {

    @NotNull(message = "La date de début est obligatoire")
    private LocalDate dateDebut;

    private LocalDate dateFin;
    private String statut;

    @NotNull(message = "Le chantier est obligatoire")
    private Long chantierId;

    @NotNull(message = "L'ouvrier est obligatoire")
    private Long ouvrierId;
}