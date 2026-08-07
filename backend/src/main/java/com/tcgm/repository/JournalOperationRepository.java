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

    /**
     * Trouver les opérations par utilisateur
     */
    Page<JournalOperation> findByUserId(Long userId, Pageable pageable);

    /**
     * Trouver les opérations par type d'action
     */
    Page<JournalOperation> findByActionType(TypeAction actionType, Pageable pageable);

    /**
     * Trouver les opérations par type d'entité
     */
    Page<JournalOperation> findByEntityType(String entityType, Pageable pageable);

    /**
     * Trouver les opérations par entité spécifique
     */
    Page<JournalOperation> findByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);

    /**
     * Trouver les opérations par période
     */
    @Query("SELECT j FROM JournalOperation j " +
           "WHERE j.createdAt BETWEEN :startDate AND :endDate")
    Page<JournalOperation> findOperationsByPeriod(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate,
                                                   Pageable pageable);

    /**
     * Recherche avancée avec filtres
     */
    @Query("SELECT j FROM JournalOperation j WHERE " +
           "(:actionType IS NULL OR j.actionType = :actionType) AND " +
           "(:entityType IS NULL OR j.entityType = :entityType) AND " +
           "(:entityId IS NULL OR j.entityId = :entityId) AND " +
           "(:userId IS NULL OR j.user.id = :userId) AND " +
           "(:search IS NULL OR LOWER(j.details) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<JournalOperation> findOperationsWithFilters(
            @Param("actionType") TypeAction actionType,
            @Param("entityType") String entityType,
            @Param("entityId") Long entityId,
            @Param("userId") Long userId,
            @Param("search") String search,
            Pageable pageable);

    /**
     * Trouver les opérations par période avec filtres
     */
    @Query("SELECT j FROM JournalOperation j WHERE " +
           "(:actionType IS NULL OR j.actionType = :actionType) AND " +
           "(:entityType IS NULL OR j.entityType = :entityType) AND " +
           "j.createdAt BETWEEN :startDate AND :endDate")
    Page<JournalOperation> findOperationsWithPeriodAndFilters(
            @Param("actionType") TypeAction actionType,
            @Param("entityType") String entityType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Compter les opérations par type d'action
     */
    @Query("SELECT j.actionType, COUNT(j) FROM JournalOperation j GROUP BY j.actionType")
    List<Object[]> countOperationsByActionType();

    /**
     * Compter les opérations par type d'entité
     */
    @Query("SELECT j.entityType, COUNT(j) FROM JournalOperation j GROUP BY j.entityType")
    List<Object[]> countOperationsByEntityType();

    /**
     * Compter les opérations par jour
     */
    @Query("SELECT DATE(j.createdAt), COUNT(j) FROM JournalOperation j " +
           "WHERE j.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(j.createdAt)")
    List<Object[]> countOperationsByDay(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    /**
     * Compter les opérations par utilisateur
     */
    @Query("SELECT j.user.id, COUNT(j) FROM JournalOperation j " +
           "WHERE j.user IS NOT NULL GROUP BY j.user.id ORDER BY COUNT(j) DESC")
    List<Object[]> countOperationsByUser();

    /**
     * Trouver les dernières opérations
     */
    @Query("SELECT j FROM JournalOperation j ORDER BY j.createdAt DESC")
    List<JournalOperation> findLastOperations(Pageable pageable);

    /**
     * Compter les opérations par statut (pour validation)
     */
    @Query("SELECT COUNT(j) FROM JournalOperation j WHERE j.actionType = 'VALIDATION'")
    long countValidationOperations();

    /**
     * Trouver les opérations d'un type d'entité spécifique sur une période
     */
    @Query("SELECT j FROM JournalOperation j " +
           "WHERE j.entityType = :entityType AND j.createdAt BETWEEN :startDate AND :endDate")
    List<JournalOperation> findOperationsByEntityTypeAndPeriod(
            @Param("entityType") String entityType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Vérifier si des opérations existent pour une entité
     */
    @Query("SELECT COUNT(j) > 0 FROM JournalOperation j " +
           "WHERE j.entityType = :entityType AND j.entityId = :entityId")
    boolean existsOperationsForEntity(@Param("entityType") String entityType,
                                      @Param("entityId") Long entityId);
}