package com.tcgm.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationCountsResponse {
    private long tachesEnAttente;
    private long affectationsEnAttente;
    private long ressourcesEnAttente;
    private long sitesEnAttente;
    private long total;
}