package com.tcgm.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SiteUpdateRequest {

    private String name;
    private String reference;
    private String address;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private Long clientId;
    private Long chefProjetId;
    private Long magasinierId;
    private Long agentSaisieId;
    private Long chefChantierId;
}