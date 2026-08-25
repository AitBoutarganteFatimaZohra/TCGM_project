package com.tcgm.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class JournalResponse {
    private Long id;
    private String actionType;
    private String entityType;
    private Long entityId;
    private String details;
    private String ipAddress;
    private UserBrief user;
    private LocalDateTime createdAt;

    // =========================================================
    // VALIDATION (cahier des charges §6.7)
    // =========================================================
    private String status; // EN_ATTENTE, VALIDE, REJETE
    private UserBrief validatedBy;
    private LocalDateTime validatedAt;

    @Data
    @Builder
    public static class UserBrief {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
    }
}