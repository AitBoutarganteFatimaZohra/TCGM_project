package com.tcgm.mapper;

import com.tcgm.dto.response.JournalResponse;
import com.tcgm.model.JournalOperation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface JournalMapper {

    // =========================================================
    // ENTITY → RESPONSE
    // =========================================================

    @Mapping(target = "user", expression = "java(mapUser(operation))")
    @Mapping(target = "actionType", source = "actionType")
    @Mapping(target = "entityType", source = "entityType")
    JournalResponse toResponse(JournalOperation operation);

    // =========================================================
    // LISTES
    // =========================================================

    List<JournalResponse> toResponseList(List<JournalOperation> operations);

    // =========================================================
    // MÉTHODES UTILITAIRES (implémentées par défaut)
    // =========================================================

    default JournalResponse.UserBrief mapUser(JournalOperation operation) {
        if (operation.getUser() == null) return null;
        return JournalResponse.UserBrief.builder()
            .id(operation.getUser().getId())
            .firstName(operation.getUser().getFirstName())
            .lastName(operation.getUser().getLastName())
            .email(operation.getUser().getEmail())
            .build();
    }
}