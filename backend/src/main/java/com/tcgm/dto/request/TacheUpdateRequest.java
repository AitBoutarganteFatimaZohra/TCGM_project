package com.tcgm.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TacheUpdateRequest {

    private String title;
    private String description;
    private LocalDateTime plannedDate;
    private String status;
    private Integer priority;
}