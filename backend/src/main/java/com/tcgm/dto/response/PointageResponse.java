package com.tcgm.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PointageResponse {
    private Long id;
    private String date;
    private String status;
    private SiteBrief site;
    private UserBrief createdBy;
    private UserBrief validatedBy;
    private String validatedAt;
    private List<LignePointageResponse> lignes;
    private Integer totalOuvriers;
    private Integer totalHeures;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class SiteBrief {
        private Long id;
        private String name;
        private String reference;
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