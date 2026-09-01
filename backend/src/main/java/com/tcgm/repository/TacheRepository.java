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

    Page<Tache> findByStatus(StatutTache status, Pageable pageable);

    Page<Tache> findByTravauxId(Long travauxId, Pageable pageable);

    List<Tache> findByTravauxIdAndStatus(Long travauxId, StatutTache status);

    @Query("SELECT t FROM Tache t LEFT JOIN FETCH t.affectationsOuvriers a " +
           "LEFT JOIN FETCH a.ouvrier WHERE t.travaux.id = :travauxId")
    List<Tache> findTachesByTravauxWithOuvriers(@Param("travauxId") Long travauxId);

    @Query("SELECT COUNT(t) FROM Tache t WHERE t.travaux.chantier.id = :siteId")
    long countByChantierId(@Param("siteId") Long siteId);

    @Query("SELECT COUNT(t) FROM Tache t WHERE t.travaux.chantier.id = :siteId AND t.status = 'TERMINEE'")
    long countCompletedTachesByChantier(@Param("siteId") Long siteId);

    @Query("SELECT t.status, COUNT(t) FROM Tache t WHERE t.travaux.chantier.id = :siteId GROUP BY t.status")
    List<Object[]> countTachesByStatusForChantier(@Param("siteId") Long siteId);

    @Query("SELECT t FROM Tache t WHERE t.travaux.chantier.id = :siteId")
    List<Tache> findAllByChantierId(@Param("siteId") Long siteId);

    // 🔧 CORRIGÉ : ajout du paramètre chantierIds pour le scoping par rôle
    // (null = pas de restriction, comportement inchangé pour l'Admin)
    @Query("SELECT t FROM Tache t WHERE " +
           "(:travauxId IS NULL OR t.travaux.id = :travauxId) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:chantierIds IS NULL OR t.travaux.chantier.id IN :chantierIds)")
    Page<Tache> findTachesWithFilters(@Param("travauxId") Long travauxId,
                                      @Param("status") StatutTache status,
                                      @Param("search") String search,
                                      @Param("chantierIds") List<Long> chantierIds,
                                      Pageable pageable);

    // ⚠️ NOUVEAU : ID du chantier d'une tâche donnée, pour vérification
    // d'appartenance sans charger toute l'entité.
    @Query("SELECT t.travaux.chantier.id FROM Tache t WHERE t.id = :id")
    Long findChantierIdByTacheId(@Param("id") Long id);

    @Query("SELECT t FROM Tache t WHERE t.plannedDate BETWEEN :start AND :end")
    List<Tache> findTachesStartingBetween(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);

    @Query("SELECT t FROM Tache t WHERE t.plannedDate < :now AND t.status != 'TERMINEE'")
    List<Tache> findOverdueTaches(@Param("now") LocalDateTime now);

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

    @Query("SELECT COUNT(t) FROM Tache t WHERE t.travaux.chantier.id IN :chantierIds " +
           "AND (t.proposedStatus IS NOT NULL OR t.proposedPlannedDate IS NOT NULL)")
    long countPendingByChantierIds(@Param("chantierIds") List<Long> chantierIds);
}