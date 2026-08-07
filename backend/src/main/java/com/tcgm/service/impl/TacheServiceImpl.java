package com.tcgm.service.impl;

import com.tcgm.dto.request.TacheCreateRequest;
import com.tcgm.dto.request.TacheUpdateRequest;
import com.tcgm.dto.response.TacheResponse;
import com.tcgm.dto.response.TacheDetailResponse;
import com.tcgm.exception.BadRequestException;
import com.tcgm.exception.ResourceNotFoundException;
import com.tcgm.mapper.TacheMapper;
import com.tcgm.model.Tache;
import com.tcgm.model.Site;
import com.tcgm.model.Ouvrier;
import com.tcgm.model.AffectationOuvrierTache;
import com.tcgm.model.enums.StatutTache;
import com.tcgm.repository.TacheRepository;
import com.tcgm.repository.SiteRepository;
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
    private final SiteRepository siteRepository;
    private final OuvrierRepository ouvrierRepository;
    private final AffectationOuvrierTacheRepository affectationRepository;
    private final TacheMapper tacheMapper;
    private final JournalService journalService;

    @Override
    @Transactional
    public TacheResponse createTache(TacheCreateRequest request) {
        log.info("Création d'une nouvelle tâche: {}", request.getTitle());

        // Vérifier que le site existe
        Site site = siteRepository.findById(request.getSiteId())
            .orElseThrow(() -> new ResourceNotFoundException("Site", request.getSiteId()));

        Tache tache = tacheMapper.toEntity(request);
        tache.setSite(site);
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
    public Page<TacheResponse> getAllTaches(Long siteId, String status, String search, Pageable pageable) {
        log.debug("Récupération de toutes les tâches");

        StatutTache statut = null;
        if (status != null) {
            try {
                statut = StatutTache.valueOf(status);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Statut invalide: " + status);
            }
        }

        Page<Tache> taches = tacheRepository.findTachesWithFilters(siteId, statut, search, pageable);
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

    @Override
    @Transactional
    public TacheResponse updateTacheStatus(Long id, String status) {
        log.info("Mise à jour du statut de la tâche ID: {} vers {}", id, status);

        Tache tache = tacheRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tâche", id));

        StatutTache newStatus;
        try {
            newStatus = StatutTache.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Statut invalide: " + status);
        }

        tache.setStatus(newStatus);
        
        // Si la tâche est terminée, mettre à jour la date de completion
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

        Ouvrier ouvrier = ouvrierRepository.findById(ouvrierId)
            .orElseThrow(() -> new ResourceNotFoundException("Ouvrier", ouvrierId));

        // Vérifier si l'ouvrier est déjà affecté à cette tâche
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
    public Page<TacheResponse> getTachesBySite(Long siteId, Pageable pageable) {
        log.debug("Récupération des tâches du site ID: {}", siteId);

        if (!siteRepository.existsById(siteId)) {
            throw new ResourceNotFoundException("Site", siteId);
        }

        Page<Tache> taches = tacheRepository.findBySiteId(siteId, pageable);
        return taches.map(tacheMapper::toResponse);
    }
}