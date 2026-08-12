package com.tcgm.dto.response;

import com.tcgm.model.enums.StatutTravaux;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TravauxResponse {
    private Long id;
    private String code;
    private String intitule;
    private String description;
    private LocalDateTime dateDebutPrevue;
    private LocalDateTime dateFinPrevue;
    private LocalDateTime dateDebutReelle;
    private LocalDateTime dateFinReelle;
    private Integer priorite;
    private StatutTravaux statut;
    private BigDecimal budgetEstime;
    private ChantierBrief chantier;
    private List<TacheBrief> taches;
    private Integer totalTaches;
    private Integer totalTachesTerminees;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class ChantierBrief {
        private Long id;
        private String name;
        private String reference;
    }

    @Data
    @Builder
    public static class TacheBrief {
        private Long id;
        private String title;
        private String status;
        private Integer priority;
    }
}
