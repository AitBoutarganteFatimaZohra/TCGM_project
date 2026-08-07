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
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private StatutSite status;
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