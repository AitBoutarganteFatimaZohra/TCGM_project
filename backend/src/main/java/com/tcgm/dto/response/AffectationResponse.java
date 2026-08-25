package com.tcgm.dto.response;

import com.tcgm.model.enums.StatutAffectation;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AffectationResponse {
    private Long id;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private StatutAffectation statut;
    // ✅ NOUVEAU
    private String rejectionReason;
    private ChantierBrief chantier;
    private OuvrierBrief ouvrier;
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
    public static class OuvrierBrief {
        private Long id;
        private String firstName;
        private String lastName;
        private String cin;
        private String specialite;
    }
}