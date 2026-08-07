package com.tcgm.controller;

import com.tcgm.dto.request.TacheCreateRequest;
import com.tcgm.dto.request.TacheUpdateRequest;
import com.tcgm.dto.response.TacheResponse;
import com.tcgm.dto.response.TacheDetailResponse;
import com.tcgm.service.TacheService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/taches")
@RequiredArgsConstructor
public class TacheController {

    private final TacheService tacheService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<TacheResponse> createTache(@Valid @RequestBody TacheCreateRequest request) {
        return new ResponseEntity<>(tacheService.createTache(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER', 'MAGASINIER', 'AGENT_SAISIE')")
    public ResponseEntity<TacheDetailResponse> getTacheById(@PathVariable Long id) {
        return ResponseEntity.ok(tacheService.getTacheById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER', 'MAGASINIER')")
    public ResponseEntity<Page<TacheResponse>> getAllTaches(
            @RequestParam(required = false) Long siteId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(tacheService.getAllTaches(siteId, status, search, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<TacheResponse> updateTache(@PathVariable Long id, 
                                                      @Valid @RequestBody TacheUpdateRequest request) {
        return ResponseEntity.ok(tacheService.updateTache(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<Void> deleteTache(@PathVariable Long id) {
        tacheService.deleteTache(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<TacheResponse> updateTacheStatus(@PathVariable Long id, 
                                                            @RequestParam String status) {
        return ResponseEntity.ok(tacheService.updateTacheStatus(id, status));
    }

    @PostMapping("/{tacheId}/ouvriers/{ouvrierId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<TacheDetailResponse> affecterOuvrierTache(
            @PathVariable Long tacheId,
            @PathVariable Long ouvrierId) {
        return ResponseEntity.ok(tacheService.affecterOuvrierTache(tacheId, ouvrierId));
    }

    @DeleteMapping("/{tacheId}/ouvriers/{ouvrierId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<Void> retirerOuvrierTache(
            @PathVariable Long tacheId,
            @PathVariable Long ouvrierId) {
        tacheService.retirerOuvrierTache(tacheId, ouvrierId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/site/{siteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER', 'MAGASINIER')")
    public ResponseEntity<Page<TacheResponse>> getTachesBySite(
            @PathVariable Long siteId,
            Pageable pageable) {
        return ResponseEntity.ok(tacheService.getTachesBySite(siteId, pageable));
    }
}