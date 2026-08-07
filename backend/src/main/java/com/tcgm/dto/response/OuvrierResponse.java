package com.tcgm.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OuvrierResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String cin;
    private String specialite;
    private String phone;
    private String hireDate;
    private Boolean active;
    private List<SiteAffectation> affectations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class SiteAffectation {
        private Long id;
        private Long siteId;
        private String siteName;
        private String startDate;
        private String endDate;
        private Boolean active;
    }
}