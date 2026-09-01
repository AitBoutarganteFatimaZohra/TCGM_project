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
    // ACTIONS MAGASINIER / ADMIN (déclenchent le circuit de validation,
    // SAUF pour l'Admin qui applique directement — cf. isAdmin ci-dessous)
    // =========================================================

    @Transactional
    public RessourceResponse create(RessourceCreateRequest request, String actingRole) {
        Site site = siteRepository.findById(request.getSiteId())
                .orElseThrow(() -> new RuntimeException("Site introuvable avec l'id " + request.getSiteId()));

        boolean isAdmin = ROLE_ADMIN.equals(actingRole);

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
                // ⚠️ NOUVEAU : l'Admin n'a pas besoin de validation — sa création
                // est directement VALIDEE, sans pendingAction.
                .validationStatus(isAdmin ? StatutValidationRessource.VALIDEE : StatutValidationRessource.EN_ATTENTE_CHEF_CHANTIER)
                .pendingAction(isAdmin ? null : TypeActionRessource.CREATION)
                .build();

        Ressource saved = ressourceRepository.save(r);

        journalService.logAction(
                isAdmin ? TypeAction.CREATION : TypeAction.SOUMISSION,
                "RESSOURCE",
                saved.getId(),
                isAdmin
                        ? "Création directe de la ressource par l'Administrateur: " + saved.getNom()
                        : "Soumission de la création de la ressource: " + saved.getNom() + " (en attente de validation)",
                null
        );

        return RessourceResponse.fromEntity(saved);
    }

    @Transactional
    public RessourceResponse update(Long id, RessourceUpdateRequest request, String actingRole) {
        Ressource r = ressourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ressource introuvable avec l'id " + id));

        boolean isAdmin = ROLE_ADMIN.equals(actingRole);

        // ⚠️ L'Admin peut modifier même si une action est déjà en attente
        // (il a autorité sur tout) ; les autres rôles restent bloqués.
        if (!isAdmin) {
            ensureNoActionPending(r);
        }

        Site site = siteRepository.findById(request.getSiteId())
                .orElseThrow(() -> new RuntimeException("Site introuvable avec l'id " + request.getSiteId()));

        if (!isAdmin) {
            r.snapshotAvant();
        }

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

        if (isAdmin) {
            // ⚠️ NOUVEAU : application directe, pas de circuit de validation
            r.setValidationStatus(StatutValidationRessource.VALIDEE);
            r.setPendingAction(null);
            r.clearAvant();
        } else {
            r.setValidationStatus(StatutValidationRessource.EN_ATTENTE_CHEF_CHANTIER);
            r.setPendingAction(TypeActionRessource.MODIFICATION);
        }
        r.setRejectionReason(null);

        Ressource saved = ressourceRepository.save(r);

        journalService.logAction(
                isAdmin ? TypeAction.MODIFICATION : TypeAction.SOUMISSION,
                "RESSOURCE",
                saved.getId(),
                isAdmin
                        ? "Modification directe de la ressource par l'Administrateur: " + saved.getNom()
                        : "Soumission de la modification de la ressource: " + saved.getNom() + " (en attente de validation)",
                null
        );

        return RessourceResponse.fromEntity(saved);
    }

    @Transactional
    public RessourceResponse updateStatut(Long id, Ressource.StatutRessource statut, String actingRole) {
        Ressource r = ressourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ressource introuvable avec l'id " + id));

        boolean isAdmin = ROLE_ADMIN.equals(actingRole);

        if (!isAdmin) {
            ensureNoActionPending(r);
        }

        if (!isAdmin) {
            r.snapshotAvant();
        }
        r.setStatut(statut);

        if (isAdmin) {
            r.setValidationStatus(StatutValidationRessource.VALIDEE);
            r.setPendingAction(null);
            r.clearAvant();
        } else {
            r.setValidationStatus(StatutValidationRessource.EN_ATTENTE_CHEF_CHANTIER);
            r.setPendingAction(TypeActionRessource.CHANGEMENT_STATUT);
        }
        r.setRejectionReason(null);

        Ressource saved = ressourceRepository.save(r);

        journalService.logAction(
                isAdmin ? TypeAction.MODIFICATION : TypeAction.SOUMISSION,
                "RESSOURCE",
                saved.getId(),
                isAdmin
                        ? "Changement direct de statut par l'Administrateur pour la ressource " + saved.getNom() + " -> " + statut
                        : "Soumission du changement de statut de la ressource " + saved.getNom() + " -> " + statut + " (en attente de validation)",
                null
        );

        return RessourceResponse.fromEntity(saved);
    }

    /**
     * Pour le Magasinier : place en attente de validation.
     * Pour l'Admin : suppression IMMÉDIATE et définitive (retourne null,
     * comme valider() le fait déjà pour une action SUPPRESSION validée).
     */
    @Transactional
    public RessourceResponse delete(Long id, String actingRole) {
        Ressource r = ressourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ressource introuvable avec l'id " + id));

        boolean isAdmin = ROLE_ADMIN.equals(actingRole);

        if (isAdmin) {
            String nom = r.getNom();
            Long ressourceId = r.getId();
            ressourceRepository.delete(r);

            journalService.logAction(
                    TypeAction.SUPPRESSION,
                    "RESSOURCE",
                    ressourceId,
                    "Suppression directe de la ressource par l'Administrateur: " + nom,
                    null
            );

            return null; // la ressource n'existe plus
        }

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
    // (inchangé — l'Admin peut toujours valider/rejeter au nom de
    // n'importe quel niveau, cf. requireRole ci-dessous)
    // =========================================================

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

            return null;
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