package com.tcgm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TacheRequest {

    @NotBlank(message = "Le titre de la tâche est obligatoire")
    private String title;

    private String description;
    private LocalDateTime plannedDate;

    private String status; // PLANIFIEE, EN_COURS, TERMINEE
    private Integer priority; // 1-5

    @NotNull(message = "Le site est obligatoire")
    private Long siteId;
}