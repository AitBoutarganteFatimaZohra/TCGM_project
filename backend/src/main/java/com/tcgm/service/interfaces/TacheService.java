package com.tcgm.service;

import com.tcgm.dto.request.TacheCreateRequest;
import com.tcgm.dto.request.TacheUpdateRequest;
import com.tcgm.dto.request.TacheSoumissionRequest;
import com.tcgm.dto.request.TacheRejetRequest;
import com.tcgm.dto.response.TacheResponse;
import com.tcgm.dto.response.TacheDetailResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TacheService {

    TacheResponse createTache(TacheCreateRequest request);

    TacheResponse updateTache(Long id, TacheUpdateRequest request);

    TacheDetailResponse getTacheById(Long id);

    Page<TacheResponse> getAllTaches(
        Long travauxId,
        String status,
        String search,
        Pageable pageable
    );

    void deleteTache(Long id);

    /**
     * Changement de statut direct, sans passer par le circuit de
     * validation. Réservé à l'Administrateur (override).
     */
    TacheResponse updateTacheStatus(Long id, String status);

    /**
     * Étape 1 du circuit : le Chef de Chantier soumet un changement de
     * statut et/ou de date prévue. La tâche passe en EN_ATTENTE_VALIDATION.
     */
    TacheResponse soumettreTache(Long id, TacheSoumissionRequest request);

    /**
     * Étape 2a du circuit : le Chef de Projet valide la demande en attente.
     * Le statut/la date proposés sont appliqués.
     */
    TacheResponse validerTache(Long id);

    /**
     * Étape 2b du circuit : le Chef de Projet rejette la demande en
     * attente. La tâche retrouve son statut d'origine.
     */
    TacheResponse rejeterTache(Long id, TacheRejetRequest request);

    TacheDetailResponse affecterOuvrierTache(
        Long tacheId,
        Long ouvrierId
    );

    void retirerOuvrierTache(
        Long tacheId,
        Long ouvrierId
    );

    Page<TacheResponse> getTachesByTravaux(
        Long travauxId,
        Pageable pageable
    );
}