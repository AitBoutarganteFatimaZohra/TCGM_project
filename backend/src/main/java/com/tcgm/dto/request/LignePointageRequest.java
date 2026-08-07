package com.tcgm.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LignePointageRequest {

    @NotNull(message = "L'ID de l'ouvrier est obligatoire")
    private Long ouvrierId;

    @NotNull(message = "L'ID de la tâche est obligatoire")
    private Long tacheId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean halfDay; // true = demi-journée, false = journée complète
    private String notes;
}