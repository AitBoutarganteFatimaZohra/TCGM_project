package com.tcgm.service;

import com.tcgm.dto.request.RessourceCreateRequest;
import com.tcgm.dto.request.RessourceRejetRequest;
import com.tcgm.dto.request.RessourceUpdateRequest;
import com.tcgm.dto.response.RessourceResponse;
import com.tcgm.model.Ressource;
import com.tcgm.model.Site;
import com.tcgm.model.enums.StatutValidationRessource;
import com.tcgm.model.enums.TypeAction;
import com.tcgm.model.enums.TypeActionRessource;
import com.tcgm.repository.RessourceRepository;
import com.tcgm.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RessourceService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_CHEF_CHANTIER = "CHEF_CHANTIER";
    private static final String ROLE_CHEF_PROJET = "CHEF_PROJET";

    private final RessourceRepository ressourceRepository;
    private final SiteRepository siteRepository;
    private final JournalService journalService;

    public List<RessourceResponse> getAll() {
        return ressourceRepository.findAll()
                .stream()
                .map(RessourceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public RessourceResponse getById(Long id) {
        Ressource r = ressourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ressource introuvable avec l'id " + id));
        return RessourceResponse.fromEntity(r);
    }

    public List<RessourceResponse> getBySite(Long siteId, String statut, String type, String search) {
        List<Ressource> ressources;

        if (search != null && !search.isBlank()) {
            ressources = ressourceRepository.findBySiteIdAndNomContainingIgnoreCase(siteId, search);
        } else if (statut != null && !statut.isBlank()) {
            ressources = ressourceRepository.findBySiteIdAndStatut(siteId, Ressource.StatutRessource.valueOf(statut));
        } else if (type != null && !type.isBlank()) {
            ressources = ressourceRepository.findBySiteIdAndType(siteId, Ressource.TypeRessource.valueOf(type));
        } else {
            ressources = ressourceRepository.findBySiteId(siteId);
        }

        return ressources.stream()
                .map(RessourceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // =========================================================
    // ACTIONS MAGASINIER (déclenchent le circuit de validation)
    // =========================================================

    @Transactional
    public RessourceResponse create(RessourceCreateRequest request) {
        Site site = siteRepository.findById(request.getSiteId())
                .orElseThrow(() -> new RuntimeException("Site introuvable avec l'id " + request.getSiteId()));

        Ressource r = Ressource.builder()
                .nom(request.getNom())
                .type(request.getType())
                .quantite(request.getQuantite())
                .unite(request.getUnite())
                .statut(request.getStatut() != null ? request.getStatut() : Ressource.StatutRessource.DISPONIBLE)
                .description(request.getDescription())
                .codeInterne(request.getCodeInterne())
                .numeroSerie(request.getNumeroSerie())
                .seuilAlerte(request.getSeuilAlerte())
                .site(site)
                // Une création reste en attente de validation niveau 1 : rien à
                // "restaurer" en cas de rejet définitif, donc pas de snapshot.
                .validationStatus(StatutValidationRessource.EN_ATTENTE_CHEF_CHANTIER)
                .pendingAction(TypeActionRessource.CREATION)
                .build();

        Ressource saved = ressourceRepository.save(r);

        journalService.logAction(
                TypeAction.SOUMISSION,
                "RESSOURCE",
                saved.getId(),
                "Soumission de la création de la ressource: " + saved.getNom() + " (en attente de validation)",
                null
        );

        return RessourceResponse.fromEntity(saved);
    }

    @Transactional
    public RessourceResponse update(Long id, RessourceUpdateRequest request) {
        Ressource r = ressourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ressource introuvable avec l'id " + id));

        ensureNoActionPending(r);

        Site site = siteRepository.findById(request.getSiteId())
                .orElseThrow(() -> new RuntimeException("Site introuvable avec l'id " + request.getSiteId()));

        r.snapshotAvant();

        r.setNom(request.getNom());
        r.setType(request.getType());
        r.setQuantite(request.getQuantite());
        r.setUnite(request.getUnite());
        if (request.getStatut() != null) {
            r.setStatut(request.getStatut());
        }
        r.setDescription(request.getDescription());
        r.setCodeInterne(request.getCodeInterne());
        r.setNumeroSerie(request.getNumeroSerie());
        r.setSeuilAlerte(request.getSeuilAlerte());
        r.setSite(site);

        r.setValidationStatus(StatutValidationRessource.EN_ATTENTE_CHEF_CHANTIER);
        r.setPendingAction(TypeActionRessource.MODIFICATION);
        r.setRejectionReason(null);

        Ressource saved = ressourceRepository.save(r);

        journalService.logAction(
                TypeAction.SOUMISSION,
                "RESSOURCE",
                saved.getId(),
                "Soumission de la modification de la ressource: " + saved.getNom() + " (en attente de validation)",
                null
        );

        return RessourceResponse.fromEntity(saved);
    }

    @Transactional
    public RessourceResponse updateStatut(Long id, Ressource.StatutRessource statut) {
        Ressource r = ressourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ressource introuvable avec l'id " + id));

        ensureNoActionPending(r);

        r.snapshotAvant();
        r.setStatut(statut);

        r.setValidationStatus(StatutValidationRessource.EN_ATTENTE_CHEF_CHANTIER);
        r.setPendingAction(TypeActionRessource.CHANGEMENT_STATUT);
        r.setRejectionReason(null);

        Ressource saved = ressourceRepository.save(r);

        journalService.logAction(
                TypeAction.SOUMISSION,
                "RESSOURCE",
                saved.getId(),
                "Soumission du changement de statut de la ressource " + saved.getNom() + " -> " + statut + " (en attente de validation)",
                null
        );

        return RessourceResponse.fromEntity(saved);
    }

    /**
     * Ne supprime plus directement : place la ressource en attente de
     * validation de suppression. La suppression réelle n'a lieu qu'après
     * validation (niveau 1 ou 2). Retourne l'état "en attente" de la
     * ressource au lieu d'un simple 204 sans contenu.
     */
    @Transactional
    public RessourceResponse delete(Long id) {
        Ressource r = ressourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ressource introuvable avec l'id " + id));

        ensureNoActionPending(r);

        r.setValidationStatus(StatutValidationRessource.EN_ATTENTE_CHEF_CHANTIER);
        r.setPendingAction(TypeActionRessource.SUPPRESSION);
        r.setRejectionReason(null);

        Ressource saved = ressourceRepository.save(r);

        journalService.logAction(
                TypeAction.SOUMISSION,
                "RESSOURCE",
                saved.getId(),
                "Soumission de la suppression de la ressource: " + saved.getNom() + " (en attente de validation)",
                null
        );

        return RessourceResponse.fromEntity(saved);
    }

    // =========================================================
    // CIRCUIT DE VALIDATION : Chef de Chantier -> Chef de Projet
    // =========================================================

    /**
     * @param validatingRole rôle de l'utilisateur connecté qui valide
     *                       (ADMIN, CHEF_CHANTIER ou CHEF_PROJET)
     * @return null si la ressource a été supprimée suite à la validation
     *         d'une action SUPPRESSION ; sinon la ressource à jour.
     */
    @Transactional
    public RessourceResponse valider(Long id, String validatingRole) {
        Ressource r = ressourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ressource introuvable avec l'id " + id));

        StatutValidationRessource statutActuel = r.getValidationStatus();

        if (statutActuel == StatutValidationRessource.EN_ATTENTE_CHEF_CHANTIER) {
            requireRole(validatingRole, ROLE_CHEF_CHANTIER, "Seul le Chef de Chantier (niveau 1) peut valider cette demande");
        } else if (statutActuel == StatutValidationRessource.EN_ATTENTE_CHEF_PROJET) {
            requireRole(validatingRole, ROLE_CHEF_PROJET, "Seul le Chef de Projet (niveau 2, recours) peut valider cette demande");
        } else {
            throw new IllegalStateException("Cette ressource n'a pas d'action en attente de validation");
        }

        TypeActionRessource action = r.getPendingAction();
        String niveau = statutActuel == StatutValidationRessource.EN_ATTENTE_CHEF_CHANTIER ? "niveau 1 (Chef de Chantier)" : "niveau 2 (Chef de Projet, recours)";

        if (action == TypeActionRessource.SUPPRESSION) {
            String nom = r.getNom();
            Long ressourceId = r.getId();
            ressourceRepository.delete(r);

            journalService.logAction(
                    TypeAction.VALIDATION,
                    "RESSOURCE",
                    ressourceId,
                    "Validation (" + niveau + ") de la suppression de la ressource: " + nom,
                    null
            );

            return null; // la ressource n'existe plus
        }

        r.setValidationStatus(StatutValidationRessource.VALIDEE);
        r.setPendingAction(null);
        r.setRejectionReason(null);
        r.clearAvant();

        Ressource saved = ressourceRepository.save(r);

        journalService.logAction(
                TypeAction.VALIDATION,
                "RESSOURCE",
                saved.getId(),
                "Validation (" + niveau + ") de l'action " + action + " sur la ressource: " + saved.getNom(),
                null
        );

        return RessourceResponse.fromEntity(saved);
    }

    @Transactional
    public RessourceResponse rejeter(Long id, RessourceRejetRequest request, String validatingRole) {
        Ressource r = ressourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ressource introuvable avec l'id " + id));

        StatutValidationRessource statutActuel = r.getValidationStatus();
        String motif = request != null ? request.getMotif() : null;

        if (statutActuel == StatutValidationRessource.EN_ATTENTE_CHEF_CHANTIER) {
            requireRole(validatingRole, ROLE_CHEF_CHANTIER, "Seul le Chef de Chantier (niveau 1) peut rejeter cette demande");

            // Rejet niveau 1 -> escalade automatique vers le Chef de Projet (recours)
            r.setValidationStatus(StatutValidationRessource.EN_ATTENTE_CHEF_PROJET);
            r.setRejectionReason(motif);
            Ressource saved = ressourceRepository.save(r);

            journalService.logAction(
                    TypeAction.REJET,
                    "RESSOURCE",
                    saved.getId(),
                    "Rejet niveau 1 (Chef de Chantier) de l'action " + saved.getPendingAction() + " sur la ressource: "
                            + saved.getNom() + " — transmis au Chef de Projet pour recours"
                            + (motif != null ? " — Motif: " + motif : ""),
                    null
            );

            return RessourceResponse.fromEntity(saved);
        }

        if (statutActuel == StatutValidationRessource.EN_ATTENTE_CHEF_PROJET) {
            requireRole(validatingRole, ROLE_CHEF_PROJET, "Seul le Chef de Projet (niveau 2, recours) peut rejeter définitivement cette demande");

            TypeActionRessource action = r.getPendingAction();

            switch (action) {
                case CREATION -> {
                    // Rien à restaurer : la ressource reste en base, marquée
                    // REJETEE, pour traçabilité. Le Magasinier peut la supprimer.
                    r.setValidationStatus(StatutValidationRessource.REJETEE);
                    r.setPendingAction(null);
                }
                case MODIFICATION, CHANGEMENT_STATUT -> {
                    r.restaurerAvant();
                    r.clearAvant();
                    r.setValidationStatus(StatutValidationRessource.VALIDEE);
                    r.setPendingAction(null);
                }
                case SUPPRESSION -> {
                    // Annulation de la suppression : la ressource redevient stable.
                    r.setValidationStatus(StatutValidationRessource.VALIDEE);
                    r.setPendingAction(null);
                }
            }

            r.setRejectionReason(motif);
            Ressource saved = ressourceRepository.save(r);

            journalService.logAction(
                    TypeAction.REJET,
                    "RESSOURCE",
                    saved.getId(),
                    "Rejet définitif niveau 2 (Chef de Projet) de l'action " + action + " sur la ressource: "
                            + saved.getNom() + (motif != null ? " — Motif: " + motif : ""),
                    null
            );

            return RessourceResponse.fromEntity(saved);
        }

        throw new IllegalStateException("Cette ressource n'a pas d'action en attente de validation");
    }

    // =========================================================
    // UTILITAIRES
    // =========================================================

    private void ensureNoActionPending(Ressource r) {
        if (r.getPendingAction() != null) {
            throw new IllegalStateException(
                    "Une action est déjà en attente de validation sur cette ressource (" + r.getPendingAction() + ")");
        }
    }

    private void requireRole(String actualRole, String requiredRole, String message) {
        if (actualRole == null || (!actualRole.equals(requiredRole) && !actualRole.equals(ROLE_ADMIN))) {
            throw new AccessDeniedException(message);
        }
    }
}