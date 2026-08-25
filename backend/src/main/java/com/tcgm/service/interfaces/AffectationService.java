package com.tcgm.service;

import com.tcgm.dto.request.AffectationRejetRequest;
import com.tcgm.dto.request.AffectationRequest;
import com.tcgm.dto.response.AffectationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AffectationService {

    /**
     * @param creatingRole rôle de l'utilisateur qui crée (ADMIN, CHEF_PROJET,
     *                     CHEF_CHANTIER). Si CHEF_CHANTIER : l'affectation est
     *                     créée en EN_ATTENTE_VALIDATION (circuit de validation).
     *                     Si ADMIN ou CHEF_PROJET : création directe (statut
     *                     demandé, ou EN_COURS par défaut).
     */
    AffectationResponse createAffectation(AffectationRequest request, String creatingRole);

    AffectationResponse updateAffectation(Long id, AffectationRequest request);

    AffectationResponse getAffectationById(Long id);

    Page<AffectationResponse> getAllAffectations(Long chantierId, Long ouvrierId, String statut, Pageable pageable);

    void deleteAffectation(Long id);

    /** Override direct réservé à l'Administrateur (contourne le circuit). */
    AffectationResponse updateStatut(Long id, String statut);

    /** Étape 2a du circuit : le Chef de Projet valide. */
    AffectationResponse validerAffectation(Long id, String validatingRole);

    /** Étape 2b du circuit : le Chef de Projet rejette (définitif). */
    AffectationResponse rejeterAffectation(Long id, AffectationRejetRequest request, String validatingRole);

    AffectationResponse getAffectationEnCoursByOuvrier(Long ouvrierId);

    Page<AffectationResponse> getAffectationsByChantier(Long chantierId, Pageable pageable);

    Page<AffectationResponse> getAffectationsByOuvrier(Long ouvrierId, Pageable pageable);
}