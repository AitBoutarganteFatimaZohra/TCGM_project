package com.tcgm.controller;

import com.tcgm.service.StatistiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statistiques")
@RequiredArgsConstructor
public class StatistiquesController {

    private final StatistiqueService statistiqueService;

    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getDashboardStats() {
        return ResponseEntity.ok(statistiqueService.getDashboardStats());
    }

    @GetMapping("/sites")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET')")
    public ResponseEntity<?> getSitesStats() {
        return ResponseEntity.ok(statistiqueService.getSitesStats());
    }

    @GetMapping("/sites/{siteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<?> getSiteStats(@PathVariable Long siteId) {
        return ResponseEntity.ok(statistiqueService.getSiteStats(siteId));
    }

    @GetMapping("/ouvriers")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<?> getOuvriersStats() {
        return ResponseEntity.ok(statistiqueService.getOuvriersStats());
    }

    @GetMapping("/ouvriers/site/{siteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<?> getOuvriersStatsBySite(@PathVariable Long siteId) {
        return ResponseEntity.ok(statistiqueService.getOuvriersStatsBySite(siteId));
    }

    @GetMapping("/pointage/site/{siteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<?> getPointageStats(@PathVariable Long siteId) {
        return ResponseEntity.ok(statistiqueService.getPointageStats(siteId));
    }

    @GetMapping("/taches")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<?> getTachesStats() {
        return ResponseEntity.ok(statistiqueService.getTachesStats());
    }

    @GetMapping("/taches/site/{siteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<?> getTachesStatsBySite(@PathVariable Long siteId) {
        return ResponseEntity.ok(statistiqueService.getTachesStatsBySite(siteId));
    }

    @GetMapping("/clients")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET')")
    public ResponseEntity<?> getClientsStats() {
        return ResponseEntity.ok(statistiqueService.getClientsStats());
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUsersStats() {
        return ResponseEntity.ok(statistiqueService.getUsersStats());
    }
}