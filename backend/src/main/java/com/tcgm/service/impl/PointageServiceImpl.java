package com.tcgm.service.impl;

import com.tcgm.dto.request.DossierPointageRequest;
import com.tcgm.dto.request.LignePointageRequest;
import com.tcgm.dto.request.ValidationPointageRequest;
import com.tcgm.dto.response.DossierPointageResponse;
import com.tcgm.dto.response.LignePointageResponse;
import com.tcgm.exception.BadRequestException;
import com.tcgm.exception.ResourceNotFoundException;
import com.tcgm.mapper.PointageMapper;
import com.tcgm.model.DossierPointage;
import com.tcgm.model.LignePointage;
import com.tcgm.model.Site;
import com.tcgm.model.Ouvrier;
import com.tcgm.model.Tache;
import com.tcgm.model.User;
import com.tcgm.model.enums.StatutPointage;
import com.tcgm.repository.DossierPointageRepository;
import com.tcgm.repository.LignePointageRepository;
import com.tcgm.repository.SiteRepository;
import com.tcgm.repository.OuvrierRepository;
import com.tcgm.repository.TacheRepository;
import com.tcgm.repository.UserRepository;
import com.tcgm.security.SecurityUtils;
import com.tcgm.service.PointageService;
import com.tcgm.service.JournalService;
import com.tcgm.model.enums.TypeAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PointageServiceImpl implements PointageService {

    private final DossierPointageRepository dossierRepository;
    private final LignePointageRepository ligneRepository;
    private final SiteRepository siteRepository;
    private final OuvrierRepository ouvrierRepository;
    private final TacheRepository tacheRepository;
    private final UserRepository userRepository;
    private final PointageMapper pointageMapper;
    private final JournalService journalService;
    // ⚠️ NOUVEAU
    private final SecurityUtils securityUtils;

    // =========================================================
    // ⚠️ NOUVEAU — vérification d'appartenance au chantier
    // =========================================================

    private void checkChantierAccess(Long siteId) {
        if (!securityUtils.isChantierInScope(siteId)) {
            throw new AccessDeniedException("Vous n'avez pas accès à ce pointage (chantier hors de votre périmètre).");
        }
    }

    private void checkDossierAccess(DossierPointage dossier) {
        checkChantierAccess(dossier.getSite() != null ? dossier.getSite().getId() : null);
    }

    @Override
    @Transactional
    public DossierPointageResponse createDossierPointage(DossierPointageRequest request) {
        log.info("Création d'un dossier de pointage pour le site {} à la date {}",
            request.getSiteId(), request.getDate());

        Site site = siteRepository.findById(request.getSiteId())
            .orElseThrow(() -> new ResourceNotFoundException("Site", request.getSiteId()));

        checkChantierAccess(site.getId()); // ⚠️ NOUVEAU

        if (dossierRepository.existsBySiteIdAndDate(request.getSiteId(), request.getDate())) {
            throw new BadRequestException("Un dossier de pointage existe déjà pour cette date et ce site");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User createdBy = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", email));

        DossierPointage dossier = pointageMapper.toDossierEntity(request);
        dossier.setSite(site);
        dossier.setCreatedBy(createdBy);

        dossier = dossierRepository.save(dossier);

        journalService.logAction(
            TypeAction.CREATION,
            "POINTAGE",
            dossier.getId(),
            "Création d'un dossier de pointage pour le site " + site.getName(),
            null
        );

        log.info("Dossier de pointage créé avec succès");
        return pointageMapper.toDossierResponse(dossier);
    }

    @Override
    public DossierPointageResponse getDossierPointageById(Long id) {
        DossierPointage dossier = dossierRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Dossier de pointage", id));
        checkDossierAccess(dossier); // ⚠️ NOUVEAU
        return pointageMapper.toDossierResponse(dossier);
    }

    @Override
    public Page<DossierPointageResponse> getAllDossiersPointage(Long siteId, String date, String status, Pageable pageable) {
        StatutPointage statut = null;
        if (status != null) {
            try {
                statut = StatutPointage.valueOf(status);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Statut invalide: " + status);
            }
        }

        if (date != null) {
            LocalDate.parse(date);
        }

        // ⚠️ NOUVEAU : scoping par rôle
        List<Long> scoped = securityUtils.getScopedChantierIds();
        if (scoped != null && scoped.isEmpty()) {
            return Page.empty(pageable);
        }
        if (siteId != null) {
            checkChantierAccess(siteId);
        }

        Page<DossierPointage> dossiers = dossierRepository.findDossiersWithFilters(siteId, statut, scoped, pageable);
        return dossiers.map(pointageMapper::toDossierResponse);
    }

    private void verifierModifiable(DossierPointage dossier) {
        if (dossier.getStatus() != StatutPointage.EN_ATTENTE) {
            throw new BadRequestException(
                "Ce dossier de pointage ne peut plus être modifié (statut actuel : " + dossier.getStatus() + ")");
        }
    }

    @Override
    @Transactional
    public DossierPointageResponse updateDossierPointage(Long id, DossierPointageRequest request) {
        log.info("Mise à jour du dossier de pointage ID: {}", id);

        DossierPointage dossier = dossierRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Dossier de pointage", id));

        checkDossierAccess(dossier); // ⚠️ NOUVEAU
        verifierModifiable(dossier);

        if (request.getNotes() != null) {
            dossier.setNotes(request.getNotes());
        }

        dossier = dossierRepository.save(dossier);

        journalService.logAction(
            TypeAction.MODIFICATION,
            "POINTAGE",
            dossier.getId(),
            "Mise à jour du dossier de pointage pour le site " + dossier.getSite().getName(),
            null
        );

        log.info("Dossier de pointage mis à jour avec succès");
        return pointageMapper.toDossierResponse(dossier);
    }

    @Override
    @Transactional
    public void deleteDossierPointage(Long id) {
        log.info("Suppression du dossier de pointage ID: {}", id);

        DossierPointage dossier = dossierRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Dossier de pointage", id));

        checkDossierAccess(dossier); // ⚠️ NOUVEAU
        verifierModifiable(dossier);

        journalService.logAction(
            TypeAction.SUPPRESSION,
            "POINTAGE",
            dossier.getId(),
            "Suppression du dossier de pointage du site " + dossier.getSite().getName(),
            null
        );

        dossierRepository.delete(dossier);
        log.info("Dossier de pointage supprimé avec succès");
    }

    @Override
    @Transactional
    public LignePointageResponse addLignePointage(Long dossierId, LignePointageRequest request) {
        log.info("Ajout d'une ligne de pointage au dossier {}", dossierId);

        DossierPointage dossier = dossierRepository.findById(dossierId)
            .orElseThrow(() -> new ResourceNotFoundException("Dossier de pointage", dossierId));

        checkDossierAccess(dossier); // ⚠️ NOUVEAU
        verifierModifiable(dossier);

        Ouvrier ouvrier = ouvrierRepository.findById(request.getOuvrierId())
            .orElseThrow(() -> new ResourceNotFoundException("Ouvrier", request.getOuvrierId()));

        Tache tache = tacheRepository.findById(request.getTacheId())
            .orElseThrow(() -> new ResourceNotFoundException("Tâche", request.getTacheId()));

        if (Boolean.FALSE.equals(request.getHalfDay()) || request.getHalfDay() == null) {
            if (request.getStartTime() == null || request.getEndTime() == null) {
                throw new BadRequestException("L'heure de début et de fin sont obligatoires (ou cochez demi-journée)");
            }
            if (!request.getStartTime().isBefore(request.getEndTime())) {
                throw new BadRequestException("L'heure de début doit être antérieure à l'heure de fin");
            }
        }

        LignePointage ligne = pointageMapper.toLigneEntity(request);
        ligne.setDossier(dossier);
        ligne.setOuvrier(ouvrier);
        ligne.setTache(tache);

        ligne = ligneRepository.save(ligne);

        journalService.logAction(
            TypeAction.MODIFICATION,
            "POINTAGE",
            dossier.getId(),
            "Ajout de l'ouvrier " + ouvrier.getFirstName() + " " + ouvrier.getLastName() + " au pointage du "
                + dossier.getDate() + " (site " + dossier.getSite().getName() + ")",
            null
        );

        log.info("Ligne de pointage ajoutée avec succès");
        return pointageMapper.toLigneResponse(ligne);
    }

    @Override
    @Transactional
    public void removeLignePointage(Long ligneId) {
        log.info("Suppression de la ligne de pointage ID: {}", ligneId);

        LignePointage ligne = ligneRepository.findById(ligneId)
            .orElseThrow(() -> new ResourceNotFoundException("Ligne de pointage", ligneId));

        DossierPointage dossier = ligne.getDossier();
        checkDossierAccess(dossier); // ⚠️ NOUVEAU
        verifierModifiable(dossier);

        journalService.logAction(
            TypeAction.MODIFICATION,
            "POINTAGE",
            dossier.getId(),
            "Retrait de l'ouvrier " + ligne.getOuvrier().getFirstName() + " " + ligne.getOuvrier().getLastName()
                + " du pointage du " + dossier.getDate(),
            null
        );

        ligneRepository.delete(ligne);
        log.info("Ligne de pointage supprimée avec succès");
    }

    @Override
    @Transactional
    public DossierPointageResponse soumettreDossierPointage(Long id) {
        log.info("Soumission pour validation du dossier de pointage ID: {}", id);

        DossierPointage dossier = dossierRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Dossier de pointage", id));

        checkDossierAccess(dossier); // ⚠️ NOUVEAU

        if (dossier.getStatus() != StatutPointage.EN_ATTENTE) {
            throw new BadRequestException(
                "Seul un dossier \"En attente\" peut être soumis pour validation (statut actuel : "
                    + dossier.getStatus() + ")");
        }

        if (dossier.getLignes() == null || dossier.getLignes().isEmpty()) {
            throw new BadRequestException("Impossible de soumettre un pointage sans aucun ouvrier");
        }

        dossier.setStatus(StatutPointage.EN_ATTENTE_VALIDATION);
        dossier = dossierRepository.save(dossier);

        journalService.logAction(
            TypeAction.SOUMISSION,
            "POINTAGE",
            dossier.getId(),
            "Soumission pour validation du pointage du " + dossier.getDate()
                + " (site " + dossier.getSite().getName() + ")",
            null
        );

        log.info("Dossier de pointage soumis pour validation");
        return pointageMapper.toDossierResponse(dossier);
    }

    @Override
    @Transactional
    public DossierPointageResponse validerDossierPointage(Long id, ValidationPointageRequest request) {
        log.info("Validation du dossier de pointage ID: {}", id);

        DossierPointage dossier = dossierRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Dossier de pointage", id));

        checkDossierAccess(dossier); // ⚠️ NOUVEAU

        if (dossier.getStatus() != StatutPointage.EN_ATTENTE_VALIDATION) {
            throw new BadRequestException(
                "Seul un dossier \"En attente de validation\" peut être validé (statut actuel : "
                    + dossier.getStatus() + ")");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User validatedBy = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", email));

        dossier.setStatus(StatutPointage.VALIDE);
        dossier.setValidatedBy(validatedBy);
        dossier.setValidatedAt(LocalDateTime.now());

        if (request.getNotes() != null && !request.getNotes().isBlank()) {
            String notes = dossier.getNotes() != null ? dossier.getNotes() : "";
            dossier.setNotes(notes + "\nValidation: " + request.getNotes());
        }

        dossier = dossierRepository.save(dossier);

        journalService.logAction(
            TypeAction.VALIDATION,
            "POINTAGE",
            dossier.getId(),
            "Validation du dossier de pointage du site " + dossier.getSite().getName()
                + " par " + validatedBy.getEmail(),
            null
        );

        log.info("Dossier de pointage validé avec succès");
        return pointageMapper.toDossierResponse(dossier);
    }

    @Override
    @Transactional
    public DossierPointageResponse rejeterDossierPointage(Long id, ValidationPointageRequest request) {
        log.info("Rejet du dossier de pointage ID: {}", id);

        DossierPointage dossier = dossierRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Dossier de pointage", id));

        checkDossierAccess(dossier); // ⚠️ NOUVEAU

        if (dossier.getStatus() != StatutPointage.EN_ATTENTE_VALIDATION) {
            throw new BadRequestException(
                "Seul un dossier \"En attente de validation\" peut être rejeté (statut actuel : "
                    + dossier.getStatus() + ")");
        }

        dossier.setStatus(StatutPointage.REJETE);

        if (request.getMotifRejet() != null && !request.getMotifRejet().isBlank()) {
            String notes = dossier.getNotes() != null ? dossier.getNotes() : "";
            dossier.setNotes(notes + "\nMotif du rejet: " + request.getMotifRejet());
        }

        dossier = dossierRepository.save(dossier);

        journalService.logAction(
            TypeAction.REJET,
            "POINTAGE",
            dossier.getId(),
            "Rejet du dossier de pointage du site " + dossier.getSite().getName(),
            null
        );

        log.info("Dossier de pointage rejeté");
        return pointageMapper.toDossierResponse(dossier);
    }

    @Override
    public DossierPointageResponse getTodayPointage(Long siteId) {
        checkChantierAccess(siteId); // ⚠️ NOUVEAU

        LocalDate today = LocalDate.now();
        DossierPointage dossier = dossierRepository.findBySiteIdAndDate(siteId, today)
            .orElseThrow(() -> new ResourceNotFoundException("Aucun pointage trouvé pour aujourd'hui"));

        return pointageMapper.toDossierResponse(dossier);
    }

    @Override
    public Map<String, Object> getPointageStatistiques(Long siteId) {
        checkChantierAccess(siteId); // ⚠️ NOUVEAU

        Map<String, Object> stats = new HashMap<>();

        long totalDossiers = dossierRepository.findBySiteId(siteId, Pageable.unpaged()).getTotalElements();
        long dossiersValides = dossierRepository.findBySiteIdAndStatus(siteId, StatutPointage.VALIDE, Pageable.unpaged()).getTotalElements();
        long dossiersEnAttente = dossierRepository.findBySiteIdAndStatus(siteId, StatutPointage.EN_ATTENTE, Pageable.unpaged()).getTotalElements();
        long dossiersEnAttenteValidation = dossierRepository.findBySiteIdAndStatus(siteId, StatutPointage.EN_ATTENTE_VALIDATION, Pageable.unpaged()).getTotalElements();
        long dossiersRejetes = dossierRepository.findBySiteIdAndStatus(siteId, StatutPointage.REJETE, Pageable.unpaged()).getTotalElements();

        stats.put("totalDossiers", totalDossiers);
        stats.put("dossiersValides", dossiersValides);
        stats.put("dossiersEnAttente", dossiersEnAttente);
        stats.put("dossiersEnAttenteValidation", dossiersEnAttenteValidation);
        stats.put("dossiersRejetes", dossiersRejetes);
        stats.put("tauxValidation", totalDossiers > 0 ? (dossiersValides * 100.0 / totalDossiers) : 0);

        return stats;
    }
}