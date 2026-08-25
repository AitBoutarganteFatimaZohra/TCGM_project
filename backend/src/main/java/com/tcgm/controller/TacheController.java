package com.tcgm.controller;

import com.tcgm.dto.request.TacheCreateRequest;
import com.tcgm.dto.request.TacheUpdateRequest;
import com.tcgm.dto.request.TacheSoumissionRequest;
import com.tcgm.dto.request.TacheRejetRequest;
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

    /**
     * Override direct réservé à l'Administrateur : contourne le circuit de
     * validation. Le Chef de Chantier doit désormais passer par
     * /soumettre, et le Chef de Projet par /valider ou /rejeter.
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TacheResponse> updateTacheStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(tacheService.updateTacheStatus(id, status));
    }

    // =========================================================
    // CIRCUIT DE VALIDATION : Chef de Chantier -> Chef de Projet
    // =========================================================

    /**
     * Étape 1 : le Chef de Chantier soumet un changement de statut et/ou
     * de date prévue. La tâche passe en EN_ATTENTE_VALIDATION.
     */
    @PostMapping("/{id}/soumettre")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_CHANTIER')")
    public ResponseEntity<TacheResponse> soumettreTache(
            @PathVariable Long id,
            @RequestBody TacheSoumissionRequest request) {
        return ResponseEntity.ok(tacheService.soumettreTache(id, request));
    }

    /**
     * Étape 2a : le Chef de Projet valide la demande en attente.
     */
    @PostMapping("/{id}/valider")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET')")
    public ResponseEntity<TacheResponse> validerTache(@PathVariable Long id) {
        return ResponseEntity.ok(tacheService.validerTache(id));
    }

    /**
     * Étape 2b : le Chef de Projet rejette la demande en attente.
     */
    @PostMapping("/{id}/rejeter")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET')")
    public ResponseEntity<TacheResponse> rejeterTache(
            @PathVariable Long id,
            @RequestBody(required = false) TacheRejetRequest request) {
        return ResponseEntity.ok(tacheService.rejeterTache(id, request));
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