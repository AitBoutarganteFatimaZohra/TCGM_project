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
import com.tcgm.model.AffectationOuvrierSite;
import com.tcgm.repository.OuvrierRepository;
import com.tcgm.repository.SiteRepository;
import com.tcgm.repository.AffectationOuvrierSiteRepository;
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
    private final AffectationOuvrierSiteRepository affectationRepository;
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
    public Page<OuvrierResponse> getAllOuvriers(Long siteId, String specialite, Boolean active, String search, Pageable pageable) {
        log.debug("Récupération de tous les ouvriers");

        Page<Ouvrier> ouvriers;
        if (siteId != null) {
            if (search != null && !search.isEmpty() || specialite != null) {
                ouvriers = ouvrierRepository.findOuvriersBySiteWithFilters(siteId, specialite, search, pageable);
            } else {
                ouvriers = ouvrierRepository.findOuvriersBySite(siteId, pageable);
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

        // Vérifier si l'ouvrier est déjà affecté à ce site
        if (affectationRepository.isOuvrierAffectedToSite(request.getOuvrierId(), request.getSiteId())) {
            throw new BadRequestException("Cet ouvrier est déjà affecté à ce site");
        }

        // Créer l'affectation
        AffectationOuvrierSite affectation = AffectationOuvrierSite.builder()
            .ouvrier(ouvrier)
            .site(site)
            .startDate(request.getStartDate() != null ? LocalDate.parse(request.getStartDate()) : LocalDate.now())
            .endDate(request.getEndDate() != null ? LocalDate.parse(request.getEndDate()) : null)
            .active(true)
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

        AffectationOuvrierSite affectation = affectationRepository.findById(affectationId)
            .orElseThrow(() -> new ResourceNotFoundException("Affectation", affectationId));

        affectation.setActive(false);
        affectation.setEndDate(LocalDate.now());
        affectationRepository.save(affectation);

        journalService.logAction(
            TypeAction.DESAFFECTATION,
            "OUVRIER",
            affectation.getOuvrier().getId(),
            "Désaffectation de l'ouvrier " + affectation.getOuvrier().getFirstName() + 
            " du site " + affectation.getSite().getName(),
            null
        );

        log.info("Ouvrier désaffecté avec succès");
    }

    @Override
    public Page<OuvrierResponse> getOuvriersBySite(Long siteId, Pageable pageable) {
        log.debug("Récupération des ouvriers du site ID: {}", siteId);

        // Vérifier que le site existe
        if (!siteRepository.existsById(siteId)) {
            throw new ResourceNotFoundException("Site", siteId);
        }

        Page<Ouvrier> ouvriers = ouvrierRepository.findOuvriersBySite(siteId, pageable);
        return ouvriers.map(ouvrierMapper::toResponse);
    }
}