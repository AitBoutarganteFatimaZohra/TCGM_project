package com.tcgm.dto.response;

import com.tcgm.model.enums.StatutSite;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SiteResponse {
    private Long id;
    private String name;
    private String reference;
    private String address;
    private String description;
    private Double latitude;
    private Double longitude;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private StatutSite status;

    // ⚠️ NOUVEAU
    private StatutSite pendingStatus;
    private LocalDateTime pendingStartDate;
    private LocalDateTime pendingEndDate;
    private String motifRejet;

    private ClientBrief client;
    private UserBrief chefProjet;
    private UserBrief magasinier;
    private UserBrief agentSaisie;

    private UserBrief chefChantier;
    private Integer totalTaches;
    private Integer totalOuvriers;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class ClientBrief {
        private Long id;
        private String name;
        private String contact;
    }

    @Data
    @Builder
    public static class UserBrief {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
    }
}