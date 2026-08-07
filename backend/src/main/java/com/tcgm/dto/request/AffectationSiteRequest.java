package com.tcgm.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AffectationSiteRequest {

    @NotNull(message = "L'ID de l'ouvrier est obligatoire")
    private Long ouvrierId;

    @NotNull(message = "L'ID du site est obligatoire")
    private Long siteId;

    private String startDate; // Format: yyyy-MM-dd
    private String endDate;   // Format: yyyy-MM-dd
}