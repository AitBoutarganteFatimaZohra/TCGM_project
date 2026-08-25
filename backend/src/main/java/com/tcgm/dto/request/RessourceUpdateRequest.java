package com.tcgm.dto.request;

import com.tcgm.model.Ressource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RessourceUpdateRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotNull(message = "Le type est obligatoire")
    private Ressource.TypeRessource type;

    private Integer quantite;

    private String unite;

    private Ressource.StatutRessource statut;

    private String description;

    private String codeInterne;

    private String numeroSerie;

    private Integer seuilAlerte;

    @NotNull(message = "Le site est obligatoire")
    private Long siteId;
}