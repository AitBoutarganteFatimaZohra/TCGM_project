package com.tcgm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TravauxRequest {

    @NotBlank(message = "Le code est obligatoire")
    private String code;

    @NotBlank(message = "L'intitulé est obligatoire")
    private String intitule;

    private String description;
    private LocalDateTime dateDebutPrevue;
    private LocalDateTime dateFinPrevue;
    private LocalDateTime dateDebutReelle;
    private LocalDateTime dateFinReelle;
    private Integer priorite;
    private String statut;
    private BigDecimal budgetEstime;

    @NotNull(message = "Le chantier est obligatoire")
    private Long chantierId;
}