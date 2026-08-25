package com.tcgm.mapper;

import com.tcgm.dto.request.AffectationRequest;
import com.tcgm.dto.response.AffectationResponse;
import com.tcgm.model.Affectation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AffectationMapper {

    @Mapping(target = "chantier", expression = "java(mapChantier(affectation))")
    @Mapping(target = "ouvrier", expression = "java(mapOuvrier(affectation))")
    @Mapping(target = "rejectionReason", source = "rejectionReason")
    AffectationResponse toResponse(Affectation affectation);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chantier", ignore = true)
    @Mapping(target = "ouvrier", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "statut", expression = "java(com.tcgm.model.enums.StatutAffectation.valueOf(request.getStatut()))")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Affectation toEntity(AffectationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chantier", ignore = true)
    @Mapping(target = "ouvrier", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Affectation affectation, AffectationRequest request);

    List<AffectationResponse> toResponseList(List<Affectation> affectations);

    default AffectationResponse.ChantierBrief mapChantier(Affectation affectation) {
        if (affectation.getChantier() == null) return null;
        return AffectationResponse.ChantierBrief.builder()
            .id(affectation.getChantier().getId())
            .name(affectation.getChantier().getName())
            .reference(affectation.getChantier().getReference())
            .build();
    }

    default AffectationResponse.OuvrierBrief mapOuvrier(Affectation affectation) {
        if (affectation.getOuvrier() == null) return null;
        return AffectationResponse.OuvrierBrief.builder()
            .id(affectation.getOuvrier().getId())
            .firstName(affectation.getOuvrier().getFirstName())
            .lastName(affectation.getOuvrier().getLastName())
            .cin(affectation.getOuvrier().getCin())
            .specialite(affectation.getOuvrier().getSpecialite())
            .build();
    }
}