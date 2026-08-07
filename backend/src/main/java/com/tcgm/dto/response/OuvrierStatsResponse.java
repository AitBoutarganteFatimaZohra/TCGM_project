package com.tcgm.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OuvrierStatsResponse {
    private Long ouvrierId;
    private String ouvrierName;
    private String specialite;
    private Integer totalSites;
    private Integer totalTaches;
    private Integer totalHeures;
    private BigDecimal tauxPresence;
    private String siteActuel;
    private String statut;
}