package com.tcgm.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LignePointageResponse {
    private Long id;
    private Long ouvrierId;
    private String ouvrierName;
    private String ouvrierCin;
    private Long tacheId;
    private String tacheTitle;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean halfDay;
    private String notes;
}