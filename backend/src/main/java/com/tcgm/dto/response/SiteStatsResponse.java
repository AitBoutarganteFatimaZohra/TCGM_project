package com.tcgm.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SiteStatsResponse {
    private Long siteId;
    private String siteName;
    private String siteStatus;
    private Integer totalTaches;
    private Integer totalTachesTerminees;
    private Integer totalTachesEnCours;
    private Integer totalOuvriers;
    private Integer totalOuvriersActifs;
    private Integer totalPointages;
    private Integer totalPointagesValides;
    private BigDecimal tauxAvancement;
    private String dureeEstimee;
    private String dureeReelle;
}