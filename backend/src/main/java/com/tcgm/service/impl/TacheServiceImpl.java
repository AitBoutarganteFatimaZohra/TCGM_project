package com.tcgm.service.impl;

import com.tcgm.dto.request.TacheCreateRequest;
import com.tcgm.dto.request.TacheUpdateRequest;
import com.tcgm.dto.request.TacheSoumissionRequest;
import com.tcgm.dto.request.TacheRejetRequest;
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
import com.tcgm.service.TacheService;
import com.tcgm.service.JournalService;
import com.tcgm.model.enums.TypeAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

    @Override
    @Transactional
    public TacheResponse createTache(TacheCreateRequest request) {
        log.info("Création d'une nouvelle tâche: {}", request.getTitle());

        Travaux travaux = travauxRepository.findById(request.getTravauxId())
            .orElseThrow(() -> new ResourceNotFoundException("Travaux", request.getTravauxId()));

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

        // Les changements de statut et de date prévue ne passent plus par
        // cette mise à jour "libre" : ils doivent obligatoirement transiter
        // par le circuit de validation (soumettre / valider / rejeter),
        // sauf pour l'Administrateur qui garde updateTacheStatus en
        // override direct. On neutralise donc ces deux champs ici pour ne
        // jamais court-circuiter le circuit.
        request.setStatus(null);
        request.setPlannedDate(null);

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

        Page<Tache> taches = tacheRepository.findTachesWithFilters(travauxId, statut, search, pageable);
        return taches.map(tacheMapper::toResponse);
    }

    @Override
    @Transactional
    public void deleteTache(Long id) {
        log.info("Suppression de la tâche ID: {}", id);

        Tache tache = tacheRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tâche", id));

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

    /**
     * Override direct réservé à l'Administrateur (@PreAuthorize côté
     * contrôleur) : contourne volontairement le circuit de validation.
     */
    @Override
    @Transactional
    public TacheResponse updateTacheStatus(Long id, String status) {
        log.info("[OVERRIDE ADMIN] Mise à jour directe du statut de la tâche ID: {} vers {}", id, status);

        Tache tache = tacheRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tâche", id));

        StatutTache newStatus = parseStatut(status);

        tache.setStatus(newStatus);
        tache.setPreviousStatus(null);
        tache.setProposedStatus(null);
        tache.setProposedPlannedDate(null);

        if (newStatus == StatutTache.TERMINEE && tache.getCompletedDate() == null) {
            tache.setCompletedDate(LocalDateTime.now());
        }

        tache = tacheRepository.save(tache);

        journalService.logAction(
            TypeAction.MODIFICATION,
            "TACHE",
            tache.getId(),
            "[Override Admin] Changement de statut de la tâche: " + tache.getTitle() + " -> " + status,
            null
        );

        log.info("Statut de la tâche mis à jour avec succès (override admin)");
        return tacheMapper.toResponse(tache);
    }

    // =========================================================
    // CIRCUIT DE VALIDATION : Chef de Chantier -> Chef de Projet
    // =========================================================

    @Override
    @Transactional
    public TacheResponse soumettreTache(Long id, TacheSoumissionRequest request) {
        log.info("Soumission d'une demande de validation pour la tâche ID: {}", id);

        Tache tache = tacheRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tâche", id));

        if (tache.getStatus() == StatutTache.EN_ATTENTE_VALIDATION) {
            throw new BadRequestException("Cette tâche a déjà une demande de validation en attente");
        }

        boolean hasStatusChange = request.getProposedStatus() != null && !request.getProposedStatus().isBlank();
        boolean hasDateChange = request.getProposedPlannedDate() != null;

        if (!hasStatusChange && !hasDateChange) {
            throw new BadRequestException("Aucun changement proposé : indiquez un nouveau statut et/ou une nouvelle date prévue");
        }

        StatutTache proposedStatus = null;
        if (hasStatusChange) {
            proposedStatus = parseStatut(request.getProposedStatus());
            if (proposedStatus == StatutTache.EN_ATTENTE_VALIDATION) {
                throw new BadRequestException("Statut proposé invalide");
            }
        }

        // Snapshot du statut d'origine pour pouvoir le restaurer en cas de rejet
        tache.setPreviousStatus(tache.getStatus());
        tache.setProposedStatus(proposedStatus);
        tache.setProposedPlannedDate(hasDateChange ? request.getProposedPlannedDate() : null);
        tache.setRejectionReason(null);
        tache.setStatus(StatutTache.EN_ATTENTE_VALIDATION);

        tache = tacheRepository.save(tache);

        journalService.logAction(
            TypeAction.SOUMISSION,
            "TACHE",
            tache.getId(),
            "Soumission d'une demande de validation pour la tâche: " + tache.getTitle(),
            null
        );

        log.info("Demande de validation soumise avec succès pour la tâche ID: {}", id);
        return tacheMapper.toResponse(tache);
    }

    @Override
    @Transactional
    public TacheResponse validerTache(Long id) {
        log.info("Validation de la tâche ID: {}", id);

        Tache tache = tacheRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tâche", id));

        if (tache.getStatus() != StatutTache.EN_ATTENTE_VALIDATION) {
            throw new BadRequestException("Cette tâche n'a pas de demande de validation en attente");
        }

        StatutTache finalStatus = tache.getProposedStatus() != null
            ? tache.getProposedStatus()
            : tache.getPreviousStatus();

        tache.setStatus(finalStatus);

        if (tache.getProposedPlannedDate() != null) {
            tache.setPlannedDate(tache.getProposedPlannedDate());
        }

        if (finalStatus == StatutTache.TERMINEE && tache.getCompletedDate() == null) {
            tache.setCompletedDate(LocalDateTime.now());
        }

        tache.setPreviousStatus(null);
        tache.setProposedStatus(null);
        tache.setProposedPlannedDate(null);
        tache.setRejectionReason(null);

        tache = tacheRepository.save(tache);

        journalService.logAction(
            TypeAction.VALIDATION,
            "TACHE",
            tache.getId(),
            "Validation de la tâche: " + tache.getTitle() + " -> " + finalStatus,
            null
        );

        log.info("Tâche validée avec succès ID: {}", id);
        return tacheMapper.toResponse(tache);
    }

    @Override
    @Transactional
    public TacheResponse rejeterTache(Long id, TacheRejetRequest request) {
        log.info("Rejet de la demande de validation pour la tâche ID: {}", id);

        Tache tache = tacheRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tâche", id));

        if (tache.getStatus() != StatutTache.EN_ATTENTE_VALIDATION) {
            throw new BadRequestException("Cette tâche n'a pas de demande de validation en attente");
        }

        StatutTache restoredStatus = tache.getPreviousStatus() != null
            ? tache.getPreviousStatus()
            : StatutTache.PLANIFIEE;

        tache.setStatus(restoredStatus);
        tache.setPreviousStatus(null);
        tache.setProposedStatus(null);
        tache.setProposedPlannedDate(null);
        tache.setRejectionReason(request != null ? request.getMotif() : null);

        tache = tacheRepository.save(tache);

        journalService.logAction(
            TypeAction.REJET,
            "TACHE",
            tache.getId(),
            "Rejet de la demande de validation pour la tâche: " + tache.getTitle()
                + (tache.getRejectionReason() != null ? " — Motif: " + tache.getRejectionReason() : ""),
            null
        );

        log.info("Demande de validation rejetée avec succès pour la tâche ID: {}", id);
        return tacheMapper.toResponse(tache);
    }

    @Override
    @Transactional
    public TacheDetailResponse affecterOuvrierTache(Long tacheId, Long ouvrierId) {
        log.info("Affectation de l'ouvrier {} à la tâche {}", ouvrierId, tacheId);

        Tache tache = tacheRepository.findById(tacheId)
            .orElseThrow(() -> new ResourceNotFoundException("Tâche", tacheId));

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

        if (!travauxRepository.existsById(travauxId)) {
            throw new ResourceNotFoundException("Travaux", travauxId);
        }

        Page<Tache> taches = tacheRepository.findByTravauxId(travauxId, pageable);
        return taches.map(tacheMapper::toResponse);
    }

    // =========================================================
    // UTILITAIRES
    // =========================================================

    private StatutTache parseStatut(String status) {
        try {
            return StatutTache.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Statut invalide: " + status);
        }
    }
}