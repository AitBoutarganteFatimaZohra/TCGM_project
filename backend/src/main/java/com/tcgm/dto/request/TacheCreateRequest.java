
package com.tcgm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TacheCreateRequest {

    @NotBlank(message = "Le titre de la tâche est obligatoire")
    private String title;

    private String description;

    private LocalDateTime plannedDate;

    private String status;

    private Integer priority;

    @NotNull(message = "Les travaux sont obligatoires")
    private Long travauxId;
}

