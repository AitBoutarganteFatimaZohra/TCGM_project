package com.tcgm.service;

import com.tcgm.dto.request.OuvrierCreateRequest;
import com.tcgm.dto.request.OuvrierUpdateRequest;
import com.tcgm.dto.request.AffectationSiteRequest;
import com.tcgm.dto.response.OuvrierResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OuvrierService {
    OuvrierResponse createOuvrier(OuvrierCreateRequest request);
    OuvrierResponse updateOuvrier(Long id, OuvrierUpdateRequest request);
    OuvrierResponse getOuvrierById(Long id);
    Page<OuvrierResponse> getAllOuvriers(Long siteId, String specialite, Boolean active, String search, Pageable pageable);
    void deleteOuvrier(Long id);
    OuvrierResponse affecterOuvrierSite(AffectationSiteRequest request);
    void desaffecterOuvrierSite(Long affectationId);
    Page<OuvrierResponse> getOuvriersBySite(Long siteId, Pageable pageable);
}