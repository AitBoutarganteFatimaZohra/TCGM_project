package com.tcgm.repository;

import com.tcgm.model.User;
import com.tcgm.model.enums.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Trouver un utilisateur par son email
     */
    Optional<User> findByEmail(String email);

    /**
     * Vérifier si un email existe déjà
     */
    boolean existsByEmail(String email);

    /**
     * Trouver les utilisateurs par rôle
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") RoleName roleName);

    /**
     * Trouver les utilisateurs par rôle avec pagination
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Page<User> findByRoleName(@Param("roleName") RoleName roleName, Pageable pageable);

    /**
     * Trouver les utilisateurs actifs
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true")
    List<User> findEnabledUsers();

    /**
     * Trouver les utilisateurs par nom ou email
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);

    /**
     * Compter les utilisateurs par rôle
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    long countByRoleName(@Param("roleName") RoleName roleName);

    /**
     * Trouver les utilisateurs créés après une date
     */
    @Query("SELECT u FROM User u WHERE u.createdAt >= :date")
    List<User> findUsersCreatedAfter(@Param("date") java.time.LocalDateTime date);

    /**
     * Trouver les utilisateurs par nom complet
     */
    @Query("SELECT u FROM User u WHERE LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :fullName, '%'))")
    List<User> findByFullNameContaining(@Param("fullName") String fullName);

    /**
     * Trouver les utilisateurs avec leurs rôles (fetch eager)
     */
    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.id = :id")
    Optional<User> findByIdWithRoles(@Param("id") Long id);

    /**
     * Trouver les utilisateurs qui n'ont jamais été connectés
     */
    @Query("SELECT u FROM User u WHERE u.lastLogin IS NULL")
    List<User> findNeverLoggedInUsers();

    /**
     * Trouver les utilisateurs par leurs permissions
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r JOIN r.permissions p WHERE p.name = :permissionName")
    List<User> findByPermissionName(@Param("permissionName") String permissionName);
}