package com.tcgm.service.impl;

import com.tcgm.dto.request.TravauxRequest;
import com.tcgm.dto.response.TravauxResponse;
import com.tcgm.exception.BadRequestException;
import com.tcgm.exception.ResourceNotFoundException;
import com.tcgm.mapper.TravauxMapper;
import com.tcgm.model.Travaux;
import com.tcgm.model.Site;
import com.tcgm.model.enums.StatutTravaux;
import com.tcgm.repository.TravauxRepository;
import com.tcgm.repository.SiteRepository;
import com.tcgm.service.TravauxService;
import com.tcgm.service.JournalService;
import com.tcgm.model.enums.TypeAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TravauxServiceImpl implements TravauxService {

    private final TravauxRepository travauxRepository;
    private final SiteRepository siteRepository;
    private final TravauxMapper travauxMapper;
    private final JournalService journalService;

    @Override
    @Transactional
    public TravauxResponse createTravaux(TravauxRequest request) {
        log.info("Création de travaux: {}", request.getCode());

        // Vérifier si le code existe déjà
        if (travauxRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Un travaux avec ce code existe déjà");
        }

        // Vérifier que le chantier existe
        Site chantier = siteRepository.findById(request.getChantierId())
            .orElseThrow(() -> new ResourceNotFoundException("Chantier", request.getChantierId()));

        Travaux travaux = travauxMapper.toEntity(request);
        travaux.setChantier(chantier);
        
        // Définir le statut par défaut si non spécifié
        if (travaux.getStatut() == null) {
            travaux.setStatut(StatutTravaux.PLANIFIE);
        }

        travaux = travauxRepository.save(travaux);

        journalService.logAction(
            TypeAction.CREATION,
            "TRAVAUX",
            travaux.getId(),
            "Création des travaux: " + travaux.getCode(),
            null
        );

        log.info("Travaux créé avec succès: {}", travaux.getCode());
        return travauxMapper.toResponse(travaux);
    }

    @Override
    @Transactional
    public TravauxResponse updateTravaux(Long id, TravauxRequest request) {
        log.info("Mise à jour des travaux ID: {}", id);

        Travaux travaux = travauxRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Travaux", id));

        // Vérifier si le code n'est pas déjà pris par un autre
        if (!travaux.getCode().equals(request.getCode()) && 
            travauxRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Un travaux avec ce code existe déjà");
        }

        // Mettre à jour le chantier si changé
        if (!travaux.getChantier().getId().equals(request.getChantierId())) {
            Site chantier = siteRepository.findById(request.getChantierId())
                .orElseThrow(() -> new ResourceNotFoundException("Chantier", request.getChantierId()));
            travaux.setChantier(chantier);
        }

        travauxMapper.updateEntity(travaux, request);
        travaux = travauxRepository.save(travaux);

        journalService.logAction(
            TypeAction.MODIFICATION,
            "TRAVAUX",
            travaux.getId(),
            "Mise à jour des travaux: " + travaux.getCode(),
            null
        );

        log.info("Travaux mis à jour avec succès: {}", travaux.getCode());
        return travauxMapper.toResponse(travaux);
    }

    @Override
    public TravauxResponse getTravauxById(Long id) {
        log.debug("Récupération des travaux ID: {}", id);
        Travaux travaux = travauxRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Travaux", id));
        return travauxMapper.toResponse(travaux);
    }

    @Override
    public Page<TravauxResponse> getAllTravaux(Long chantierId, String statut, Pageable pageable) {
        log.debug("Récupération de tous les travaux");

        StatutTravaux statutEnum = null;
        if (statut != null) {
            try {
                statutEnum = StatutTravaux.valueOf(statut);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Statut invalide: " + statut);
            }
        }

        Page<Travaux> travaux;
        if (chantierId != null && statutEnum != null) {
            travaux = travauxRepository.findByChantierIdAndStatut(chantierId, statutEnum, pageable);
        } else if (chantierId != null) {
            travaux = travauxRepository.findByChantierId(chantierId, pageable);
        } else if (statutEnum != null) {
            travaux = travauxRepository.findByStatut(statutEnum, pageable);
        } else {
            travaux = travauxRepository.findAll(pageable);
        }

        return travaux.map(travauxMapper::toResponse);
    }

    @Override
    @Transactional
    public void deleteTravaux(Long id) {
        log.info("Suppression des travaux ID: {}", id);

        Travaux travaux = travauxRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Travaux", id));

        journalService.logAction(
            TypeAction.SUPPRESSION,
            "TRAVAUX",
            travaux.getId(),
            "Suppression des travaux: " + travaux.getCode(),
            null
        );

        travauxRepository.delete(travaux);
        log.info("Travaux supprimé avec succès: {}", travaux.getCode());
    }

    @Override
    @Transactional
    public TravauxResponse updateStatut(Long id, String statut) {
        log.info("Mise à jour du statut des travaux ID: {} vers {}", id, statut);

        Travaux travaux = travauxRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Travaux", id));

        StatutTravaux newStatut;
        try {
            newStatut = StatutTravaux.valueOf(statut);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Statut invalide: " + statut);
        }

        travaux.setStatut(newStatut);
        travaux = travauxRepository.save(travaux);

        journalService.logAction(
            TypeAction.MODIFICATION,
            "TRAVAUX",
            travaux.getId(),
            "Changement de statut des travaux: " + travaux.getCode() + " -> " + statut,
            null
        );

        log.info("Statut des travaux mis à jour avec succès");
        return travauxMapper.toResponse(travaux);
    }

    @Override
    public Page<TravauxResponse> getTravauxByChantier(Long chantierId, Pageable pageable) {
        log.debug("Récupération des travaux du chantier ID: {}", chantierId);

        if (!siteRepository.existsById(chantierId)) {
            throw new ResourceNotFoundException("Chantier", chantierId);
        }

        Page<Travaux> travaux = travauxRepository.findByChantierId(chantierId, pageable);
        return travaux.map(travauxMapper::toResponse);
    }
}