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

    Page<Tache> findByStatus(StatutTache status, Pageable pageable);

    // =========================================================
    // RECHERCHES PAR SITE (SUPPRIMÉES)
    // =========================================================

    // SUPPRIMER : Page<Tache> findBySiteId(Long siteId, Pageable pageable);
    // SUPPRIMER : List<Tache> findBySiteIdAndStatus(Long siteId, StatutTache status);
    // SUPPRIMER : countCompletedTachesBySite()
    // SUPPRIMER : countTachesByStatusForSite()
    // SUPPRIMER : countTachesBySite()
    // SUPPRIMER : findTachesWithFilters() avec siteId
    // SUPPRIMER : findTachesBySiteWithOuvriers()

    // =========================================================
    // NOUVELLES RECHERCHES PAR TRAVAUX
    // =========================================================

    Page<Tache> findByTravauxId(Long travauxId, Pageable pageable);

    List<Tache> findByTravauxIdAndStatus(Long travauxId, StatutTache status);

    @Query("SELECT t FROM Tache t LEFT JOIN FETCH t.affectationsOuvriers a " +
           "LEFT JOIN FETCH a.ouvrier WHERE t.travaux.id = :travauxId")
    List<Tache> findTachesByTravauxWithOuvriers(@Param("travauxId") Long travauxId);

    // =========================================================
    // ⚠️ CORRECTIF : RECHERCHES PAR SITE/CHANTIER (via travaux.chantier.id)
    // Un site peut avoir PLUSIEURS Travaux, il ne faut donc jamais
    // supposer que travauxId == siteId. Ces méthodes remontent la
    // relation Tache -> Travaux -> Site correctement.
    // =========================================================

    @Query("SELECT COUNT(t) FROM Tache t WHERE t.travaux.chantier.id = :siteId")
    long countByChantierId(@Param("siteId") Long siteId);

    @Query("SELECT COUNT(t) FROM Tache t WHERE t.travaux.chantier.id = :siteId AND t.status = 'TERMINEE'")
    long countCompletedTachesByChantier(@Param("siteId") Long siteId);

    @Query("SELECT t.status, COUNT(t) FROM Tache t WHERE t.travaux.chantier.id = :siteId GROUP BY t.status")
    List<Object[]> countTachesByStatusForChantier(@Param("siteId") Long siteId);

    @Query("SELECT t FROM Tache t WHERE t.travaux.chantier.id = :siteId")
    List<Tache> findAllByChantierId(@Param("siteId") Long siteId);

    // =========================================================
    // RECHERCHES AVEC FILTRES
    // =========================================================

    @Query("SELECT t FROM Tache t WHERE " +
           "(:travauxId IS NULL OR t.travaux.id = :travauxId) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Tache> findTachesWithFilters(@Param("travauxId") Long travauxId,
                                      @Param("status") StatutTache status,
                                      @Param("search") String search,
                                      Pageable pageable);

    // =========================================================
    // RECHERCHES PAR DATE
    // =========================================================

    @Query("SELECT t FROM Tache t WHERE t.plannedDate BETWEEN :start AND :end")
    List<Tache> findTachesStartingBetween(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);

    @Query("SELECT t FROM Tache t WHERE t.plannedDate < :now AND t.status != 'TERMINEE'")
    List<Tache> findOverdueTaches(@Param("now") LocalDateTime now);

    // =========================================================
    // STATISTIQUES
    // =========================================================

    @Query("SELECT t.status, COUNT(t) FROM Tache t WHERE t.travaux.id = :travauxId GROUP BY t.status")
    List<Object[]> countTachesByStatusForTravaux(@Param("travauxId") Long travauxId);

    @Query("SELECT t.status, COUNT(t) FROM Tache t GROUP BY t.status")
    List<Object[]> countTachesByStatus();

    @Query("SELECT t.priority, COUNT(t) FROM Tache t GROUP BY t.priority")
    List<Object[]> countTachesByPriority();

    @Query("SELECT COUNT(t) FROM Tache t WHERE t.travaux.id = :travauxId AND t.status = 'TERMINEE'")
    long countCompletedTachesByTravaux(@Param("travauxId") Long travauxId);

    @Query("SELECT t.travaux.id, COUNT(t) FROM Tache t GROUP BY t.travaux.id")
    List<Object[]> countTachesByTravaux();

    // =========================================================
    // RECHERCHES AVANCÉES
    // =========================================================

    @Query("SELECT t FROM Tache t JOIN t.affectationsOuvriers a WHERE a.ouvrier.id = :ouvrierId")
    List<Tache> findTachesByOuvrierId(@Param("ouvrierId") Long ouvrierId);

    @Query("SELECT t FROM Tache t LEFT JOIN FETCH t.affectationsOuvriers a " +
           "LEFT JOIN FETCH a.ouvrier WHERE t.id = :id")
    Tache findByIdWithOuvriers(@Param("id") Long id);

    @Query("SELECT t FROM Tache t LEFT JOIN FETCH t.travaux tr " +
           "LEFT JOIN FETCH tr.chantier " +
           "WHERE t.id = :id")
    Tache findByIdWithTravauxAndChantier(@Param("id") Long id);

    @Query("SELECT t FROM Tache t LEFT JOIN FETCH t.affectationsOuvriers a " +
           "LEFT JOIN FETCH a.ouvrier WHERE t.travaux.id = :travauxId")
    List<Tache> findTachesByTravauxWithAffectations(@Param("travauxId") Long travauxId);
}