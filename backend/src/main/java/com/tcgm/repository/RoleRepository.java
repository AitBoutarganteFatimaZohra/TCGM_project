package com.tcgm.repository;

import com.tcgm.model.Role;
import com.tcgm.model.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Trouver un rôle par son nom
     */
    Optional<Role> findByName(RoleName name);

    /**
     * Trouver un rôle par son nom (String)
     */
    @Query("SELECT r FROM Role r WHERE r.name = :name")
    Optional<Role> findByName(@Param("name") String name);

    /**
     * Vérifier si un rôle existe
     */
    boolean existsByName(RoleName name);

    /**
     * Trouver tous les rôles avec leurs permissions
     */
    @Query("SELECT r FROM Role r JOIN FETCH r.permissions")
    List<Role> findAllWithPermissions();

    /**
     * Trouver un rôle avec ses permissions
     */
    @Query("SELECT r FROM Role r JOIN FETCH r.permissions WHERE r.id = :id")
    Optional<Role> findByIdWithPermissions(@Param("id") Long id);

    /**
     * Trouver un rôle avec ses permissions par nom
     */
    @Query("SELECT r FROM Role r JOIN FETCH r.permissions WHERE r.name = :name")
    Optional<Role> findByNameWithPermissions(@Param("name") RoleName name);

    /**
     * Compter le nombre d'utilisateurs ayant ce rôle
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.id = :roleId")
    long countUsersByRoleId(@Param("roleId") Long roleId);
}