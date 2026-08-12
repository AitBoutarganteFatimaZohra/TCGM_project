
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

    /**
     * Créer une tâche
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<TacheResponse> createTache(
            @Valid @RequestBody TacheCreateRequest request) {

        return new ResponseEntity<>(
                tacheService.createTache(request),
                HttpStatus.CREATED
        );
    }

    /**
     * Récupérer une tâche par son ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER', 'MAGASINIER', 'AGENT_SAISIE')")
    public ResponseEntity<TacheDetailResponse> getTacheById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                tacheService.getTacheById(id)
        );
    }

    /**
     * Récupérer toutes les tâches
     * avec filtre optionnel par Travaux
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER', 'MAGASINIER')")
    public ResponseEntity<Page<TacheResponse>> getAllTaches(
            @RequestParam(required = false) Long travauxId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            Pageable pageable) {

        return ResponseEntity.ok(
                tacheService.getAllTaches(
                        travauxId,
                        status,
                        search,
                        pageable
                )
        );
    }

    /**
     * Modifier une tâche
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<TacheResponse> updateTache(
            @PathVariable Long id,
            @Valid @RequestBody TacheUpdateRequest request) {

        return ResponseEntity.ok(
                tacheService.updateTache(id, request)
        );
    }

    /**
     * Supprimer une tâche
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<Void> deleteTache(
            @PathVariable Long id) {

        tacheService.deleteTache(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Modifier le statut d'une tâche
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<TacheResponse> updateTacheStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        return ResponseEntity.ok(
                tacheService.updateTacheStatus(id, status)
        );
    }

    /**
     * Affecter un ouvrier à une tâche
     */
    @PostMapping("/{tacheId}/ouvriers/{ouvrierId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<TacheDetailResponse> affecterOuvrierTache(
            @PathVariable Long tacheId,
            @PathVariable Long ouvrierId) {

        return ResponseEntity.ok(
                tacheService.affecterOuvrierTache(
                        tacheId,
                        ouvrierId
                )
        );
    }

    /**
     * Retirer un ouvrier d'une tâche
     */
    @DeleteMapping("/{tacheId}/ouvriers/{ouvrierId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<Void> retirerOuvrierTache(
            @PathVariable Long tacheId,
            @PathVariable Long ouvrierId) {

        tacheService.retirerOuvrierTache(
                tacheId,
                ouvrierId
        );

        return ResponseEntity.noContent().build();
    }

    /**
     * Récupérer les tâches d'un lot de travaux
     */
    @GetMapping("/travaux/{travauxId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER', 'MAGASINIER')")
    public ResponseEntity<Page<TacheResponse>> getTachesByTravaux(
            @PathVariable Long travauxId,
            Pageable pageable) {

        return ResponseEntity.ok(
                tacheService.getTachesByTravaux(
                        travauxId,
                        pageable
                )
        );
    }
}

