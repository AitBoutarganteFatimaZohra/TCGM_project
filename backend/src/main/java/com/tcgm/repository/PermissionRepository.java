package com.tcgm.repository;

import com.tcgm.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * Trouver une permission par son nom
     */
    Optional<Permission> findByName(String name);

    /**
     * Vérifier si une permission existe
     */
    boolean existsByName(String name);

    /**
     * Trouver les permissions par liste de noms
     */
    List<Permission> findByNameIn(List<String> names);

    /**
     * Trouver les permissions non assignées à un rôle
     */
    @Query("SELECT p FROM Permission p WHERE p.id NOT IN " +
           "(SELECT rp.id FROM Role r JOIN r.permissions rp WHERE r.id = :roleId)")
    List<Permission> findPermissionsNotAssignedToRole(@Param("roleId") Long roleId);

    /**
     * Trouver les permissions assignées à un rôle
     */
    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.id = :roleId")
    List<Permission> findPermissionsByRoleId(@Param("roleId") Long roleId);
}