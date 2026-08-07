package com.tcgm.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class StatistiquesResponse {
    // Statistiques générales
    private Long totalSites;
    private Long totalSitesEnCours;
    private Long totalSitesTermines;
    private Long totalClients;
    private Long totalOuvriers;
    private Long totalOuvriersActifs;
    private Long totalTaches;
    private Long totalTachesEnCours;
    private Long totalTachesTerminees;
    private Long totalPointages;
    private Long totalPointagesValides;
    private Long totalUsers;

    // Statistiques par statut
    private Map<String, Long> sitesByStatus;
    private Map<String, Long> tachesByStatus;
    private Map<String, Long> pointagesByStatus;

    // Statistiques de valeur (si applicable)
    private BigDecimal valeurTotale;
    private BigDecimal valeurMoyenne;

    // Période
    private String periode;
}