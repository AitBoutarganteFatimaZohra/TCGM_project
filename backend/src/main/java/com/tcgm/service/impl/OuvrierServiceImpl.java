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
import com.tcgm.model.Affectation;
import com.tcgm.model.enums.StatutAffectation;
import com.tcgm.repository.OuvrierRepository;
import com.tcgm.repository.SiteRepository;
import com.tcgm.repository.AffectationRepository;
import com.tcgm.security.SecurityUtils;
import com.tcgm.service.OuvrierService;
import com.tcgm.service.JournalService;
import com.tcgm.model.enums.TypeAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OuvrierServiceImpl implements OuvrierService {

    private final OuvrierRepository ouvrierRepository;
    private final SiteRepository siteRepository;
    private final AffectationRepository affectationRepository;
    private final OuvrierMapper ouvrierMapper;
    private final JournalService journalService;
    private final SecurityUtils securityUtils;

    // =========================================================
    // CREATE
    // =========================================================

    @Override
    @Transactional
    public OuvrierResponse createOuvrier(OuvrierCreateRequest request) {
        log.info("Création d'un nouvel ouvrier: {} {}", request.getFirstName(), request.getLastName());

        // Création de la fiche ouvrier : pas de notion de chantier à ce stade
        // (l'affectation à un chantier se fait via affecterOuvrierSite),
        // donc pas de scoping nécessaire ici.
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

    // =========================================================
    // UPDATE
    // =========================================================

    @Override
    @Transactional
    public OuvrierResponse updateOuvrier(Long id, OuvrierUpdateRequest request) {
        log.info("Mise à jour de l'ouvrier ID: {}", id);

        Ouvrier ouvrier = ouvrierRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ouvrier", id));

        checkOuvrierAccessForChefChantier(id);

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

    // =========================================================
    // READ - by ID
    // =========================================================

    @Override
    public OuvrierResponse getOuvrierById(Long id) {
        log.debug("Récupération de l'ouvrier ID: {}", id);
        Ouvrier ouvrier = ouvrierRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ouvrier", id));

        checkOuvrierAccessForChefChantier(id);

        return ouvrierMapper.toResponse(ouvrier);
    }

    // =========================================================
    // READ - liste
    // =========================================================

    @Override
    public Page<OuvrierResponse> getAllOuvriers(Long chantierId, String specialite, Boolean active, String search, Pageable pageable) {
        log.debug("Récupération de tous les ouvriers");

        if (securityUtils.isChefChantier()) {
            List<Long> chantierIds = securityUtils.getChantierIdsAsChefChantier();

            if (chantierId != null) {
                if (!chantierIds.contains(chantierId)) {
                    throw new AccessDeniedException("Vous n'êtes pas responsable de ce chantier");
                }
                chantierIds = List.of(chantierId);
            }

            if (chantierIds.isEmpty()) {
                return Page.empty(pageable);
            }

            boolean hasFilters = (search != null && !search.isEmpty()) || specialite != null;
            Page<Ouvrier> ouvriers = hasFilters
                    ? ouvrierRepository.findOuvriersByChantierInWithFilters(chantierIds, specialite, search, pageable)
                    : ouvrierRepository.findOuvriersByChantierIn(chantierIds, pageable);

            return ouvriers.map(ouvrierMapper::toResponse);
        }

        // Comportement inchangé pour Admin / Chef de Projet / Magasinier
        Page<Ouvrier> ouvriers;
        if (chantierId != null) {
            if (search != null && !search.isEmpty() || specialite != null) {
                ouvriers = ouvrierRepository.findOuvriersByChantierWithFilters(chantierId, specialite, search, pageable);
            } else {
                ouvriers = ouvrierRepository.findOuvriersByChantier(chantierId, pageable);
            }
        } else {
            ouvriers = ouvrierRepository.findOuvriersWithFilters(specialite, active, search, pageable);
        }

        return ouvriers.map(ouvrierMapper::toResponse);
    }

    // =========================================================
    // DELETE
    // =========================================================

    @Override
    @Transactional
    public void deleteOuvrier(Long id) {
        log.info("Suppression de l'ouvrier ID: {}", id);

        Ouvrier ouvrier = ouvrierRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ouvrier", id));

        checkOuvrierAccessForChefChantier(id);

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

    // =========================================================
    // AFFECTATIONS
    // =========================================================

    @Override
    @Transactional
    public OuvrierResponse affecterOuvrierSite(AffectationSiteRequest request) {
        log.info("Affectation de l'ouvrier {} au site {}", request.getOuvrierId(), request.getSiteId());

        Ouvrier ouvrier = ouvrierRepository.findById(request.getOuvrierId())
            .orElseThrow(() -> new ResourceNotFoundException("Ouvrier", request.getOuvrierId()));

        Site site = siteRepository.findById(request.getSiteId())
            .orElseThrow(() -> new ResourceNotFoundException("Site", request.getSiteId()));

        // Un Chef de Chantier ne peut affecter un ouvrier qu'à SON chantier
        if (securityUtils.isChefChantier()
                && !securityUtils.getChantierIdsAsChefChantier().contains(request.getSiteId())) {
            throw new AccessDeniedException("Vous n'êtes pas responsable de ce chantier");
        }

        if (affectationRepository.hasAffectationEnCours(request.getOuvrierId())) {
            throw new BadRequestException("Cet ouvrier a déjà une affectation en cours");
        }

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

        Affectation affectation = affectationRepository.findById(affectationId)
            .orElseThrow(() -> new ResourceNotFoundException("Affectation", affectationId));

        // Un Chef de Chantier ne peut désaffecter que sur SON chantier
        if (securityUtils.isChefChantier()
                && !securityUtils.getChantierIdsAsChefChantier().contains(affectation.getChantier().getId())) {
            throw new AccessDeniedException("Vous n'êtes pas responsable de ce chantier");
        }

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

        if (!siteRepository.existsById(chantierId)) {
            throw new ResourceNotFoundException("Chantier", chantierId);
        }

        if (securityUtils.isChefChantier()
                && !securityUtils.getChantierIdsAsChefChantier().contains(chantierId)) {
            throw new AccessDeniedException("Vous n'êtes pas responsable de ce chantier");
        }

        Page<Ouvrier> ouvriers = ouvrierRepository.findOuvriersByChantier(chantierId, pageable);
        return ouvriers.map(ouvrierMapper::toResponse);
    }

    // =========================================================
    // HELPER PRIVÉ
    // =========================================================

    /**
     * Pour un Chef de Chantier : vérifie que l'ouvrier ciblé est bien
     * affecté (EN_COURS) à l'un de ses chantiers. Ne fait rien pour
     * les autres rôles (Admin, Chef de Projet, Magasinier).
     */
    private void checkOuvrierAccessForChefChantier(Long ouvrierId) {
        if (!securityUtils.isChefChantier()) {
            return;
        }

        List<Long> chantierIds = securityUtils.getChantierIdsAsChefChantier();

        boolean autorise = chantierIds.stream()
                .anyMatch(chantierId ->
                        !ouvrierRepository.findOuvriersWithAffectationEnCoursByChantier(chantierId).stream()
                                .noneMatch(o -> o.getId().equals(ouvrierId))
                );

        if (!autorise) {
            throw new AccessDeniedException("Cet ouvrier n'est pas affecté à l'un de vos chantiers");
        }
    }
}