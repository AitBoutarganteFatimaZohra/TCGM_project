package com.tcgm.dto.response;

import com.tcgm.model.enums.StatutPointage;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DossierPointageResponse {
    private Long id;
    private LocalDate date;
    private StatutPointage status;
    private SiteBrief site;
    private UserBrief createdBy;
    private UserBrief validatedBy;
    private LocalDateTime validatedAt;
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