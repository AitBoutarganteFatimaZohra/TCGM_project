package com.tcgm.dto.response;

import com.tcgm.model.enums.StatutSite;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SiteDetailResponse {
    private Long id;
    private String name;
    private String reference;
    private String address;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private StatutSite status;
    private ClientBrief client;
    private UserBrief chefProjet;
    private UserBrief magasinier;
    private UserBrief agentSaisie;
    private UserBrief chefChantier;
    private List<TacheBrief> taches;
    private List<OuvrierBrief> ouvriers;
    private Integer totalTaches;
    private Integer totalOuvriers;
    private Integer totalPointages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class ClientBrief {
        private Long id;
        private String name;
        private String contact;
        private String phone;
        private String email;
    }

    @Data
    @Builder
    public static class UserBrief {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
    }

    @Data
    @Builder
    public static class TacheBrief {
        private Long id;
        private String title;
        private String status;
        private Integer priority;
    }

    @Data
    @Builder
    public static class OuvrierBrief {
        private Long id;
        private String firstName;
        private String lastName;
        private String cin;
        private String specialite;
    }
}