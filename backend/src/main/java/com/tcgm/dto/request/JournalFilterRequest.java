package com.tcgm.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JournalFilterRequest {

    private String actionType;   // CREATION, MODIFICATION, SUPPRESSION, etc.
    private String entityType;   // SITE, TACHE, OUVRIER, etc.
    private Long entityId;
    private Long userId;
    private String startDate;    // Format: yyyy-MM-dd
    private String endDate;      // Format: yyyy-MM-dd
    private String search;       // Recherche textuelle
}