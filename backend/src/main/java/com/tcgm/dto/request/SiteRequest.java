package com.tcgm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SiteRequest {

    @NotBlank(message = "Le nom du site est obligatoire")
    private String name;

    private String reference;
    private String address;
    private String description;
    private Double latitude;
    private Double longitude;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private String status; // PLANIFIE, EN_COURS, TERMINE, SUSPENDU

    @NotNull(message = "Le client est obligatoire")
    private Long clientId;

    @NotNull(message = "Le chef de projet est obligatoire")
    private Long chefProjetId;

    private Long magasinierId;
    private Long agentSaisieId;
    private Long chefChantierId;
}