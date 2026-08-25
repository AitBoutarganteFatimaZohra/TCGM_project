package com.tcgm.mapper;

import com.tcgm.dto.request.DossierPointageRequest;
import com.tcgm.dto.request.LignePointageRequest;
import com.tcgm.dto.response.DossierPointageResponse;
import com.tcgm.dto.response.LignePointageResponse;
import com.tcgm.model.DossierPointage;
import com.tcgm.model.LignePointage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PointageMapper {

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

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "site", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "validatedBy", ignore = true)
    @Mapping(target = "status", expression = "java(com.tcgm.model.enums.StatutPointage.EN_ATTENTE)")
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

    List<LignePointageResponse> toLigneResponseList(List<LignePointage> lignes);
    List<DossierPointageResponse> toDossierResponseList(List<DossierPointage> dossiers);

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

    /**
     * Total en heures avec demi-heures (ex: 7.5), calculé à partir des
     * horaires réels de chaque ligne. Le flag halfDay ne sert plus qu'à
     * piloter l'UI (désactivation des champs) — les startTime/endTime
     * réels (08:00-12:00 ou 13:00-17:00) sont toujours envoyés, donc le
     * calcul par différence fonctionne uniformément pour toutes les lignes.
     */
    default Double calculerTotalHeures(List<LignePointage> lignes) {
        if (lignes == null || lignes.isEmpty()) return 0.0;
        double total = 0.0;
        for (LignePointage ligne : lignes) {
            if (ligne.getStartTime() != null && ligne.getEndTime() != null) {
                long minutes = java.time.Duration.between(ligne.getStartTime(), ligne.getEndTime()).toMinutes();
                total += minutes / 60.0;
            } else if (Boolean.TRUE.equals(ligne.getHalfDay())) {
                total += 4.0;
            } else {
                total += 8.0;
            }
        }
        return Math.round(total * 2) / 2.0; // arrondi au quart d'heure... au 0.5h près
    }
}