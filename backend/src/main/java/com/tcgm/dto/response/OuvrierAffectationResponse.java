package com.tcgm.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OuvrierAffectationResponse {
    private Long id;
    private Long ouvrierId;
    private String ouvrierName;
    private Long siteId;
    private String siteName;
    private String startDate;
    private String endDate;
    private Boolean active;
    private String createdAt;
}