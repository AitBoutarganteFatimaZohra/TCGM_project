package com.tcgm.mapper;

import com.tcgm.dto.request.OuvrierCreateRequest;
import com.tcgm.dto.request.OuvrierUpdateRequest;
import com.tcgm.dto.request.AffectationSiteRequest;
import com.tcgm.dto.response.OuvrierResponse;
import com.tcgm.dto.response.OuvrierAffectationResponse;
import com.tcgm.model.Ouvrier;
import com.tcgm.model.Affectation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OuvrierMapper {

    // =========================================================
    // ENTITY → RESPONSE
    // =========================================================

    // MODIFIÉ : affectationsSites → affectations
    @Mapping(target = "affectations", expression = "java(mapAffectations(ouvrier.getAffectations()))")
    @Mapping(target = "hireDate", expression = "java(formatDate(ouvrier.getHireDate()))")
    OuvrierResponse toResponse(Ouvrier ouvrier);

    @Mapping(target = "ouvrierName", expression = "java(affectation.getOuvrier().getFirstName() + \" \" + affectation.getOuvrier().getLastName())")
    @Mapping(target = "ouvrierId", source = "ouvrier.id")
    @Mapping(target = "siteId", source = "chantier.id")
    @Mapping(target = "siteName", source = "chantier.name")
    @Mapping(target = "startDate", expression = "java(formatDate(affectation.getDateDebut()))")
    @Mapping(target = "endDate", expression = "java(formatDate(affectation.getDateFin()))")
    OuvrierAffectationResponse toAffectationResponse(Affectation affectation);

    // =========================================================
    // REQUEST → ENTITY
    // =========================================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hireDate", expression = "java(parseDate(request.getHireDate()))")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "affectations", ignore = true)  // MODIFIÉ
    @Mapping(target = "affectationsTaches", ignore = true)
    @Mapping(target = "pointages", ignore = true)
    Ouvrier toEntity(OuvrierCreateRequest request);

    // =========================================================
    // UPDATE
    // =========================================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cin", ignore = true)
    @Mapping(target = "hireDate", expression = "java(parseDate(request.getHireDate()))")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "affectations", ignore = true)  // MODIFIÉ
    @Mapping(target = "affectationsTaches", ignore = true)
    @Mapping(target = "pointages", ignore = true)
    void updateEntity(@MappingTarget Ouvrier ouvrier, OuvrierUpdateRequest request);

    // =========================================================
    // LISTES
    // =========================================================

    List<OuvrierResponse> toResponseList(List<Ouvrier> ouvriers);
    List<OuvrierAffectationResponse> toAffectationResponseList(List<Affectation> affectations);

    // =========================================================
    // MÉTHODES UTILITAIRES
    // =========================================================

    // MODIFIÉ : prend maintenant une liste d'Affectation
    default List<OuvrierResponse.SiteAffectation> mapAffectations(List<Affectation> affectations) {
        if (affectations == null) return null;
        return affectations.stream()
            .filter(a -> a.getChantier() != null)
            .map(a -> OuvrierResponse.SiteAffectation.builder()
                .id(a.getId())
                .siteId(a.getChantier().getId())
                .siteName(a.getChantier().getName())
                .startDate(formatDate(a.getDateDebut()))
                .endDate(formatDate(a.getDateFin()))
                .active(a.isEnCours())
                .build())
            .collect(java.util.stream.Collectors.toList());
    }

    default String formatDate(LocalDate date) {
        if (date == null) return null;
        return date.toString();
    }

    default LocalDate parseDate(String date) {
        if (date == null || date.isEmpty()) return null;
        return LocalDate.parse(date);
    }
}