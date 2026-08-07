package com.tcgm.service;

import com.tcgm.dto.request.DossierPointageRequest;
import com.tcgm.dto.request.LignePointageRequest;
import com.tcgm.dto.request.ValidationPointageRequest;
import com.tcgm.dto.response.DossierPointageResponse;
import com.tcgm.dto.response.LignePointageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface PointageService {
    DossierPointageResponse createDossierPointage(DossierPointageRequest request);
    DossierPointageResponse getDossierPointageById(Long id);
    Page<DossierPointageResponse> getAllDossiersPointage(Long siteId, String date, String status, Pageable pageable);
    DossierPointageResponse updateDossierPointage(Long id, DossierPointageRequest request);
    void deleteDossierPointage(Long id);
    LignePointageResponse addLignePointage(Long dossierId, LignePointageRequest request);
    void removeLignePointage(Long ligneId);
    DossierPointageResponse validerDossierPointage(Long id, ValidationPointageRequest request);
    DossierPointageResponse rejeterDossierPointage(Long id, ValidationPointageRequest request);
    DossierPointageResponse getTodayPointage(Long siteId);
    Map<String, Object> getPointageStatistiques(Long siteId);
}