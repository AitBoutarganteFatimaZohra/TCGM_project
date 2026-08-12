package com.tcgm.mapper;

import com.tcgm.dto.request.UserCreateRequest;
import com.tcgm.dto.request.UserUpdateRequest;
import com.tcgm.dto.response.UserResponse;
import com.tcgm.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // =========================================================
    // ENTITY → RESPONSE
    // =========================================================

    @Mapping(target = "roles", expression = "java(getRoleNames(user))")
    @Mapping(target = "lastLogin", source = "lastLogin")
    UserResponse toResponse(User user);

    // =========================================================
    // REQUEST → ENTITY
    // =========================================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "sitesAsChefProjet", ignore = true)
    @Mapping(target = "sitesAsMagasinier", ignore = true)
    @Mapping(target = "sitesAsAgentSaisie", ignore = true)
    @Mapping(target = "sitesAsChefChantier", ignore = true)
    @Mapping(target = "enabled", expression = "java(request.getEnabled() != null ? request.getEnabled() : true)")
    User toEntity(UserCreateRequest request);

    // =========================================================
    // UPDATE
    // =========================================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true) // Email ne doit pas être modifiable
    @Mapping(target = "password", ignore = true) // Mot de passe modifié séparément
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "sitesAsChefProjet", ignore = true)
    @Mapping(target = "sitesAsMagasinier", ignore = true)
    @Mapping(target = "sitesAsAgentSaisie", ignore = true)
    @Mapping(target = "sitesAsChefChantier", ignore = true)
    void updateEntity(@MappingTarget User user, UserUpdateRequest request);

    // =========================================================
    // LISTES
    // =========================================================

    List<UserResponse> toResponseList(List<User> users);

    // =========================================================
    // MÉTHODES UTILITAIRES (implémentées par défaut)
    // =========================================================

    default Set<String> getRoleNames(User user) {
        if (user.getRoles() == null) return null;
        return user.getRoles().stream()
            .map(role -> role.getName().name())
            .collect(Collectors.toSet());
    }
}