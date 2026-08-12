package com.tcgm.service;

import com.tcgm.dto.request.TravauxRequest;
import com.tcgm.dto.response.TravauxResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TravauxService {
    TravauxResponse createTravaux(TravauxRequest request);
    TravauxResponse updateTravaux(Long id, TravauxRequest request);
    TravauxResponse getTravauxById(Long id);
    Page<TravauxResponse> getAllTravaux(Long chantierId, String statut, Pageable pageable);
    void deleteTravaux(Long id);
    TravauxResponse updateStatut(Long id, String statut);
    Page<TravauxResponse> getTravauxByChantier(Long chantierId, Pageable pageable);
}