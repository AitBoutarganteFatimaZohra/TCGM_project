package com.tcgm.service.impl;

import com.tcgm.dto.request.AffectationRequest;
import com.tcgm.dto.response.AffectationResponse;
import com.tcgm.model.Affectation;
import com.tcgm.model.Ouvrier;
import com.tcgm.model.Site;
import com.tcgm.model.enums.StatutAffectation;
import com.tcgm.repository.AffectationRepository;
import com.tcgm.repository.OuvrierRepository;
import com.tcgm.repository.SiteRepository;
import com.tcgm.service.AffectationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AffectationServiceImpl implements AffectationService {

private final AffectationRepository affectationRepository;
private final OuvrierRepository ouvrierRepository;
private final SiteRepository siteRepository;

// =========================================================
// CREATE
// =========================================================

@Override
public AffectationResponse createAffectation(AffectationRequest request) {

    Site chantier = siteRepository.findById(request.getChantierId())
            .orElseThrow(() ->
                    new RuntimeException("Chantier introuvable avec l'id : " + request.getChantierId()));

    Ouvrier ouvrier = ouvrierRepository.findById(request.getOuvrierId())
            .orElseThrow(() ->
                    new RuntimeException("Ouvrier introuvable avec l'id : " + request.getOuvrierId()));

    // Vérifier qu'il n'existe pas déjà une affectation en cours
    if (affectationRepository.hasAffectationEnCours(ouvrier.getId())) {
        throw new IllegalStateException(
                "Cet ouvrier possède déjà une affectation en cours."
        );
    }

    Affectation affectation = new Affectation();

    affectation.setDateDebut(request.getDateDebut());
    affectation.setDateFin(request.getDateFin());

    if (request.getStatut() != null && !request.getStatut().isBlank()) {
        affectation.setStatut(
                StatutAffectation.valueOf(request.getStatut().toUpperCase())
        );
    } else {
        affectation.setStatut(StatutAffectation.EN_COURS);
    }

    affectation.setChantier(chantier);
    affectation.setOuvrier(ouvrier);

    Affectation saved = affectationRepository.save(affectation);

    return toResponse(saved);
}

// =========================================================
// UPDATE
// =========================================================

@Override
public AffectationResponse updateAffectation(
        Long id,
        AffectationRequest request) {

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
        affectation.setStatut(
                StatutAffectation.valueOf(request.getStatut().toUpperCase())
        );
    }

    Affectation updated = affectationRepository.save(affectation);

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
public Page<AffectationResponse> getAllAffectations(
        Long chantierId,
        Long ouvrierId,
        String statut,
        Pageable pageable) {

    List<Affectation> affectations = affectationRepository.findAll();

    // Filtre chantier
    if (chantierId != null) {
        affectations = affectations.stream()
                .filter(a -> a.getChantier() != null
                        && a.getChantier().getId().equals(chantierId))
                .toList();
    }

    // Filtre ouvrier
    if (ouvrierId != null) {
        affectations = affectations.stream()
                .filter(a -> a.getOuvrier() != null
                        && a.getOuvrier().getId().equals(ouvrierId))
                .toList();
    }

    // Filtre statut
    if (statut != null && !statut.isBlank()) {

        StatutAffectation statutEnum =
                StatutAffectation.valueOf(statut.toUpperCase());

        affectations = affectations.stream()
                .filter(a -> a.getStatut() == statutEnum)
                .toList();
    }

    // Pagination
    int start = (int) pageable.getOffset();

    int end = Math.min(
            start + pageable.getPageSize(),
            affectations.size()
    );

    List<Affectation> pageContent;

    if (start >= affectations.size()) {
        pageContent = List.of();
    } else {
        pageContent = affectations.subList(start, end);
    }

    List<AffectationResponse> responses = pageContent.stream()
            .map(this::toResponse)
            .toList();

    return new PageImpl<>(
            responses,
            pageable,
            affectations.size()
    );
}

// =========================================================
// DELETE
// =========================================================

@Override
public void deleteAffectation(Long id) {

    Affectation affectation = affectationRepository.findById(id)
            .orElseThrow(() ->
                    new RuntimeException("Affectation introuvable avec l'id : " + id));

    affectationRepository.delete(affectation);
}

// =========================================================
// UPDATE STATUT
// =========================================================

@Override
public AffectationResponse updateStatut(Long id, String statut) {

    Affectation affectation = affectationRepository.findById(id)
            .orElseThrow(() ->
                    new RuntimeException("Affectation introuvable avec l'id : " + id));

    if (statut == null || statut.isBlank()) {
        throw new IllegalArgumentException(
                "Le statut est obligatoire."
        );
    }

    StatutAffectation nouveauStatut;

    try {
        nouveauStatut =
                StatutAffectation.valueOf(statut.toUpperCase());
    } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(
                "Statut d'affectation invalide : " + statut
        );
    }

    affectation.setStatut(nouveauStatut);

    // Si l'affectation est terminée, on peut automatiquement
    // renseigner la date de fin si elle n'existe pas.
    if (nouveauStatut != StatutAffectation.EN_COURS
            && affectation.getDateFin() == null) {

        affectation.setDateFin(LocalDate.now());
    }

    Affectation updated = affectationRepository.save(affectation);

    return toResponse(updated);
}

// =========================================================
// AFFECTATION EN COURS D'UN OUVRIER
// =========================================================

@Override
@Transactional(readOnly = true)
public AffectationResponse getAffectationEnCoursByOuvrier(Long ouvrierId) {

    Affectation affectation =
            affectationRepository.findAffectationEnCoursByOuvrier(ouvrierId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Aucune affectation en cours pour l'ouvrier : "
                                            + ouvrierId
                            ));

    return toResponse(affectation);
}

// =========================================================
// AFFECTATIONS PAR CHANTIER
// =========================================================

@Override
@Transactional(readOnly = true)
public Page<AffectationResponse> getAffectationsByChantier(
        Long chantierId,
        Pageable pageable) {

    Page<Affectation> page =
            affectationRepository.findByChantierId(
                    chantierId,
                    pageable
            );

    return page.map(this::toResponse);
}

// =========================================================
// AFFECTATIONS PAR OUVRIER
// =========================================================

@Override
@Transactional(readOnly = true)
public Page<AffectationResponse> getAffectationsByOuvrier(
        Long ouvrierId,
        Pageable pageable) {

    Page<Affectation> page =
            affectationRepository.findByOuvrierId(
                    ouvrierId,
                    pageable
            );

    return page.map(this::toResponse);
}

// =========================================================
// MAPPING ENTITY -> RESPONSE
// =========================================================

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
            .chantier(chantierBrief)
            .ouvrier(ouvrierBrief)
            .createdAt(affectation.getCreatedAt())
            .updatedAt(affectation.getUpdatedAt())
            .build();
}


}
