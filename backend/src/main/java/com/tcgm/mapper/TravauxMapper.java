package com.tcgm.mapper;

import com.tcgm.dto.request.TravauxRequest;
import com.tcgm.dto.response.TravauxResponse;
import com.tcgm.model.Travaux;
import com.tcgm.model.enums.StatutTravaux;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TravauxMapper {

    @Mapping(target = "chantier", expression = "java(mapChantier(travaux))")
    @Mapping(target = "taches", expression = "java(mapTaches(travaux.getTaches()))")
    @Mapping(target = "totalTaches", expression = "java(travaux.getTaches() != null ? travaux.getTaches().size() : 0)")
    @Mapping(target = "totalTachesTerminees", expression = "java(countTachesTerminees(travaux))")
    TravauxResponse toResponse(Travaux travaux);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chantier", ignore = true)
    @Mapping(target = "taches", ignore = true)
    @Mapping(target = "statut", expression = "java(com.tcgm.model.enums.StatutTravaux.valueOf(request.getStatut()))")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Travaux toEntity(TravauxRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chantier", ignore = true)
    @Mapping(target = "taches", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Travaux travaux, TravauxRequest request);

    List<TravauxResponse> toResponseList(List<Travaux> travaux);

    default TravauxResponse.ChantierBrief mapChantier(Travaux travaux) {
        if (travaux.getChantier() == null) return null;
        return TravauxResponse.ChantierBrief.builder()
            .id(travaux.getChantier().getId())
            .name(travaux.getChantier().getName())
            .reference(travaux.getChantier().getReference())
            .build();
    }

    default List<TravauxResponse.TacheBrief> mapTaches(List<com.tcgm.model.Tache> taches) {
        if (taches == null) return null;
        return taches.stream()
            .map(t -> TravauxResponse.TacheBrief.builder()
                .id(t.getId())
                .title(t.getTitle())
                .status(t.getStatus() != null ? t.getStatus().name() : null)
                .priority(t.getPriority())
                .build())
            .collect(java.util.stream.Collectors.toList());
    }

    default int countTachesTerminees(Travaux travaux) {
        if (travaux.getTaches() == null) return 0;
        return (int) travaux.getTaches().stream()
            .filter(t -> t.getStatus() == com.tcgm.model.enums.StatutTache.TERMINEE)
            .count();
    }
}