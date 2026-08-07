package com.tcgm.mapper;

import com.tcgm.dto.request.SiteCreateRequest;
import com.tcgm.dto.request.SiteUpdateRequest;
import com.tcgm.dto.response.SiteDetailResponse;
import com.tcgm.dto.response.SiteResponse;
import com.tcgm.model.Site;
import com.tcgm.model.enums.StatutSite;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SiteMapper {

    // =========================================================
    // ENTITY → RESPONSE
    // =========================================================

    @Mapping(target = "client", expression = "java(mapClient(site))")
    @Mapping(target = "chefProjet", expression = "java(mapUser(site.getChefProjet()))")
    @Mapping(target = "magasinier", expression = "java(mapUser(site.getMagasinier()))")
    @Mapping(target = "agentSaisie", expression = "java(mapUser(site.getAgentSaisie()))")
    @Mapping(target = "chefChantier", expression = "java(mapUser(site.getChefChantier()))")
    @Mapping(target = "totalTaches", expression = "java(site.getTaches() != null ? site.getTaches().size() : 0)")
    @Mapping(target = "totalOuvriers", expression = "java(site.getAffectationsOuvriers() != null ? site.getAffectationsOuvriers().size() : 0)")
    SiteResponse toResponse(Site site);

    @Mapping(target = "client", expression = "java(mapClientDetail(site))")
    @Mapping(target = "chefProjet", expression = "java(mapUserDetail(site.getChefProjet()))")
    @Mapping(target = "magasinier", expression = "java(mapUserDetail(site.getMagasinier()))")
    @Mapping(target = "agentSaisie", expression = "java(mapUserDetail(site.getAgentSaisie()))")
    @Mapping(target = "chefChantier", expression = "java(mapUserDetail(site.getChefChantier()))")
    @Mapping(target = "taches", expression = "java(mapTachesBrief(site.getTaches()))")
    @Mapping(target = "ouvriers", expression = "java(mapOuvriersBrief(site.getAffectationsOuvriers()))")
    @Mapping(target = "totalTaches", expression = "java(site.getTaches() != null ? site.getTaches().size() : 0)")
    @Mapping(target = "totalOuvriers", expression = "java(site.getAffectationsOuvriers() != null ? site.getAffectationsOuvriers().size() : 0)")
    @Mapping(target = "totalPointages", expression = "java(site.getDossiersPointage() != null ? site.getDossiersPointage().size() : 0)")
    SiteDetailResponse toDetailResponse(Site site);

    // =========================================================
    // REQUEST → ENTITY
    // =========================================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "chefProjet", ignore = true)
    @Mapping(target = "magasinier", ignore = true)
    @Mapping(target = "agentSaisie", ignore = true)
    @Mapping(target = "chefChantier", ignore = true)
    @Mapping(target = "status", expression = "java(com.tcgm.model.enums.StatutSite.valueOf(request.getStatus()))")  // ← CORRIGÉ
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "taches", ignore = true)
    @Mapping(target = "affectationsOuvriers", ignore = true)
    @Mapping(target = "dossiersPointage", ignore = true)
    Site toEntity(SiteCreateRequest request);

    // =========================================================
    // UPDATE
    // =========================================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "chefProjet", ignore = true)
    @Mapping(target = "magasinier", ignore = true)
    @Mapping(target = "agentSaisie", ignore = true)
    @Mapping(target = "chefChantier", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "taches", ignore = true)
    @Mapping(target = "affectationsOuvriers", ignore = true)
    @Mapping(target = "dossiersPointage", ignore = true)
    void updateEntity(@MappingTarget Site site, SiteUpdateRequest request);

    // =========================================================
    // LISTES
    // =========================================================

    List<SiteResponse> toResponseList(List<Site> sites);
    List<SiteDetailResponse> toDetailResponseList(List<Site> sites);

    // =========================================================
    // MÉTHODES UTILITAIRES (implémentées par défaut)
    // =========================================================

    default SiteResponse.ClientBrief mapClient(Site site) {
        if (site.getClient() == null) return null;
        return SiteResponse.ClientBrief.builder()
            .id(site.getClient().getId())
            .name(site.getClient().getName())
            .contact(site.getClient().getContact())
            .build();
    }

    default SiteResponse.UserBrief mapUser(com.tcgm.model.User user) {
        if (user == null) return null;
        return SiteResponse.UserBrief.builder()
            .id(user.getId())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .email(user.getEmail())
            .build();
    }

    default SiteDetailResponse.ClientBrief mapClientDetail(Site site) {
        if (site.getClient() == null) return null;
        return SiteDetailResponse.ClientBrief.builder()
            .id(site.getClient().getId())
            .name(site.getClient().getName())
            .contact(site.getClient().getContact())
            .phone(site.getClient().getPhone())
            .email(site.getClient().getEmail())
            .build();
    }

    default SiteDetailResponse.UserBrief mapUserDetail(com.tcgm.model.User user) {
        if (user == null) return null;
        return SiteDetailResponse.UserBrief.builder()
            .id(user.getId())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .email(user.getEmail())
            .phone(user.getPhone())
            .build();
    }

    default List<SiteDetailResponse.TacheBrief> mapTachesBrief(List<com.tcgm.model.Tache> taches) {
        if (taches == null) return null;
        return taches.stream()
            .map(t -> SiteDetailResponse.TacheBrief.builder()
                .id(t.getId())
                .title(t.getTitle())
                .status(t.getStatus() != null ? t.getStatus().name() : null)
                .priority(t.getPriority())
                .build())
            .collect(java.util.stream.Collectors.toList());
    }

    default List<SiteDetailResponse.OuvrierBrief> mapOuvriersBrief(List<com.tcgm.model.AffectationOuvrierSite> affectations) {
        if (affectations == null) return null;
        return affectations.stream()
            .filter(a -> a.getOuvrier() != null)
            .map(a -> SiteDetailResponse.OuvrierBrief.builder()
                .id(a.getOuvrier().getId())
                .firstName(a.getOuvrier().getFirstName())
                .lastName(a.getOuvrier().getLastName())
                .cin(a.getOuvrier().getCin())
                .specialite(a.getOuvrier().getSpecialite())
                .build())
            .collect(java.util.stream.Collectors.toList());
    }
}