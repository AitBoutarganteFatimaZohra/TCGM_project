package com.tcgm.repository;

import com.tcgm.model.JournalOperation;
import com.tcgm.model.enums.TypeAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JournalOperationRepository extends JpaRepository<JournalOperation, Long> {

    // 🔧 CORRIGÉ : suffixe OrderByCreatedAtDesc ajouté (requêtes dérivées
    // par nom de méthode → tri appliqué automatiquement par Spring Data,
    // sans dépendre du Pageable envoyé par le frontend).
    Page<JournalOperation> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<JournalOperation> findByActionTypeOrderByCreatedAtDesc(TypeAction actionType, Pageable pageable);

    Page<JournalOperation> findByEntityTypeOrderByCreatedAtDesc(String entityType, Pageable pageable);

    Page<JournalOperation> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId, Pageable pageable);

    // 🔧 CORRIGÉ : ORDER BY ajouté.
    @Query("SELECT j FROM JournalOperation j " +
           "WHERE j.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY j.createdAt DESC")
    Page<JournalOperation> findOperationsByPeriod(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate,
                                                   Pageable pageable);

    // Vision globale (ADMIN uniquement) : aucune restriction sur l'auteur.
    // 🔧 CORRIGÉ : ORDER BY ajouté — c'est LA requête la plus utilisée
    // (tous les rôles hors export passent par ici ou par les deux
    // suivantes), donc la correction la plus impactante du fichier.
    @Query("SELECT j FROM JournalOperation j WHERE " +
           "(:actionType IS NULL OR j.actionType = :actionType) AND " +
           "(:entityType IS NULL OR j.entityType = :entityType) AND " +
           "(:entityId IS NULL OR j.entityId = :entityId) AND " +
           "(:userId IS NULL OR j.user.id = :userId) AND " +
           "(:search IS NULL OR LOWER(j.details) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY j.createdAt DESC")
    Page<JournalOperation> findOperationsWithFilters(
            @Param("actionType") TypeAction actionType,
            @Param("entityType") String entityType,
            @Param("entityId") Long entityId,
            @Param("userId") Long userId,
            @Param("search") String search,
            Pageable pageable);

    // Filtre restreint à un site donné (Magasinier / Agent de Saisie via
    // computeAllowedUserIdsForSite).
    // 🔧 CORRIGÉ : ORDER BY ajouté.
    @Query("SELECT j FROM JournalOperation j WHERE " +
           "(:actionType IS NULL OR j.actionType = :actionType) AND " +
           "(:entityType IS NULL OR j.entityType = :entityType) AND " +
           "(:entityId IS NULL OR j.entityId = :entityId) AND " +
           "(:userId IS NULL OR j.user.id = :userId) AND " +
           "(:search IS NULL OR LOWER(j.details) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "( (j.entityType = 'SITE' AND j.entityId = :siteId) OR " +
           "  (j.entityType = 'RESSOURCE' AND j.entityId IN " +
           "     (SELECT r.id FROM Ressource r WHERE r.site.id = :siteId)) ) " +
           "ORDER BY j.createdAt DESC")
    Page<JournalOperation> findOperationsWithFiltersAndSite(
            @Param("actionType") TypeAction actionType,
            @Param("entityType") String entityType,
            @Param("entityId") Long entityId,
            @Param("userId") Long userId,
            @Param("search") String search,
            @Param("siteId") Long siteId,
            Pageable pageable);

    // Vision restreinte à un périmètre d'utilisateurs (Chef de Projet /
    // Chef de Chantier / Agent de Saisie / Magasinier).
    // 🔧 CORRIGÉ : ORDER BY ajouté — c'est la requête utilisée pour TOUS
    // les rôles non-admin, y compris le nouveau journal de l'Agent de
    // Saisie : c'était la plus importante à corriger pour ta demande.
    @Query("SELECT j FROM JournalOperation j WHERE " +
           "(:actionType IS NULL OR j.actionType = :actionType) AND " +
           "(:entityType IS NULL OR j.entityType = :entityType) AND " +
           "(:entityId IS NULL OR j.entityId = :entityId) AND " +
           "(:userId IS NULL OR j.user.id = :userId) AND " +
           "(:search IS NULL OR LOWER(j.details) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "j.user.id IN :allowedUserIds " +
           "ORDER BY j.createdAt DESC")
    Page<JournalOperation> findOperationsWithFiltersForUsers(
            @Param("actionType") TypeAction actionType,
            @Param("entityType") String entityType,
            @Param("entityId") Long entityId,
            @Param("userId") Long userId,
            @Param("search") String search,
            @Param("allowedUserIds") List<Long> allowedUserIds,
            Pageable pageable);

    @Query("SELECT j.actionType, COUNT(j) FROM JournalOperation j GROUP BY j.actionType")
    List<Object[]> countOperationsByActionType();

    @Query("SELECT j.entityType, COUNT(j) FROM JournalOperation j GROUP BY j.entityType")
    List<Object[]> countOperationsByEntityType();

    @Query("SELECT DATE(j.createdAt), COUNT(j) FROM JournalOperation j " +
           "WHERE j.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(j.createdAt)")
    List<Object[]> countOperationsByDay(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT j.user.id, COUNT(j) FROM JournalOperation j " +
           "WHERE j.user IS NOT NULL GROUP BY j.user.id ORDER BY COUNT(j) DESC")
    List<Object[]> countOperationsByUser();

    @Query("SELECT j FROM JournalOperation j ORDER BY j.createdAt DESC")
    List<JournalOperation> findLastOperations(Pageable pageable);

    @Query("SELECT COUNT(j) FROM JournalOperation j WHERE j.actionType = 'VALIDATION'")
    long countValidationOperations();

    @Query("SELECT j FROM JournalOperation j " +
           "WHERE j.entityType = :entityType AND j.createdAt BETWEEN :startDate AND :endDate")
    List<JournalOperation> findOperationsByEntityTypeAndPeriod(
            @Param("entityType") String entityType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(j) > 0 FROM JournalOperation j " +
           "WHERE j.entityType = :entityType AND j.entityId = :entityId")
    boolean existsOperationsForEntity(@Param("entityType") String entityType,
                                      @Param("entityId") Long entityId);

    // Liste complète (non paginée) pour générer l'export PDF/Excel —
    // avait déjà le bon ORDER BY, inchangé.
    @Query("SELECT j FROM JournalOperation j WHERE " +
           "(:entityType IS NULL OR j.entityType = :entityType) AND " +
           "(:startDate IS NULL OR j.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR j.createdAt <= :endDate) " +
           "ORDER BY j.createdAt DESC")
    List<JournalOperation> findForExport(@Param("entityType") String entityType,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);
}