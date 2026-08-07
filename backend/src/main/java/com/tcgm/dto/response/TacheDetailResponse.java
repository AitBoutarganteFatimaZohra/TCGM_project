package com.tcgm.dto.response;

import com.tcgm.model.enums.StatutTache;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TacheDetailResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime plannedDate;
    private LocalDateTime completedDate;
    private StatutTache status;
    private Integer priority;
    private SiteBrief site;
    private List<OuvrierDetail> ouvriers;
    private Integer totalOuvriers;
    private Integer totalHeures;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class SiteBrief {
        private Long id;
        private String name;
        private String reference;
        private String address;
    }

    @Data
    @Builder
    public static class OuvrierDetail {
        private Long id;
        private String firstName;
        private String lastName;
        private String cin;
        private String specialite;
        private String assignedAt;
    }
}