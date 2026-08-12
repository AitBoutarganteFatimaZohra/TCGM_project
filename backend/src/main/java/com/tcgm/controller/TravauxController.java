package com.tcgm.controller;

import com.tcgm.dto.request.TravauxRequest;
import com.tcgm.dto.response.TravauxResponse;
import com.tcgm.service.TravauxService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/travaux")
@RequiredArgsConstructor
public class TravauxController {

    private final TravauxService travauxService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<TravauxResponse> createTravaux(@Valid @RequestBody TravauxRequest request) {
        return new ResponseEntity<>(travauxService.createTravaux(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<TravauxResponse> getTravauxById(@PathVariable Long id) {
        return ResponseEntity.ok(travauxService.getTravauxById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<Page<TravauxResponse>> getAllTravaux(
            @RequestParam(required = false) Long chantierId,
            @RequestParam(required = false) String statut,
            Pageable pageable) {
        return ResponseEntity.ok(travauxService.getAllTravaux(chantierId, statut, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<TravauxResponse> updateTravaux(@PathVariable Long id,
                                                          @Valid @RequestBody TravauxRequest request) {
        return ResponseEntity.ok(travauxService.updateTravaux(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET')")
    public ResponseEntity<Void> deleteTravaux(@PathVariable Long id) {
        travauxService.deleteTravaux(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<TravauxResponse> updateStatut(@PathVariable Long id,
                                                         @RequestParam String statut) {
        return ResponseEntity.ok(travauxService.updateStatut(id, statut));
    }

    @GetMapping("/chantier/{chantierId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<Page<TravauxResponse>> getTravauxByChantier(@PathVariable Long chantierId,
                                                                       Pageable pageable) {
        return ResponseEntity.ok(travauxService.getTravauxByChantier(chantierId, pageable));
    }
}