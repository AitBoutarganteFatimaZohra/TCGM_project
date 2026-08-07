package com.tcgm.service;

import com.tcgm.dto.request.TacheCreateRequest;
import com.tcgm.dto.request.TacheUpdateRequest;
import com.tcgm.dto.response.TacheResponse;
import com.tcgm.dto.response.TacheDetailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TacheService {
    TacheResponse createTache(TacheCreateRequest request);
    TacheResponse updateTache(Long id, TacheUpdateRequest request);
    TacheDetailResponse getTacheById(Long id);
    Page<TacheResponse> getAllTaches(Long siteId, String status, String search, Pageable pageable);
    void deleteTache(Long id);
    TacheResponse updateTacheStatus(Long id, String status);
    TacheDetailResponse affecterOuvrierTache(Long tacheId, Long ouvrierId);
    void retirerOuvrierTache(Long tacheId, Long ouvrierId);
    Page<TacheResponse> getTachesBySite(Long siteId, Pageable pageable);
}