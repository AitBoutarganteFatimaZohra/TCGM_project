package com.tcgm.service;

import com.tcgm.dto.request.TacheCreateRequest;
import com.tcgm.dto.request.TacheUpdateRequest;
import com.tcgm.dto.response.TacheResponse;
import com.tcgm.dto.response.TacheDetailResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

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

    TacheResponse updateTacheStatus(Long id, String status);

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

    // ⚠️ NOUVEAU — circuit de validation (Chef de Chantier → Chef de Projet)
    TacheResponse proposerModification(Long id, String proposedStatus, LocalDateTime proposedPlannedDate);
    TacheResponse validerModification(Long id);
    TacheResponse rejeterModification(Long id, String motif);
}