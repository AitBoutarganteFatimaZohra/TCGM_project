package com.tcgm.dto.response;

import com.tcgm.model.enums.StatutAlerte;
import com.tcgm.model.enums.TypeAlerte;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AlerteResponse {
    private Long id;
    private TypeAlerte type;
    private StatutAlerte statut;
    private String message;
    private Long siteId;
    private String siteName;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private String resolvedByName; // null si résolution automatique
}