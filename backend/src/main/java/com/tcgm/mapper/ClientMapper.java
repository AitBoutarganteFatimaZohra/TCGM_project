package com.tcgm.mapper;

import com.tcgm.dto.request.ClientRequest;
import com.tcgm.dto.response.ClientResponse;
import com.tcgm.model.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    // =========================================================
    // ENTITY → RESPONSE
    // =========================================================

    @Mapping(target = "totalSites", expression = "java(client.getSites() != null ? client.getSites().size() : 0)")
    ClientResponse toResponse(Client client);

    // =========================================================
    // REQUEST → ENTITY
    // =========================================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "sites", ignore = true)
    Client toEntity(ClientRequest request);

    // =========================================================
    // UPDATE
    // =========================================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "sites", ignore = true)
    void updateEntity(@MappingTarget Client client, ClientRequest request);

    // =========================================================
    // LISTES
    // =========================================================

    List<ClientResponse> toResponseList(List<Client> clients);
}