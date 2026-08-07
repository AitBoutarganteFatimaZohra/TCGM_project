package com.tcgm.service;

import com.tcgm.dto.request.SiteCreateRequest;
import com.tcgm.dto.request.SiteUpdateRequest;
import com.tcgm.dto.response.SiteResponse;
import com.tcgm.dto.response.SiteDetailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface SiteService {
    SiteResponse createSite(SiteCreateRequest request);
    SiteResponse updateSite(Long id, SiteUpdateRequest request);
    SiteDetailResponse getSiteById(Long id);
    Page<SiteResponse> getAllSites(String status, Long clientId, String search, Pageable pageable);
    void deleteSite(Long id);
    SiteResponse updateSiteStatus(Long id, String status);
    Page<SiteResponse> getMySites(Pageable pageable);
    Map<String, Object> getGlobalStatistiques();
}