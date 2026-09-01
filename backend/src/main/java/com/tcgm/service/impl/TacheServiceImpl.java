package com.tcgm.service.impl;

import com.tcgm.dto.request.TacheCreateRequest;
import com.tcgm.dto.request.TacheUpdateRequest;
import com.tcgm.dto.response.TacheResponse;
import com.tcgm.dto.response.TacheDetailResponse;
import com.tcgm.exception.BadRequestException;
import com.tcgm.exception.ResourceNotFoundException;
import com.tcgm.mapper.TacheMapper;
import com.tcgm.model.Tache;
import com.tcgm.model.Travaux;
import com.tcgm.model.Ouvrier;
import com.tcgm.model.AffectationOuvrierTache;
import com.tcgm.model.enums.StatutTache;
import com.tcgm.repository.TacheRepository;
import com.tcgm.repository.TravauxRepository;
import com.tcgm.repository.OuvrierRepository;
import com.tcgm.repository.AffectationOuvrierTacheRepository;
import com.tcgm.security.SecurityUtils;
import com.tcgm.service.TacheService;
import com.tcgm.service.JournalService;
import com.tcgm.model.enums.TypeAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TacheServiceImpl implements TacheService {

    private final TacheRepository tacheRepository;
    private final TravauxRepository travauxRepository;
    private final OuvrierRepository ouvrierRepository;
    private final AffectationOuvrierTacheRepository affectationRepository;
    private final TacheMapper tacheMapper;
    private final JournalService journalService;
    // ⚠️ NOUVEAU
    private final SecurityUtils securityUtils;

    // =========================================================
    // ⚠️ NOUVEAU — vérification d'appartenance au chantier
    // =========================================================

    private void checkChantierAccess(Long chantierId) {
        if (!securityUtils.isChantierInScope(chantierId)) {
            throw new AccessDeniedException("Vous n'avez pas accès à cette tâche (chantier hors de votre périmètre).");
        }
    }

    private void checkTacheAccess(Tache tache) {
        Long chantierId = (tache.getTravaux() != null && tache.getTravaux().getChantier() != null)
            ? tache.getTravaux().getChantier().getId() : null;
        checkChantierAccess(chantierId);
    }

    @Override
    @Transactional
    public TacheResponse createTache(TacheCreateRequest request) {
        log.info("Création d'une nouvelle tâche: {}", request.getTitle());

        Travaux travaux = travauxRepository.findById(request.getTravauxId())
            .orElseThrow(() -> new ResourceNotFoundException("Travaux", request.getTravauxId()));

        // ⚠️ NOUVEAU : impossible de créer une tâche sur un chantier hors périmètre
        checkChantierAccess(travaux.getChantier() != null ? travaux.getChantier().getId() : null);

        Tache tache = tacheMapper.toEntity(request);
        tache.setTravaux(travaux);
        tache = tacheRepository.save(tache);

        journalService.logAction(
            TypeAction.CREATION,
            "TACHE",
            tache.getId(),
            "Création de la tâche: " + tache.getTitle(),
            null
        );

        log.info("Tâche créée avec succès: {}", tache.getTitle());
        return tacheMapper.toResponse(tache);
    }

    @Override
    @Transactional
    public TacheResponse updateTache(Long id, TacheUpdateRequest request) {
        log.info("Mise à jour de la tâche ID: {}", id);

        Tache tache = tacheRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tâche", id));

        checkTacheAccess(tache); // ⚠️ NOUVEAU

        tacheMapper.updateEntity(tache, request);
        tache = tacheRepository.save(tache);

        journalService.logAction(
            TypeAction.MODIFICATION,
            "TACHE",
            tache.getId(),
            "Mise à jour de la tâche: " + tache.getTitle(),
            null
        );

        log.info("Tâche mise à jour avec succès: {}", tache.getTitle());
        return tacheMapper.toResponse(tache);
    }

    @Override
    public TacheDetailResponse getTacheById(Long id) {
        log.debug("Récupération de la tâche ID: {}", id);
        Tache tache = tacheRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tâche", id));

        checkTacheAccess(tache); // ⚠️ NOUVEAU

        return tacheMapper.toDetailResponse(tache);
    }

    @Override
    public Page<TacheResponse> getAllTaches(Long travauxId, String status, String search, Pageable pageable) {
        log.debug("Récupération de toutes les tâches");

        StatutTache statut = null;
        if (status != null) {
            try {
                statut = StatutTache.valueOf(status);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Statut invalide: " + status);
            }
        }

        // ⚠️ NOUVEAU : scoping par rôle
        List<Long> chantierIds = securityUtils.getScopedChantierIds();
        if (chantierIds != null && chantierIds.isEmpty()) {
            return Page.empty(pageable);
        }

        Page<Tache> taches = tacheRepository.findTachesWithFilters(travauxId, statut, search, chantierIds, pageable);
        return taches.map(tacheMapper::toResponse);
    }

    @Override
    @Transactional
    public void deleteTache(Long id) {
        log.info("Suppression de la tâche ID: {}", id);

        Tache tache = tacheRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tâche", id));

        checkTacheAccess(tache); // ⚠️ NOUVEAU

        journalService.logAction(
            TypeAction.SUPPRESSION,
            "TACHE",
            tache.getId(),
            "Suppression de la tâche: " + tache.getTitle(),
            null
        );

        tacheRepository.delete(tache);
        log.info("Tâche supprimée avec succès");
    }

    @Override
    @Transactional
    public TacheResponse updateTacheStatus(Long id, String status) {
        log.info("Mise à jour du statut de la tâche ID: {} vers {}", id, status);

        Tache tache = tacheRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tâche", id));

        checkTacheAccess(tache); // ⚠️ NOUVEAU

        StatutTache newStatus;
        try {
            newStatus = StatutTache.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Statut invalide: " + status);
        }

        tache.setStatus(newStatus);

        if (newStatus == StatutTache.TERMINEE && tache.getCompletedDate() == null) {
            tache.setCompletedDate(LocalDateTime.now());
        }

        tache = tacheRepository.save(tache);

        journalService.logAction(
            TypeAction.MODIFICATION,
            "TACHE",
            tache.getId(),
            "Changement de statut de la tâche: " + tache.getTitle() + " -> " + status,
            null
        );

        log.info("Statut de la tâche mis à jour avec succès");
        return tacheMapper.toResponse(tache);
    }

    @Override
    @Transactional
    public TacheDetailResponse affecterOuvrierTache(Long tacheId, Long ouvrierId) {
        log.info("Affectation de l'ouvrier {} à la tâche {}", ouvrierId, tacheId);

        Tache tache = tacheRepository.findById(tacheId)
            .orElseThrow(() -> new ResourceNotFoundException("Tâche", tacheId));

        checkTacheAccess(tache); // ⚠️ NOUVEAU

        Ouvrier ouvrier = ouvrierRepository.findById(ouvrierId)
            .orElseThrow(() -> new ResourceNotFoundException("Ouvrier", ouvrierId));

        if (affectationRepository.isOuvrierAffectedToTache(ouvrierId, tacheId)) {
            throw new BadRequestException("Cet ouvrier est déjà affecté à cette tâche");
        }

        AffectationOuvrierTache affectation = AffectationOuvrierTache.builder()
            .tache(tache)
            .ouvrier(ouvrier)
            .build();

        affectationRepository.save(affectation);

        journalService.logAction(
            TypeAction.AFFECTATION,
            "TACHE",
            tache.getId(),
            "Affectation de l'ouvrier " + ouvrier.getFirstName() + " à la tâche " + tache.getTitle(),
            null
        );

        log.info("Ouvrier affecté à la tâche avec succès");
        return tacheMapper.toDetailResponse(tache);
    }

    @Override
    @Transactional
    public void retirerOuvrierTache(Long tacheId, Long ouvrierId) {
        log.info("Retrait de l'ouvrier {} de la tâche {}", ouvrierId, tacheId);

        Tache tache = tacheRepository.findById(tacheId)
            .orElseThrow(() -> new ResourceNotFoundException("Tâche", tacheId));

        checkTacheAccess(tache); // ⚠️ NOUVEAU

        Ouvrier ouvrier = ouvrierRepository.findById(ouvrierId)
            .orElseThrow(() -> new ResourceNotFoundException("Ouvrier", ouvrierId));

        AffectationOuvrierTache affectation = affectationRepository
            .findByOuvrierIdAndTacheId(ouvrierId, tacheId)
            .orElseThrow(() -> new ResourceNotFoundException("Affectation non trouvée"));

        affectationRepository.delete(affectation);

        journalService.logAction(
            TypeAction.DESAFFECTATION,
            "TACHE",
            tache.getId(),
            "Retrait de l'ouvrier " + ouvrier.getFirstName() + " de la tâche " + tache.getTitle(),
            null
        );

        log.info("Ouvrier retiré de la tâche avec succès");
    }

    @Override
    public Page<TacheResponse> getTachesByTravaux(Long travauxId, Pageable pageable) {
        log.debug("Récupération des tâches des travaux ID: {}", travauxId);

        Travaux travaux = travauxRepository.findById(travauxId)
            .orElseThrow(() -> new ResourceNotFoundException("Travaux", travauxId));

        // ⚠️ NOUVEAU
        checkChantierAccess(travaux.getChantier() != null ? travaux.getChantier().getId() : null);

        Page<Tache> taches = tacheRepository.findByTravauxId(travauxId, pageable);
        return taches.map(tacheMapper::toResponse);
    }

    @Override
    @Transactional
    public TacheResponse proposerModification(Long id, String proposedStatus, LocalDateTime proposedPlannedDate) {
        Tache tache = tacheRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tâche", id));

        checkTacheAccess(tache); // ⚠️ NOUVEAU

        if (proposedStatus == null && proposedPlannedDate == null) {
            throw new BadRequestException("Aucune modification proposée (statut ou date requis).");
        }

        StringBuilder details = new StringBuilder("Proposition de modification pour la tâche " + tache.getTitle() + " :");

        if (proposedStatus != null) {
            StatutTache newStatus;
            try {
                newStatus = StatutTache.valueOf(proposedStatus);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Statut invalide: " + proposedStatus);
            }
            tache.setPreviousStatus(tache.getStatus());
            tache.setStatus(newStatus);
            tache.setProposedStatus(newStatus);
            details.append(" statut -> ").append(newStatus);
        }

        if (proposedPlannedDate != null) {
            tache.setProposedPlannedDate(proposedPlannedDate);
            details.append(" date prévue -> ").append(proposedPlannedDate);
        }

        tache.setRejectionReason(null);

        Tache saved = tacheRepository.save(tache);

        journalService.logAction(
            TypeAction.MODIFICATION,
            "TACHE",
            saved.getId(),
            details.toString(),
            null
        );

        return tacheMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TacheResponse validerModification(Long id) {
        Tache tache = tacheRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tâche", id));

        checkTacheAccess(tache); // ⚠️ NOUVEAU

        boolean hasPending = tache.getProposedStatus() != null || tache.getProposedPlannedDate() != null;
        if (!hasPending) {
            throw new BadRequestException("Aucune modification en attente pour cette tâche.");
        }

        if (tache.getProposedPlannedDate() != null) {
            tache.setPlannedDate(tache.getProposedPlannedDate());
            tache.setProposedPlannedDate(null);
        }

        if (tache.getProposedStatus() == StatutTache.TERMINEE && tache.getCompletedDate() == null) {
            tache.setCompletedDate(LocalDateTime.now());
        }

        tache.setPreviousStatus(null);
        tache.setProposedStatus(null);
        tache.setRejectionReason(null);

        Tache saved = tacheRepository.save(tache);

        journalService.logAction(
            TypeAction.MODIFICATION,
            "TACHE",
            saved.getId(),
            "Validation de la modification de la tâche: " + saved.getTitle(),
            null
        );

        return tacheMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TacheResponse rejeterModification(Long id, String motif) {
        Tache tache = tacheRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tâche", id));

        checkTacheAccess(tache); // ⚠️ NOUVEAU

        boolean hasPending = tache.getProposedStatus() != null || tache.getProposedPlannedDate() != null;
        if (!hasPending) {
            throw new BadRequestException("Aucune modification en attente pour cette tâche.");
        }

        if (tache.getPreviousStatus() != null) {
            tache.setStatus(tache.getPreviousStatus());
        }

        tache.setPreviousStatus(null);
        tache.setProposedStatus(null);
        tache.setProposedPlannedDate(null);
        tache.setRejectionReason(motif);

        Tache saved = tacheRepository.save(tache);

        journalService.logAction(
            TypeAction.MODIFICATION,
            "TACHE",
            saved.getId(),
            "Rejet de la modification de la tâche: " + saved.getTitle()
                + (motif != null && !motif.isBlank() ? " — motif: " + motif : ""),
            null
        );

        return tacheMapper.toResponse(saved);
    }
}