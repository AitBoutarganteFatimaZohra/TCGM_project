package com.tcgm.dto.response;

import com.tcgm.model.Ressource;
import com.tcgm.model.enums.StatutValidationRessource;
import com.tcgm.model.enums.TypeActionRessource;
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
    private String description;
    private String codeInterne;
    private String numeroSerie;
    private Integer seuilAlerte;
    private LocalDateTime dateAjout;
    private Long siteId;

    // ===== Circuit de validation =====
    private StatutValidationRessource validationStatus;
    private TypeActionRessource pendingAction;
    private String rejectionReason;

    // Instantané "avant" (utile pour afficher ce qui sera restauré en cas
    // de rejet définitif) ; toujours null si pendingAction == CREATION.
    private String avantNom;
    private Ressource.TypeRessource avantType;
    private Integer avantQuantite;
    private String avantUnite;
    private Ressource.StatutRessource avantStatut;
    private String avantDescription;
    private String avantCodeInterne;
    private String avantNumeroSerie;
    private Integer avantSeuilAlerte;

    public static RessourceResponse fromEntity(Ressource r) {
        return RessourceResponse.builder()
                .id(r.getId())
                .nom(r.getNom())
                .type(r.getType())
                .quantite(r.getQuantite())
                .unite(r.getUnite())
                .statut(r.getStatut())
                .description(r.getDescription())
                .codeInterne(r.getCodeInterne())
                .numeroSerie(r.getNumeroSerie())
                .seuilAlerte(r.getSeuilAlerte())
                .dateAjout(r.getDateAjout())
                .siteId(r.getSite() != null ? r.getSite().getId() : null)
                .validationStatus(r.getValidationStatus())
                .pendingAction(r.getPendingAction())
                .rejectionReason(r.getRejectionReason())
                .avantNom(r.getAvantNom())
                .avantType(r.getAvantType())
                .avantQuantite(r.getAvantQuantite())
                .avantUnite(r.getAvantUnite())
                .avantStatut(r.getAvantStatut())
                .avantDescription(r.getAvantDescription())
                .avantCodeInterne(r.getAvantCodeInterne())
                .avantNumeroSerie(r.getAvantNumeroSerie())
                .avantSeuilAlerte(r.getAvantSeuilAlerte())
                .build();
    }
}