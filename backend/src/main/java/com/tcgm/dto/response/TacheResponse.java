package com.tcgm.dto.response;

import com.tcgm.model.enums.StatutTache;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TacheResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime plannedDate;
    private LocalDateTime completedDate;
    private StatutTache status;
    private Integer priority;
    private SiteBrief site;
    private List<OuvrierBrief> ouvriers;
    private Integer totalOuvriers;
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
    public static class OuvrierBrief {
        private Long id;
        private String firstName;
        private String lastName;
        private String cin;
    }
}