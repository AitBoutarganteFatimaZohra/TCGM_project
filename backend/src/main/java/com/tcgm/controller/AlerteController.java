package com.tcgm.controller;

import com.tcgm.dto.response.AlerteResponse;
import com.tcgm.service.AlerteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alertes")
@RequiredArgsConstructor
public class AlerteController {

    private final AlerteService alerteService;

    /**
     * Alertes actives pour les chantiers dont l'utilisateur connecté
     * est Chef de Projet.
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET')")
    public ResponseEntity<List<AlerteResponse>> getMyAlertes(Authentication authentication) {
        return ResponseEntity.ok(alerteService.getAlertesForCurrentUser(authentication.getName()));
    }

    /**
     * Marque une alerte comme résolue manuellement.
     */
    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET')")
    public ResponseEntity<AlerteResponse> resolveAlerte(@PathVariable Long id,
                                                         Authentication authentication) {
        return ResponseEntity.ok(alerteService.resolveAlerte(id, authentication.getName()));
    }
}