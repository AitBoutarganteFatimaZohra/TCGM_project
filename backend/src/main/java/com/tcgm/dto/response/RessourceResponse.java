package com.tcgm.dto.response;

import com.tcgm.model.Ressource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RessourceResponse {

    private Long id;
    private String nom;
    private Ressource.TypeRessource type;
    private Integer quantite;
    private String unite;
    private Ressource.StatutRessource statut;

    // ⚠️ NOUVEAU
    private Ressource.StatutRessource pendingStatut;
    private String motifRejet;

    private String description;
    private String codeInterne;
    private String numeroSerie;
    private Integer seuilAlerte;
    private LocalDateTime dateAjout;
    private Long siteId;

    public static RessourceResponse fromEntity(Ressource r) {
        return RessourceResponse.builder()
                .id(r.getId())
                .nom(r.getNom())
                .type(r.getType())
                .quantite(r.getQuantite())
                .unite(r.getUnite())
                .statut(r.getStatut())
                .pendingStatut(r.getPendingStatut())
                .motifRejet(r.getMotifRejet())
                .description(r.getDescription())
                .codeInterne(r.getCodeInterne())
                .numeroSerie(r.getNumeroSerie())
                .seuilAlerte(r.getSeuilAlerte())
                .dateAjout(r.getDateAjout())
                .siteId(r.getSite() != null ? r.getSite().getId() : null)
                .build();
    }
}