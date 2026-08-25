package com.tcgm.controller;

import com.tcgm.dto.request.RessourceCreateRequest;
import com.tcgm.dto.request.RessourceUpdateRequest;
import com.tcgm.dto.response.RessourceResponse;
import com.tcgm.model.Ressource;
import com.tcgm.service.RessourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ressources")
@RequiredArgsConstructor
public class RessourceController {

    private final RessourceService ressourceService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RessourceResponse>> getAll() {
        return ResponseEntity.ok(ressourceService.getAll());
    }

    @GetMapping("/site/{siteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAGASINIER', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<List<RessourceResponse>> getBySite(
            @PathVariable Long siteId,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ressourceService.getBySite(siteId, statut, type, search));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAGASINIER', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<RessourceResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ressourceService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MAGASINIER')")
    public ResponseEntity<RessourceResponse> create(@Valid @RequestBody RessourceCreateRequest request) {
        return new ResponseEntity<>(ressourceService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAGASINIER')")
    public ResponseEntity<RessourceResponse> update(@PathVariable Long id, @Valid @RequestBody RessourceUpdateRequest request) {
        return ResponseEntity.ok(ressourceService.update(id, request));
    }

    // 🔧 MODIFIÉ : réservé à l'Admin — override direct hors circuit de validation.
    // Le Magasinier passe désormais par /proposer-statut.
    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RessourceResponse> updateStatut(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Ressource.StatutRessource statut = Ressource.StatutRessource.valueOf(body.get("statut"));
        return ResponseEntity.ok(ressourceService.updateStatut(id, statut));
    }

    // ⚠️ NOUVEAU — Étape 1 (§3) : le Magasinier propose un changement de statut
    @PostMapping("/{id}/proposer-statut")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAGASINIER')")
    public ResponseEntity<RessourceResponse> proposerStatut(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Ressource.StatutRessource statut = Ressource.StatutRessource.valueOf(body.get("statut"));
        return ResponseEntity.ok(ressourceService.proposerStatut(id, statut));
    }

    // ⚠️ NOUVEAU — Étape 2, niveau 1 ou 2 : Chef de Chantier ou Chef de Projet valide
    @PostMapping("/{id}/valider-statut")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<RessourceResponse> validerStatut(@PathVariable Long id) {
        return ResponseEntity.ok(ressourceService.validerStatut(id));
    }

    // ⚠️ NOUVEAU — rejet par Chef de Chantier ou Chef de Projet
    @PostMapping("/{id}/rejeter-statut")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<RessourceResponse> rejeterStatut(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String motif = body != null ? body.get("motif") : null;
        return ResponseEntity.ok(ressourceService.rejeterStatut(id, motif));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAGASINIER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ressourceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}