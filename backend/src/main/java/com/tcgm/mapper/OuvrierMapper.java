package com.tcgm.mapper;

import com.tcgm.dto.request.OuvrierCreateRequest;
import com.tcgm.dto.request.OuvrierUpdateRequest;
import com.tcgm.dto.request.AffectationSiteRequest;
import com.tcgm.dto.response.OuvrierResponse;
import com.tcgm.dto.response.OuvrierAffectationResponse;
import com.tcgm.model.Ouvrier;
import com.tcgm.model.AffectationOuvrierSite;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.time.LocalDate;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OuvrierMapper {

    // =========================================================
    // ENTITY → RESPONSE
    // =========================================================

    @Mapping(target = "affectations", expression = "java(mapAffectations(ouvrier.getAffectationsSites()))")
    @Mapping(target = "hireDate", expression = "java(formatDate(ouvrier.getHireDate()))")
    OuvrierResponse toResponse(Ouvrier ouvrier);

    @Mapping(target = "ouvrierName", expression = "java(affectation.getOuvrier().getFirstName() + \" \" + affectation.getOuvrier().getLastName())")
    @Mapping(target = "ouvrierId", source = "ouvrier.id")
    @Mapping(target = "siteId", source = "site.id")
    @Mapping(target = "siteName", source = "site.name")
    @Mapping(target = "startDate", expression = "java(formatDate(affectation.getStartDate()))")
    @Mapping(target = "endDate", expression = "java(formatDate(affectation.getEndDate()))")
    OuvrierAffectationResponse toAffectationResponse(AffectationOuvrierSite affectation);

    // =========================================================
    // REQUEST → ENTITY
    // =========================================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hireDate", expression = "java(parseDate(request.getHireDate()))")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "affectationsSites", ignore = true)
    @Mapping(target = "affectationsTaches", ignore = true)
    @Mapping(target = "pointages", ignore = true)
    Ouvrier toEntity(OuvrierCreateRequest request);

    // =========================================================
    // UPDATE
    // =========================================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cin", ignore = true) // CIN ne doit pas être modifiable
    @Mapping(target = "hireDate", expression = "java(parseDate(request.getHireDate()))")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "affectationsSites", ignore = true)
    @Mapping(target = "affectationsTaches", ignore = true)
    @Mapping(target = "pointages", ignore = true)
    void updateEntity(@MappingTarget Ouvrier ouvrier, OuvrierUpdateRequest request);

    // =========================================================
    // LISTES
    // =========================================================

    List<OuvrierResponse> toResponseList(List<Ouvrier> ouvriers);
    List<OuvrierAffectationResponse> toAffectationResponseList(List<AffectationOuvrierSite> affectations);

    // =========================================================
    // MÉTHODES UTILITAIRES (implémentées par défaut)
    // =========================================================

    default List<OuvrierResponse.SiteAffectation> mapAffectations(List<AffectationOuvrierSite> affectations) {
        if (affectations == null) return null;
        return affectations.stream()
            .filter(a -> a.getSite() != null)
            .map(a -> OuvrierResponse.SiteAffectation.builder()
                .id(a.getId())
                .siteId(a.getSite().getId())
                .siteName(a.getSite().getName())
                .startDate(formatDate(a.getStartDate()))
                .endDate(formatDate(a.getEndDate()))
                .active(a.getActive())
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