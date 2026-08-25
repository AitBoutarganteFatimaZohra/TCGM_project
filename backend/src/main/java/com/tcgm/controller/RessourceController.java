package com.tcgm.controller;

import com.tcgm.dto.request.RessourceCreateRequest;
import com.tcgm.dto.request.RessourceRejetRequest;
import com.tcgm.dto.request.RessourceUpdateRequest;
import com.tcgm.dto.response.RessourceResponse;
import com.tcgm.model.Ressource;
import com.tcgm.service.RessourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAGASINIER')")
    public ResponseEntity<RessourceResponse> updateStatut(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Ressource.StatutRessource statut = Ressource.StatutRessource.valueOf(body.get("statut"));
        return ResponseEntity.ok(ressourceService.updateStatut(id, statut));
    }

    /**
     * ⚠️ CHANGEMENT DE COMPORTEMENT : ne supprime plus immédiatement — place
     * la ressource en attente de validation de suppression et renvoie son
     * état à jour (200) au lieu d'un 204 sans contenu. Le frontend doit
     * gérer le corps de la réponse (voir ressourceApi.js).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAGASINIER')")
    public ResponseEntity<RessourceResponse> delete(@PathVariable Long id) {
        return ResponseEntity.ok(ressourceService.delete(id));
    }

    // =========================================================
    // CIRCUIT DE VALIDATION : Magasinier -> Chef de Chantier -> Chef de Projet
    // =========================================================

    /**
     * Valide l'action en attente, au niveau où elle se trouve actuellement
     * (Chef de Chantier pour le niveau 1, Chef de Projet pour le niveau 2 —
     * recours après un rejet niveau 1). Le rôle exact est vérifié côté
     * service à partir du token de l'utilisateur connecté.
     */
    @PostMapping("/{id}/valider")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_CHANTIER', 'CHEF_PROJET')")
    public ResponseEntity<RessourceResponse> valider(@PathVariable Long id, Authentication authentication) {
        RessourceResponse response = ressourceService.valider(id, extractRole(authentication));
        // response == null signifie que l'action validée était une
        // SUPPRESSION : la ressource a été définitivement effacée.
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/rejeter")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_CHANTIER', 'CHEF_PROJET')")
    public ResponseEntity<RessourceResponse> rejeter(
            @PathVariable Long id,
            @RequestBody(required = false) RessourceRejetRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(ressourceService.rejeter(id, request, extractRole(authentication)));
    }

    // =========================================================
    // UTILITAIRE
    // =========================================================

    private String extractRole(Authentication authentication) {
        if (authentication == null) return null;
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .findFirst()
                .orElse(null);
    }
}