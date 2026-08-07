package com.tcgm.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardStatsResponse {
    private Long totalSites;
    private Long totalOuvriers;
    private Long totalTaches;
    private Long totalClients;
    private Long totalUsers;
    private Long totalPointages;
    private Long ouvriersActifs;

    private Map<String, Long> sitesByStatus;
    private Map<String, Long> tachesByStatus;
    private Map<String, Long> pointagesByStatus;
    private Map<String, Long> ouvriersBySpecialite;
    private Map<String, Long> sitesByClient;

    private List<JournalResponse> recentActivity;
    private List<SiteResponse> sitesRecents;

    @Data
    @Builder
    public static class KpiCard {
        private String label;
        private Long value;
        private String icon;
        private String color;
        private BigDecimal evolution;
    }

    @Data
    @Builder
    public static class ChartData {
        private String label;
        private Long value;
        private String color;
    }
}