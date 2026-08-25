package com.tcgm.controller;

import com.tcgm.dto.request.OuvrierCreateRequest;
import com.tcgm.dto.request.OuvrierUpdateRequest;
import com.tcgm.dto.request.AffectationSiteRequest;
import com.tcgm.dto.response.OuvrierResponse;
import com.tcgm.service.OuvrierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ouvriers")
@RequiredArgsConstructor
public class OuvrierController {

    private final OuvrierService ouvrierService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_CHANTIER')")
    public ResponseEntity<OuvrierResponse> createOuvrier(@Valid @RequestBody OuvrierCreateRequest request) {
        return new ResponseEntity<>(ouvrierService.createOuvrier(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER', 'AGENT_SAISIE')")
    public ResponseEntity<OuvrierResponse> getOuvrierById(@PathVariable Long id) {
        return ResponseEntity.ok(ouvrierService.getOuvrierById(id));
    }

    // 🔧 CORRIGÉ : AGENT_SAISIE ajouté — sans quoi le dropdown "Ouvrier"
    // du formulaire de pointage reste vide (403 silencieux côté frontend).
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER', 'MAGASINIER', 'AGENT_SAISIE')")
    public ResponseEntity<Page<OuvrierResponse>> getAllOuvriers(
            @RequestParam(required = false) Long chantierId,
            @RequestParam(required = false) String specialite,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(ouvrierService.getAllOuvriers(chantierId, specialite, active, search, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_CHANTIER')")
    public ResponseEntity<OuvrierResponse> updateOuvrier(@PathVariable Long id, 
                                                          @Valid @RequestBody OuvrierUpdateRequest request) {
        return ResponseEntity.ok(ouvrierService.updateOuvrier(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_CHANTIER')")
    public ResponseEntity<Void> deleteOuvrier(@PathVariable Long id) {
        ouvrierService.deleteOuvrier(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/affectations")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_CHANTIER')")
    public ResponseEntity<OuvrierResponse> affecterOuvrierSite(
            @Valid @RequestBody AffectationSiteRequest request) {
        return ResponseEntity.ok(ouvrierService.affecterOuvrierSite(request));
    }

    @DeleteMapping("/affectations/{affectationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_CHANTIER')")
    public ResponseEntity<Void> desaffecterOuvrierSite(@PathVariable Long affectationId) {
        ouvrierService.desaffecterOuvrierSite(affectationId);
        return ResponseEntity.noContent().build();
    }

    // 🔧 CORRIGÉ : AGENT_SAISIE ajouté — utilisé pour peupler le dropdown
    // "Ouvrier" scopé sur le chantier unique de l'agent.
    @GetMapping("/chantier/{chantierId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER', 'MAGASINIER', 'AGENT_SAISIE')")
    public ResponseEntity<Page<OuvrierResponse>> getOuvriersByChantier(
            @PathVariable Long chantierId,
            Pageable pageable) {
        return ResponseEntity.ok(ouvrierService.getOuvriersByChantier(chantierId, pageable));
    }
}