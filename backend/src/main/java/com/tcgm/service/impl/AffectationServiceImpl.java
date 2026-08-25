package com.tcgm.service.impl;

import com.tcgm.dto.request.AffectationRejetRequest;
import com.tcgm.dto.request.AffectationRequest;
import com.tcgm.dto.response.AffectationResponse;
import com.tcgm.model.Affectation;
import com.tcgm.model.Ouvrier;
import com.tcgm.model.Site;
import com.tcgm.model.enums.StatutAffectation;
import com.tcgm.model.enums.TypeAction;
import com.tcgm.repository.AffectationRepository;
import com.tcgm.repository.OuvrierRepository;
import com.tcgm.repository.SiteRepository;
import com.tcgm.service.AffectationService;
import com.tcgm.service.JournalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AffectationServiceImpl implements AffectationService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_CHEF_PROJET = "CHEF_PROJET";
    private static final String ROLE_CHEF_CHANTIER = "CHEF_CHANTIER";

    private final AffectationRepository affectationRepository;
    private final OuvrierRepository ouvrierRepository;
    private final SiteRepository siteRepository;
    private final JournalService journalService;

    // =========================================================
    // CREATE
    // =========================================================

    @Override
    public AffectationResponse createAffectation(AffectationRequest request, String creatingRole) {

        Site chantier = siteRepository.findById(request.getChantierId())
                .orElseThrow(() ->
                        new RuntimeException("Chantier introuvable avec l'id : " + request.getChantierId()));

        Ouvrier ouvrier = ouvrierRepository.findById(request.getOuvrierId())
                .orElseThrow(() ->
                        new RuntimeException("Ouvrier introuvable avec l'id : " + request.getOuvrierId()));

        if (affectationRepository.hasAffectationEnCours(ouvrier.getId())) {
            throw new IllegalStateException(
                    "Cet ouvrier possède déjà une affectation en cours."
            );
        }

        Affectation affectation = new Affectation();

        affectation.setDateDebut(request.getDateDebut());
        affectation.setDateFin(request.getDateFin());
        affectation.setChantier(chantier);
        affectation.setOuvrier(ouvrier);

        boolean requiresValidation = ROLE_CHEF_CHANTIER.equals(creatingRole);

        if (requiresValidation) {
            // Circuit de validation : le Chef de Chantier propose, la
            // décision finale (EN_COURS ou REJETEE) appartient au Chef de Projet.
            affectation.setStatut(StatutAffectation.EN_ATTENTE_VALIDATION);
        } else if (request.getStatut() != null && !request.getStatut().isBlank()) {
            affectation.setStatut(StatutAffectation.valueOf(request.getStatut().toUpperCase()));
        } else {
            affectation.setStatut(StatutAffectation.EN_COURS);
        }

        Affectation saved = affectationRepository.save(affectation);

        journalService.logAction(
                requiresValidation ? TypeAction.SOUMISSION : TypeAction.CREATION,
                "AFFECTATION",
                saved.getId(),
                (requiresValidation ? "Soumission d'une nouvelle affectation (en attente de validation) : "
                        : "Création de l'affectation : ")
                        + ouvrier.getFirstName() + " " + ouvrier.getLastName() + " -> " + chantier.getName(),
                null
        );

        return toResponse(saved);
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @Override
    public AffectationResponse updateAffectation(Long id, AffectationRequest request) {

        Affectation affectation = affectationRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Affectation introuvable avec l'id : " + id));

        Site chantier = siteRepository.findById(request.getChantierId())
                .orElseThrow(() ->
                        new RuntimeException("Chantier introuvable avec l'id : " + request.getChantierId()));

        Ouvrier ouvrier = ouvrierRepository.findById(request.getOuvrierId())
                .orElseThrow(() ->
                        new RuntimeException("Ouvrier introuvable avec l'id : " + request.getOuvrierId()));

        affectation.setDateDebut(request.getDateDebut());
        affectation.setDateFin(request.getDateFin());
        affectation.setChantier(chantier);
        affectation.setOuvrier(ouvrier);

        if (request.getStatut() != null && !request.getStatut().isBlank()) {
            affectation.setStatut(StatutAffectation.valueOf(request.getStatut().toUpperCase()));
        }

        Affectation updated = affectationRepository.save(affectation);

        journalService.logAction(
                TypeAction.MODIFICATION,
                "AFFECTATION",
                updated.getId(),
                "Modification de l'affectation #" + updated.getId(),
                null
        );

        return toResponse(updated);
    }

    // =========================================================
    // GET BY ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public AffectationResponse getAffectationById(Long id) {
        Affectation affectation = affectationRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Affectation introuvable avec l'id : " + id));
        return toResponse(affectation);
    }

    // =========================================================
    // GET ALL + FILTRES
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Page<AffectationResponse> getAllAffectations(Long chantierId, Long ouvrierId, String statut, Pageable pageable) {

        List<Affectation> affectations = affectationRepository.findAll();

        if (chantierId != null) {
            affectations = affectations.stream()
                    .filter(a -> a.getChantier() != null && a.getChantier().getId().equals(chantierId))
                    .toList();
        }

        if (ouvrierId != null) {
            affectations = affectations.stream()
                    .filter(a -> a.getOuvrier() != null && a.getOuvrier().getId().equals(ouvrierId))
                    .toList();
        }

        if (statut != null && !statut.isBlank()) {
            StatutAffectation statutEnum = StatutAffectation.valueOf(statut.toUpperCase());
            affectations = affectations.stream()
                    .filter(a -> a.getStatut() == statutEnum)
                    .toList();
        }

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), affectations.size());

        List<Affectation> pageContent = start >= affectations.size() ? List.of() : affectations.subList(start, end);
        List<AffectationResponse> responses = pageContent.stream().map(this::toResponse).toList();

        return new PageImpl<>(responses, pageable, affectations.size());
    }

    // =========================================================
    // DELETE
    // =========================================================

    @Override
    public void deleteAffectation(Long id) {
        Affectation affectation = affectationRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Affectation introuvable avec l'id : " + id));

        journalService.logAction(
                TypeAction.SUPPRESSION,
                "AFFECTATION",
                id,
                "Suppression de l'affectation #" + id,
                null
        );

        affectationRepository.delete(affectation);
    }

    // =========================================================
    // OVERRIDE DIRECT — ADMIN UNIQUEMENT
    // =========================================================

    @Override
    public AffectationResponse updateStatut(Long id, String statut) {

        Affectation affectation = affectationRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Affectation introuvable avec l'id : " + id));

        if (statut == null || statut.isBlank()) {
            throw new IllegalArgumentException("Le statut est obligatoire.");
        }

        StatutAffectation nouveauStatut;
        try {
            nouveauStatut = StatutAffectation.valueOf(statut.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Statut d'affectation invalide : " + statut);
        }

        affectation.setStatut(nouveauStatut);
        affectation.setRejectionReason(null);

        if (nouveauStatut != StatutAffectation.EN_COURS
                && nouveauStatut != StatutAffectation.EN_ATTENTE_VALIDATION
                && affectation.getDateFin() == null) {
            affectation.setDateFin(LocalDate.now());
        }

        Affectation updated = affectationRepository.save(affectation);

        journalService.logAction(
                TypeAction.MODIFICATION,
                "AFFECTATION",
                updated.getId(),
                "[Override Admin] Changement de statut de l'affectation #" + id + " -> " + statut,
                null
        );

        return toResponse(updated);
    }

    // =========================================================
    // CIRCUIT DE VALIDATION : Chef de Chantier -> Chef de Projet
    // =========================================================

    @Override
    public AffectationResponse validerAffectation(Long id, String validatingRole) {
        Affectation affectation = affectationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Affectation introuvable avec l'id : " + id));

        if (affectation.getStatut() != StatutAffectation.EN_ATTENTE_VALIDATION) {
            throw new IllegalStateException("Cette affectation n'a pas de demande de validation en attente");
        }

        requireRole(validatingRole, "Seul le Chef de Projet peut valider une affectation");

        affectation.setStatut(StatutAffectation.EN_COURS);
        affectation.setRejectionReason(null);

        Affectation saved = affectationRepository.save(affectation);

        journalService.logAction(
                TypeAction.VALIDATION,
                "AFFECTATION",
                saved.getId(),
                "Validation de l'affectation #" + saved.getId(),
                null
        );

        return toResponse(saved);
    }

    @Override
    public AffectationResponse rejeterAffectation(Long id, AffectationRejetRequest request, String validatingRole) {
        Affectation affectation = affectationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Affectation introuvable avec l'id : " + id));

        if (affectation.getStatut() != StatutAffectation.EN_ATTENTE_VALIDATION) {
            throw new IllegalStateException("Cette affectation n'a pas de demande de validation en attente");
        }

        requireRole(validatingRole, "Seul le Chef de Projet peut rejeter une affectation");

        String motif = request != null ? request.getMotif() : null;

        affectation.setStatut(StatutAffectation.REJETEE);
        affectation.setRejectionReason(motif);

        Affectation saved = affectationRepository.save(affectation);

        journalService.logAction(
                TypeAction.REJET,
                "AFFECTATION",
                saved.getId(),
                "Rejet de l'affectation #" + saved.getId() + (motif != null ? " — Motif: " + motif : ""),
                null
        );

        return toResponse(saved);
    }

    // =========================================================
    // AFFECTATION EN COURS D'UN OUVRIER
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public AffectationResponse getAffectationEnCoursByOuvrier(Long ouvrierId) {
        Affectation affectation = affectationRepository.findAffectationEnCoursByOuvrier(ouvrierId)
                .orElseThrow(() -> new RuntimeException("Aucune affectation en cours pour l'ouvrier : " + ouvrierId));
        return toResponse(affectation);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AffectationResponse> getAffectationsByChantier(Long chantierId, Pageable pageable) {
        Page<Affectation> page = affectationRepository.findByChantierId(chantierId, pageable);
        return page.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AffectationResponse> getAffectationsByOuvrier(Long ouvrierId, Pageable pageable) {
        Page<Affectation> page = affectationRepository.findByOuvrierId(ouvrierId, pageable);
        return page.map(this::toResponse);
    }

    // =========================================================
    // UTILITAIRES
    // =========================================================

    private void requireRole(String actualRole, String message) {
        if (actualRole == null || (!actualRole.equals(ROLE_CHEF_PROJET) && !actualRole.equals(ROLE_ADMIN))) {
            throw new AccessDeniedException(message);
        }
    }

    private AffectationResponse toResponse(Affectation affectation) {

        AffectationResponse.ChantierBrief chantierBrief = null;
        if (affectation.getChantier() != null) {
            chantierBrief = AffectationResponse.ChantierBrief.builder()
                    .id(affectation.getChantier().getId())
                    .name(affectation.getChantier().getName())
                    .reference(affectation.getChantier().getReference())
                    .build();
        }

        AffectationResponse.OuvrierBrief ouvrierBrief = null;
        if (affectation.getOuvrier() != null) {
            ouvrierBrief = AffectationResponse.OuvrierBrief.builder()
                    .id(affectation.getOuvrier().getId())
                    .firstName(affectation.getOuvrier().getFirstName())
                    .lastName(affectation.getOuvrier().getLastName())
                    .cin(affectation.getOuvrier().getCin())
                    .specialite(affectation.getOuvrier().getSpecialite())
                    .build();
        }

        return AffectationResponse.builder()
                .id(affectation.getId())
                .dateDebut(affectation.getDateDebut())
                .dateFin(affectation.getDateFin())
                .statut(affectation.getStatut())
                .rejectionReason(affectation.getRejectionReason())
                .chantier(chantierBrief)
                .ouvrier(ouvrierBrief)
                .createdAt(affectation.getCreatedAt())
                .updatedAt(affectation.getUpdatedAt())
                .build();
    }
}