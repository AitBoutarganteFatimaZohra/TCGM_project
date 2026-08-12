package com.tcgm.controller;

import com.tcgm.dto.request.AffectationRequest;
import com.tcgm.dto.response.AffectationResponse;
import com.tcgm.service.AffectationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/affectations")
@RequiredArgsConstructor
public class AffectationController {

    private final AffectationService affectationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<AffectationResponse> createAffectation(@Valid @RequestBody AffectationRequest request) {
        return new ResponseEntity<>(affectationService.createAffectation(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<AffectationResponse> getAffectationById(@PathVariable Long id) {
        return ResponseEntity.ok(affectationService.getAffectationById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<Page<AffectationResponse>> getAllAffectations(
            @RequestParam(required = false) Long chantierId,
            @RequestParam(required = false) Long ouvrierId,
            @RequestParam(required = false) String statut,
            Pageable pageable) {
        return ResponseEntity.ok(affectationService.getAllAffectations(chantierId, ouvrierId, statut, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<AffectationResponse> updateAffectation(@PathVariable Long id,
                                                                   @Valid @RequestBody AffectationRequest request) {
        return ResponseEntity.ok(affectationService.updateAffectation(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET')")
    public ResponseEntity<Void> deleteAffectation(@PathVariable Long id) {
        affectationService.deleteAffectation(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<AffectationResponse> updateStatut(@PathVariable Long id,
                                                              @RequestParam String statut) {
        return ResponseEntity.ok(affectationService.updateStatut(id, statut));
    }

    @GetMapping("/ouvrier/{ouvrierId}/encours")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<AffectationResponse> getAffectationEnCoursByOuvrier(@PathVariable Long ouvrierId) {
        return ResponseEntity.ok(affectationService.getAffectationEnCoursByOuvrier(ouvrierId));
    }

    @GetMapping("/chantier/{chantierId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<Page<AffectationResponse>> getAffectationsByChantier(@PathVariable Long chantierId,
                                                                                Pageable pageable) {
        return ResponseEntity.ok(affectationService.getAffectationsByChantier(chantierId, pageable));
    }

    @GetMapping("/ouvrier/{ouvrierId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<Page<AffectationResponse>> getAffectationsByOuvrier(@PathVariable Long ouvrierId,
                                                                               Pageable pageable) {
        return ResponseEntity.ok(affectationService.getAffectationsByOuvrier(ouvrierId, pageable));
    }
}