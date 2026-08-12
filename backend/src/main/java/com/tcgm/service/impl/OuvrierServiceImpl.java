package com.tcgm.service.impl;

import com.tcgm.dto.request.OuvrierCreateRequest;
import com.tcgm.dto.request.OuvrierUpdateRequest;
import com.tcgm.dto.request.AffectationSiteRequest;
import com.tcgm.dto.response.OuvrierResponse;
import com.tcgm.exception.BadRequestException;
import com.tcgm.exception.ResourceNotFoundException;
import com.tcgm.mapper.OuvrierMapper;
import com.tcgm.model.Ouvrier;
import com.tcgm.model.Site;
import com.tcgm.model.Affectation;  // ← NOUVEAU (remplace AffectationOuvrierSite)
import com.tcgm.model.enums.StatutAffectation;  // ← NOUVEAU
import com.tcgm.repository.OuvrierRepository;
import com.tcgm.repository.SiteRepository;
import com.tcgm.repository.AffectationRepository;  // ← NOUVEAU (remplace AffectationOuvrierSiteRepository)
import com.tcgm.service.OuvrierService;
import com.tcgm.service.JournalService;
import com.tcgm.model.enums.TypeAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class OuvrierServiceImpl implements OuvrierService {

    private final OuvrierRepository ouvrierRepository;
    private final SiteRepository siteRepository;
    private final AffectationRepository affectationRepository;  // ← MODIFIÉ
    private final OuvrierMapper ouvrierMapper;
    private final JournalService journalService;

    @Override
    @Transactional
    public OuvrierResponse createOuvrier(OuvrierCreateRequest request) {
        log.info("Création d'un nouvel ouvrier: {} {}", request.getFirstName(), request.getLastName());

        // Vérifier si le CIN existe déjà
        if (ouvrierRepository.existsByCin(request.getCin())) {
            throw new BadRequestException("Un ouvrier avec ce CIN existe déjà");
        }

        Ouvrier ouvrier = ouvrierMapper.toEntity(request);
        ouvrier = ouvrierRepository.save(ouvrier);

        journalService.logAction(
            TypeAction.CREATION,
            "OUVRIER",
            ouvrier.getId(),
            "Création de l'ouvrier: " + ouvrier.getFirstName() + " " + ouvrier.getLastName(),
            null
        );

        log.info("Ouvrier créé avec succès: {} {}", ouvrier.getFirstName(), ouvrier.getLastName());
        return ouvrierMapper.toResponse(ouvrier);
    }

    @Override
    @Transactional
    public OuvrierResponse updateOuvrier(Long id, OuvrierUpdateRequest request) {
        log.info("Mise à jour de l'ouvrier ID: {}", id);

        Ouvrier ouvrier = ouvrierRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ouvrier", id));

        ouvrierMapper.updateEntity(ouvrier, request);
        ouvrier = ouvrierRepository.save(ouvrier);

        journalService.logAction(
            TypeAction.MODIFICATION,
            "OUVRIER",
            ouvrier.getId(),
            "Mise à jour de l'ouvrier: " + ouvrier.getFirstName() + " " + ouvrier.getLastName(),
            null
        );

        log.info("Ouvrier mis à jour avec succès: {}", ouvrier.getFirstName());
        return ouvrierMapper.toResponse(ouvrier);
    }

    @Override
    public OuvrierResponse getOuvrierById(Long id) {
        log.debug("Récupération de l'ouvrier ID: {}", id);
        Ouvrier ouvrier = ouvrierRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ouvrier", id));
        return ouvrierMapper.toResponse(ouvrier);
    }

    @Override
    public Page<OuvrierResponse> getAllOuvriers(Long chantierId, String specialite, Boolean active, String search, Pageable pageable) {
        log.debug("Récupération de tous les ouvriers");

        Page<Ouvrier> ouvriers;
        if (chantierId != null) {
            if (search != null && !search.isEmpty() || specialite != null) {
                // MODIFIÉ : findOuvriersBySiteWithFilters → findOuvriersByChantierWithFilters
                ouvriers = ouvrierRepository.findOuvriersByChantierWithFilters(chantierId, specialite, search, pageable);
            } else {
                // MODIFIÉ : findOuvriersBySite → findOuvriersByChantier
                ouvriers = ouvrierRepository.findOuvriersByChantier(chantierId, pageable);
            }
        } else {
            ouvriers = ouvrierRepository.findOuvriersWithFilters(specialite, active, search, pageable);
        }

        return ouvriers.map(ouvrierMapper::toResponse);
    }

    @Override
    @Transactional
    public void deleteOuvrier(Long id) {
        log.info("Suppression de l'ouvrier ID: {}", id);

        Ouvrier ouvrier = ouvrierRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ouvrier", id));

        journalService.logAction(
            TypeAction.SUPPRESSION,
            "OUVRIER",
            ouvrier.getId(),
            "Suppression de l'ouvrier: " + ouvrier.getFirstName() + " " + ouvrier.getLastName(),
            null
        );

        ouvrierRepository.delete(ouvrier);
        log.info("Ouvrier supprimé avec succès");
    }

    @Override
    @Transactional
    public OuvrierResponse affecterOuvrierSite(AffectationSiteRequest request) {
        log.info("Affectation de l'ouvrier {} au site {}", request.getOuvrierId(), request.getSiteId());

        // Vérifier que l'ouvrier existe
        Ouvrier ouvrier = ouvrierRepository.findById(request.getOuvrierId())
            .orElseThrow(() -> new ResourceNotFoundException("Ouvrier", request.getOuvrierId()));

        // Vérifier que le site existe
        Site site = siteRepository.findById(request.getSiteId())
            .orElseThrow(() -> new ResourceNotFoundException("Site", request.getSiteId()));

        // MODIFIÉ : Vérifier si l'ouvrier a déjà une affectation en cours
        if (affectationRepository.hasAffectationEnCours(request.getOuvrierId())) {
            throw new BadRequestException("Cet ouvrier a déjà une affectation en cours");
        }

        // MODIFIÉ : Créer une Affectation (au lieu de AffectationOuvrierSite)
        Affectation affectation = Affectation.builder()
            .ouvrier(ouvrier)
            .chantier(site)
            .dateDebut(request.getStartDate() != null ? LocalDate.parse(request.getStartDate()) : LocalDate.now())
            .dateFin(request.getEndDate() != null ? LocalDate.parse(request.getEndDate()) : null)
            .statut(StatutAffectation.EN_COURS)
            .build();

        affectation = affectationRepository.save(affectation);

        journalService.logAction(
            TypeAction.AFFECTATION,
            "OUVRIER",
            ouvrier.getId(),
            "Affectation de l'ouvrier " + ouvrier.getFirstName() + " au site " + site.getName(),
            null
        );

        log.info("Ouvrier affecté avec succès au site");
        return ouvrierMapper.toResponse(ouvrier);
    }

    @Override
    @Transactional
    public void desaffecterOuvrierSite(Long affectationId) {
        log.info("Désaffectation de l'ouvrier du site (ID affectation: {})", affectationId);

        // MODIFIÉ : Utiliser Affectation au lieu de AffectationOuvrierSite
        Affectation affectation = affectationRepository.findById(affectationId)
            .orElseThrow(() -> new ResourceNotFoundException("Affectation", affectationId));

        // MODIFIÉ : Utiliser le statut au lieu de active
        affectation.setStatut(StatutAffectation.TERMINEE);
        affectation.setDateFin(LocalDate.now());
        affectationRepository.save(affectation);

        journalService.logAction(
            TypeAction.DESAFFECTATION,
            "OUVRIER",
            affectation.getOuvrier().getId(),
            "Désaffectation de l'ouvrier " + affectation.getOuvrier().getFirstName() + 
            " du site " + affectation.getChantier().getName(),
            null
        );

        log.info("Ouvrier désaffecté avec succès");
    }

    @Override
    public Page<OuvrierResponse> getOuvriersByChantier(Long chantierId, Pageable pageable) {
        log.debug("Récupération des ouvriers du chantier ID: {}", chantierId);

        // Vérifier que le chantier existe
        if (!siteRepository.existsById(chantierId)) {
            throw new ResourceNotFoundException("Chantier", chantierId);
        }

        // MODIFIÉ : findOuvriersBySite → findOuvriersByChantier
        Page<Ouvrier> ouvriers = ouvrierRepository.findOuvriersByChantier(chantierId, pageable);
        return ouvriers.map(ouvrierMapper::toResponse);
    }
}