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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/taches")
@RequiredArgsConstructor
public class TacheController {

    private final TacheService tacheService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<TacheResponse> createTache(
            @Valid @RequestBody TacheCreateRequest request) {
        return new ResponseEntity<>(tacheService.createTache(request), HttpStatus.CREATED);

    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER', 'MAGASINIER', 'AGENT_SAISIE')")
    public ResponseEntity<TacheDetailResponse> getTacheById(@PathVariable Long id) {
        return ResponseEntity.ok(tacheService.getTacheById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER', 'MAGASINIER', 'AGENT_SAISIE')")
    public ResponseEntity<Page<TacheResponse>> getAllTaches(
            @RequestParam(required = false) Long travauxId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(tacheService.getAllTaches(travauxId, status, search, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<TacheResponse> updateTache(
            @PathVariable Long id,
            @Valid @RequestBody TacheUpdateRequest request) {
        return ResponseEntity.ok(tacheService.updateTache(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<Void> deleteTache(@PathVariable Long id) {
        tacheService.deleteTache(id);
        return ResponseEntity.noContent().build();
    }


    // 🔧 MODIFIÉ : Chef de Chantier retiré — il passe désormais par
    // /proposer-modification. Réservé à Admin/Chef de Projet (les
    // validateurs eux-mêmes n'ont pas besoin de validation).
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET')")
    public ResponseEntity<TacheResponse> updateTacheStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(tacheService.updateTacheStatus(id, status));
    }

    // ⚠️ NOUVEAU — Étape 1 (§2) : Chef de Chantier propose
    @PostMapping("/{id}/proposer-modification")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_CHANTIER')")
    public ResponseEntity<TacheResponse> proposerModification(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String proposedStatus = body.get("status");
        LocalDateTime proposedPlannedDate = body.get("plannedDate") != null
                ? LocalDateTime.parse(body.get("plannedDate"))
                : null;
        return ResponseEntity.ok(tacheService.proposerModification(id, proposedStatus, proposedPlannedDate));
    }

    // ⚠️ NOUVEAU — Étape 2 : Chef de Projet valide
    @PostMapping("/{id}/valider-modification")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET')")
    public ResponseEntity<TacheResponse> validerModification(@PathVariable Long id) {
        return ResponseEntity.ok(tacheService.validerModification(id));
    }


    // ⚠️ NOUVEAU — rejet par Chef de Projet
    @PostMapping("/{id}/rejeter-modification")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET')")
    public ResponseEntity<TacheResponse> rejeterModification(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String motif = body != null ? body.get("motif") : null;
        return ResponseEntity.ok(tacheService.rejeterModification(id, motif));
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

    @GetMapping("/travaux/{travauxId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER', 'MAGASINIER', 'AGENT_SAISIE')")
    public ResponseEntity<Page<TacheResponse>> getTachesByTravaux(
            @PathVariable Long travauxId,
            Pageable pageable) {

        return ResponseEntity.ok(tacheService.getTachesByTravaux(travauxId, pageable));
    }
}