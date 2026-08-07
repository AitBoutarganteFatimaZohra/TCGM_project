package com.tcgm.controller;

import com.tcgm.dto.request.SiteCreateRequest;
import com.tcgm.dto.request.SiteUpdateRequest;
import com.tcgm.dto.response.SiteResponse;
import com.tcgm.dto.response.SiteDetailResponse;
import com.tcgm.service.SiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sites")
@RequiredArgsConstructor
public class SiteController {

    private final SiteService siteService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET')")
    public ResponseEntity<SiteResponse> createSite(@Valid @RequestBody SiteCreateRequest request) {
        return new ResponseEntity<>(siteService.createSite(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER', 'MAGASINIER', 'AGENT_SAISIE')")
    public ResponseEntity<SiteDetailResponse> getSiteById(@PathVariable Long id) {
        return ResponseEntity.ok(siteService.getSiteById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER', 'MAGASINIER', 'AGENT_SAISIE')")
    public ResponseEntity<Page<SiteResponse>> getAllSites(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(siteService.getAllSites(status, clientId, search, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET')")
    public ResponseEntity<SiteResponse> updateSite(@PathVariable Long id, 
                                                    @Valid @RequestBody SiteUpdateRequest request) {
        return ResponseEntity.ok(siteService.updateSite(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSite(@PathVariable Long id) {
        siteService.deleteSite(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET')")
    public ResponseEntity<SiteResponse> updateSiteStatus(@PathVariable Long id, 
                                                          @RequestParam String status) {
        return ResponseEntity.ok(siteService.updateSiteStatus(id, status));
    }

    @GetMapping("/my-sites")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<SiteResponse>> getMySites(Pageable pageable) {
        return ResponseEntity.ok(siteService.getMySites(pageable));
    }

    @GetMapping("/statistiques")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET')")
    public ResponseEntity<?> getSiteStatistiques() {
        return ResponseEntity.ok(siteService.getGlobalStatistiques());
    }
}