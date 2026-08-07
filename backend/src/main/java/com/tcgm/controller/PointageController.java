package com.tcgm.controller;

import com.tcgm.dto.request.DossierPointageRequest;
import com.tcgm.dto.request.LignePointageRequest;
import com.tcgm.dto.request.ValidationPointageRequest;
import com.tcgm.dto.response.DossierPointageResponse;
import com.tcgm.dto.response.LignePointageResponse;
import com.tcgm.service.PointageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pointage")
@RequiredArgsConstructor
public class PointageController {

    private final PointageService pointageService;

    @PostMapping("/dossiers")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'AGENT_SAISIE')")
    public ResponseEntity<DossierPointageResponse> createDossierPointage(
            @Valid @RequestBody DossierPointageRequest request) {
        return new ResponseEntity<>(pointageService.createDossierPointage(request), HttpStatus.CREATED);
    }

    @GetMapping("/dossiers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER', 'AGENT_SAISIE')")
    public ResponseEntity<DossierPointageResponse> getDossierPointageById(@PathVariable Long id) {
        return ResponseEntity.ok(pointageService.getDossierPointageById(id));
    }

    @GetMapping("/dossiers")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER', 'AGENT_SAISIE')")
    public ResponseEntity<Page<DossierPointageResponse>> getAllDossiersPointage(
            @RequestParam(required = false) Long siteId,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        return ResponseEntity.ok(pointageService.getAllDossiersPointage(siteId, date, status, pageable));
    }

    @PutMapping("/dossiers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'AGENT_SAISIE')")
    public ResponseEntity<DossierPointageResponse> updateDossierPointage(
            @PathVariable Long id,
            @Valid @RequestBody DossierPointageRequest request) {
        return ResponseEntity.ok(pointageService.updateDossierPointage(id, request));
    }

    @DeleteMapping("/dossiers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'AGENT_SAISIE')")
    public ResponseEntity<Void> deleteDossierPointage(@PathVariable Long id) {
        pointageService.deleteDossierPointage(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/dossiers/{dossierId}/lignes")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'AGENT_SAISIE')")
    public ResponseEntity<LignePointageResponse> addLignePointage(
            @PathVariable Long dossierId,
            @Valid @RequestBody LignePointageRequest request) {
        return new ResponseEntity<>(pointageService.addLignePointage(dossierId, request), HttpStatus.CREATED);
    }

    @DeleteMapping("/lignes/{ligneId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'AGENT_SAISIE')")
    public ResponseEntity<Void> removeLignePointage(@PathVariable Long ligneId) {
        pointageService.removeLignePointage(ligneId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/dossiers/{id}/valider")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<DossierPointageResponse> validerDossierPointage(
            @PathVariable Long id,
            @Valid @RequestBody ValidationPointageRequest request) {
        return ResponseEntity.ok(pointageService.validerDossierPointage(id, request));
    }

    @PostMapping("/dossiers/{id}/rejeter")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<DossierPointageResponse> rejeterDossierPointage(
            @PathVariable Long id,
            @Valid @RequestBody ValidationPointageRequest request) {
        return ResponseEntity.ok(pointageService.rejeterDossierPointage(id, request));
    }

    @GetMapping("/dossiers/site/{siteId}/today")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER', 'AGENT_SAISIE')")
    public ResponseEntity<DossierPointageResponse> getTodayPointage(@PathVariable Long siteId) {
        return ResponseEntity.ok(pointageService.getTodayPointage(siteId));
    }

    @GetMapping("/statistiques/site/{siteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<?> getPointageStatistiques(@PathVariable Long siteId) {
        return ResponseEntity.ok(pointageService.getPointageStatistiques(siteId));
    }
}