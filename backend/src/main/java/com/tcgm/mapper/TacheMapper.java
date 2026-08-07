package com.tcgm.mapper;

import com.tcgm.dto.request.TacheCreateRequest;
import com.tcgm.dto.request.TacheUpdateRequest;
import com.tcgm.dto.response.TacheResponse;
import com.tcgm.dto.response.TacheDetailResponse;
import com.tcgm.model.Tache;
import com.tcgm.model.enums.StatutTache;
import com.tcgm.model.AffectationOuvrierTache;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TacheMapper {

    // =========================================================
    // ENTITY → RESPONSE
    // =========================================================

    @Mapping(target = "site", expression = "java(mapSiteBrief(tache))")
    @Mapping(target = "ouvriers", expression = "java(mapOuvriersBrief(tache.getAffectationsOuvriers()))")
    @Mapping(target = "totalOuvriers", expression = "java(tache.getAffectationsOuvriers() != null ? tache.getAffectationsOuvriers().size() : 0)")
    @Mapping(target = "status", source = "status")
    TacheResponse toResponse(Tache tache);

    @Mapping(target = "site", expression = "java(mapSiteDetail(tache))")
    @Mapping(target = "ouvriers", expression = "java(mapOuvriersDetail(tache.getAffectationsOuvriers()))")
    @Mapping(target = "totalOuvriers", expression = "java(tache.getAffectationsOuvriers() != null ? tache.getAffectationsOuvriers().size() : 0)")
    @Mapping(target = "totalHeures", expression = "java(0)") // À calculer si nécessaire
    TacheDetailResponse toDetailResponse(Tache tache);

    // =========================================================
    // REQUEST → ENTITY
    // =========================================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "site", ignore = true)
    @Mapping(target = "status", expression = "java(com.tcgm.model.enums.StatutTache.valueOf(request.getStatus()))")  // ← CORRIGÉ
    @Mapping(target = "completedDate", ignore = true)
    @Mapping(target = "affectationsOuvriers", ignore = true)
    Tache toEntity(TacheCreateRequest request);

    // =========================================================
    // UPDATE
    // =========================================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "site", ignore = true)
    @Mapping(target = "completedDate", ignore = true)
    @Mapping(target = "affectationsOuvriers", ignore = true)
    void updateEntity(@MappingTarget Tache tache, TacheUpdateRequest request);

    // =========================================================
    // LISTES
    // =========================================================

    List<TacheResponse> toResponseList(List<Tache> taches);
    List<TacheDetailResponse> toDetailResponseList(List<Tache> taches);

    // =========================================================
    // MÉTHODES UTILITAIRES (implémentées par défaut)
    // =========================================================

    default TacheResponse.SiteBrief mapSiteBrief(Tache tache) {
        if (tache.getSite() == null) return null;
        return TacheResponse.SiteBrief.builder()
            .id(tache.getSite().getId())
            .name(tache.getSite().getName())
            .reference(tache.getSite().getReference())
            .build();
    }

    default TacheResponse.OuvrierBrief mapOuvrierBrief(AffectationOuvrierTache affectation) {
        if (affectation == null || affectation.getOuvrier() == null) return null;
        return TacheResponse.OuvrierBrief.builder()
            .id(affectation.getOuvrier().getId())
            .firstName(affectation.getOuvrier().getFirstName())
            .lastName(affectation.getOuvrier().getLastName())
            .cin(affectation.getOuvrier().getCin())
            .build();
    }

    default List<TacheResponse.OuvrierBrief> mapOuvriersBrief(List<AffectationOuvrierTache> affectations) {
        if (affectations == null) return null;
        return affectations.stream()
            .filter(a -> a.getOuvrier() != null)
            .map(this::mapOuvrierBrief)
            .collect(java.util.stream.Collectors.toList());
    }

    default TacheDetailResponse.SiteBrief mapSiteDetail(Tache tache) {
        if (tache.getSite() == null) return null;
        return TacheDetailResponse.SiteBrief.builder()
            .id(tache.getSite().getId())
            .name(tache.getSite().getName())
            .reference(tache.getSite().getReference())
            .address(tache.getSite().getAddress())
            .build();
    }

    default TacheDetailResponse.OuvrierDetail mapOuvrierDetail(AffectationOuvrierTache affectation) {
        if (affectation == null || affectation.getOuvrier() == null) return null;
        return TacheDetailResponse.OuvrierDetail.builder()
            .id(affectation.getOuvrier().getId())
            .firstName(affectation.getOuvrier().getFirstName())
            .lastName(affectation.getOuvrier().getLastName())
            .cin(affectation.getOuvrier().getCin())
            .specialite(affectation.getOuvrier().getSpecialite())
            .assignedAt(affectation.getAssignedAt() != null ? affectation.getAssignedAt().toString() : null)
            .build();
    }

    default List<TacheDetailResponse.OuvrierDetail> mapOuvriersDetail(List<AffectationOuvrierTache> affectations) {
        if (affectations == null) return null;
        return affectations.stream()
            .filter(a -> a.getOuvrier() != null)
            .map(this::mapOuvrierDetail)
            .collect(java.util.stream.Collectors.toList());
    }
}