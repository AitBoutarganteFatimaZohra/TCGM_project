package com.tcgm.service;

import com.tcgm.dto.request.AffectationRequest;
import com.tcgm.dto.response.AffectationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AffectationService {
    AffectationResponse createAffectation(AffectationRequest request);
    AffectationResponse updateAffectation(Long id, AffectationRequest request);
    AffectationResponse getAffectationById(Long id);
    Page<AffectationResponse> getAllAffectations(Long chantierId, Long ouvrierId, String statut, Pageable pageable);
    void deleteAffectation(Long id);
    AffectationResponse updateStatut(Long id, String statut);
    AffectationResponse getAffectationEnCoursByOuvrier(Long ouvrierId);
    Page<AffectationResponse> getAffectationsByChantier(Long chantierId, Pageable pageable);
    Page<AffectationResponse> getAffectationsByOuvrier(Long ouvrierId, Pageable pageable);
}