package com.tcgm.repository;

import com.tcgm.model.Tache;
import com.tcgm.model.enums.StatutTache;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TacheRepository extends JpaRepository<Tache, Long> {

    // =========================================================
    // RECHERCHES DE BASE
    // =========================================================

    /**
     * Trouver les tâches par statut
     */
    Page<Tache> findByStatus(StatutTache status, Pageable pageable);

    /**
     * Trouver les tâches par site
     */
    Page<Tache> findBySiteId(Long siteId, Pageable pageable);

    /**
     * Trouver les tâches par priorité
     */
    List<Tache> findByPriority(Integer priority);

    // =========================================================
    // RECHERCHES AVEC FILTRES
    // =========================================================

    /**
     * Recherche de tâches avec filtres
     */
    @Query("SELECT t FROM Tache t WHERE " +
           "(:siteId IS NULL OR t.site.id = :siteId) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Tache> findTachesWithFilters(@Param("siteId") Long siteId,
                                      @Param("status") StatutTache status,
                                      @Param("search") String search,
                                      Pageable pageable);

    /**
     * Trouver les tâches par site et statut
     */
    List<Tache> findBySiteIdAndStatus(Long siteId, StatutTache status);

    // =========================================================
    // RECHERCHES PAR DATE
    // =========================================================

    /**
     * Trouver les tâches qui doivent commencer bientôt
     */
    @Query("SELECT t FROM Tache t WHERE t.plannedDate BETWEEN :start AND :end")
    List<Tache> findTachesStartingBetween(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);

    /**
     * Trouver les tâches en retard
     */
    @Query("SELECT t FROM Tache t WHERE t.plannedDate < :now AND t.status != 'TERMINEE'")
    List<Tache> findOverdueTaches(@Param("now") LocalDateTime now);

    // =========================================================
    // STATISTIQUES
    // =========================================================

    /**
     * Compter les tâches par statut pour un site
     */
    @Query("SELECT t.status, COUNT(t) FROM Tache t WHERE t.site.id = :siteId GROUP BY t.status")
    List<Object[]> countTachesByStatusForSite(@Param("siteId") Long siteId);

    /**
     * Compter les tâches par statut global
     */
    @Query("SELECT t.status, COUNT(t) FROM Tache t GROUP BY t.status")
    List<Object[]> countTachesByStatus();

    /**
     * Compter les tâches par priorité
     */
    @Query("SELECT t.priority, COUNT(t) FROM Tache t GROUP BY t.priority")
    List<Object[]> countTachesByPriority();

    /**
     * Compter les tâches terminées pour un site
     */
    @Query("SELECT COUNT(t) FROM Tache t WHERE t.site.id = :siteId AND t.status = 'TERMINEE'")
    long countCompletedTachesBySite(@Param("siteId") Long siteId);

    /**
     * Calculer le nombre total de tâches par site
     */
    @Query("SELECT t.site.id, COUNT(t) FROM Tache t GROUP BY t.site.id")
    List<Object[]> countTachesBySite();

    // =========================================================
    // RECHERCHES AVANCÉES
    // =========================================================

    /**
     * Trouver les tâches affectées à un ouvrier
     */
    @Query("SELECT t FROM Tache t JOIN t.affectationsOuvriers a WHERE a.ouvrier.id = :ouvrierId")
    List<Tache> findTachesByOuvrierId(@Param("ouvrierId") Long ouvrierId);

    /**
     * Trouver les tâches avec leurs relations
     */
    @Query("SELECT t FROM Tache t LEFT JOIN FETCH t.affectationsOuvriers a " +
           "LEFT JOIN FETCH a.ouvrier WHERE t.id = :id")
    Tache findByIdWithOuvriers(@Param("id") Long id);

    /**
     * Trouver les tâches d'un site avec leurs relations
     */
    @Query("SELECT t FROM Tache t LEFT JOIN FETCH t.affectationsOuvriers a " +
           "LEFT JOIN FETCH a.ouvrier WHERE t.site.id = :siteId")
    List<Tache> findTachesBySiteWithOuvriers(@Param("siteId") Long siteId);
}