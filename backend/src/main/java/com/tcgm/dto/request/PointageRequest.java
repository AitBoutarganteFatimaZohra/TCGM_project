package com.tcgm.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PointageRequest {

    private LocalDate date;
    private Long siteId;
    private List<LignePointageRequest> lignes;
    private String notes;
}