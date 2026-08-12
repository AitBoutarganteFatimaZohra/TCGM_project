package com.tcgm.mapper;

import com.tcgm.dto.request.DossierPointageRequest;
import com.tcgm.dto.request.LignePointageRequest;
import com.tcgm.dto.response.DossierPointageResponse;
import com.tcgm.dto.response.LignePointageResponse;
import com.tcgm.dto.response.PointageResponse;
import com.tcgm.model.DossierPointage;
import com.tcgm.model.LignePointage;
import com.tcgm.model.enums.StatutPointage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PointageMapper {

    // =========================================================
    // ENTITY → RESPONSE
    // =========================================================

    @Mapping(target = "site", expression = "java(mapSiteBrief(dossier))")
    @Mapping(target = "createdBy", expression = "java(mapUserBrief(dossier.getCreatedBy()))")
    @Mapping(target = "validatedBy", expression = "java(mapUserBrief(dossier.getValidatedBy()))")
    @Mapping(target = "lignes", expression = "java(toLigneResponseList(dossier.getLignes()))")
    @Mapping(target = "totalOuvriers", expression = "java(dossier.getLignes() != null ? dossier.getLignes().size() : 0)")
    @Mapping(target = "totalHeures", expression = "java(calculerTotalHeures(dossier.getLignes()))")
    DossierPointageResponse toDossierResponse(DossierPointage dossier);

    @Mapping(target = "ouvrierId", source = "ouvrier.id")
    @Mapping(target = "ouvrierName", expression = "java(ligne.getOuvrier().getFirstName() + \" \" + ligne.getOuvrier().getLastName())")
    @Mapping(target = "ouvrierCin", source = "ouvrier.cin")
    @Mapping(target = "tacheId", source = "tache.id")
    @Mapping(target = "tacheTitle", source = "tache.title")
    LignePointageResponse toLigneResponse(LignePointage ligne);

    // =========================================================
    // REQUEST → ENTITY
    // =========================================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "site", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "validatedBy", ignore = true)
    @Mapping(target = "status", expression = "java(com.tcgm.model.enums.StatutPointage.EN_ATTENTE)")  // ← CORRIGÉ
    @Mapping(target = "validatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lignes", ignore = true)
    DossierPointage toDossierEntity(DossierPointageRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dossier", ignore = true)
    @Mapping(target = "ouvrier", ignore = true)
    @Mapping(target = "tache", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    LignePointage toLigneEntity(LignePointageRequest request);

    // =========================================================
    // LISTES
    // =========================================================

    List<LignePointageResponse> toLigneResponseList(List<LignePointage> lignes);
    List<DossierPointageResponse> toDossierResponseList(List<DossierPointage> dossiers);

    // =========================================================
    // MÉTHODES UTILITAIRES (implémentées par défaut)
    // =========================================================

    default DossierPointageResponse.SiteBrief mapSiteBrief(DossierPointage dossier) {
        if (dossier.getSite() == null) return null;
        return DossierPointageResponse.SiteBrief.builder()
            .id(dossier.getSite().getId())
            .name(dossier.getSite().getName())
            .reference(dossier.getSite().getReference())
            .build();
    }

    default DossierPointageResponse.UserBrief mapUserBrief(com.tcgm.model.User user) {
        if (user == null) return null;
        return DossierPointageResponse.UserBrief.builder()
            .id(user.getId())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .email(user.getEmail())
            .build();
    }

    default Integer calculerTotalHeures(List<LignePointage> lignes) {
        if (lignes == null || lignes.isEmpty()) return 0;
        int total = 0;
        for (LignePointage ligne : lignes) {
            if (ligne.getStartTime() != null && ligne.getEndTime() != null) {
                long diff = java.time.Duration.between(ligne.getStartTime(), ligne.getEndTime()).toHours();
                total += (int) diff;
            } else if (ligne.getHalfDay() != null && ligne.getHalfDay()) {
                total += 4;
            } else if (ligne.getHalfDay() != null && !ligne.getHalfDay()) {
                total += 8;
            }
        }
        return total;
    }
}