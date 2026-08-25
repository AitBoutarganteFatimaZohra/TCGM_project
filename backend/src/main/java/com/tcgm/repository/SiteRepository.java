package com.tcgm.repository;

import com.tcgm.model.Site;
import com.tcgm.model.enums.StatutSite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long>, JpaSpecificationExecutor<Site> {

    // =========================================================
    // RECHERCHES DE BASE
    // =========================================================

    Page<Site> findByStatus(StatutSite status, Pageable pageable);

    Page<Site> findByClientId(Long clientId, Pageable pageable);

    Page<Site> findByStatusAndClientId(StatutSite status, Long clientId, Pageable pageable);

    Optional<Site> findByReference(String reference);

    boolean existsByReference(String reference);

    // =========================================================
    // RECHERCHES PAR RESPONSABLE
    // =========================================================

    @Query("SELECT s FROM Site s WHERE s.chefProjet.id = :userId")
    List<Site> findByChefProjetId(@Param("userId") Long userId);

    @Query("SELECT s FROM Site s WHERE s.chefChantier.id = :userId")
    List<Site> findByChefChantierId(@Param("userId") Long userId);

    @Query("SELECT s FROM Site s WHERE s.magasinier.id = :userId")
    List<Site> findByMagasinierId(@Param("userId") Long userId);

    @Query("SELECT s FROM Site s WHERE s.agentSaisie.id = :userId")
    List<Site> findByAgentSaisieId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT s FROM Site s WHERE s.chefProjet.id = :userId " +
           "OR s.chefChantier.id = :userId " +
           "OR s.magasinier.id = :userId " +
           "OR s.agentSaisie.id = :userId")
    List<Site> findSitesByUserId(@Param("userId") Long userId);

    // =========================================================
    // RECHERCHES DE BASE AVEC ID UNIQUEMENT (pour les selects)
    // =========================================================

    @Query("SELECT s.id FROM Site s WHERE s.chefChantier.id = :userId")
    List<Long> findIdsByChefChantierId(@Param("userId") Long userId);

    /**
     * Trouver les IDs des sites dont l'utilisateur est agent de saisie.
     * 🔧 CORRIGÉ : remplacé par une @Query explicite avec projection s.id.
     * L'ancienne version en requête dérivée par nom de méthode
     * (`List<Long> findIdsByAgentSaisieId(Long)`) était mal interprétée
     * par Spring Data / Hibernate, qui retournait des entités Site
     * complètes au lieu de Long → QueryTypeMismatchException ("Result
     * type is 'Long' but the query returned a 'Site'") → 500 sur
     * /api/statistiques/dashboard pour le rôle AGENT_SAISIE.
     */
    @Query("SELECT s.id FROM Site s WHERE s.agentSaisie.id = :agentSaisieId")
    List<Long> findIdsByAgentSaisieId(@Param("agentSaisieId") Long agentSaisieId);

    // =========================================================
    // RECHERCHES AVEC PÉRIODE
    // =========================================================

    @Query("SELECT s FROM Site s WHERE s.startDate BETWEEN :start AND :end " +
           "OR s.endDate BETWEEN :start AND :end")
    List<Site> findSitesInPeriod(@Param("start") LocalDateTime start, 
                                 @Param("end") LocalDateTime end);

    @Query("SELECT s FROM Site s WHERE s.endDate BETWEEN :today AND :soon")
    List<Site> findSitesEndingSoon(@Param("today") LocalDateTime today, 
                                   @Param("soon") LocalDateTime soon);

    @Query("SELECT s FROM Site s WHERE s.status = 'EN_COURS'")
    List<Site> findActiveSites();

    // =========================================================
    // STATISTIQUES
    // =========================================================

    @Query("SELECT s.status, COUNT(s) FROM Site s GROUP BY s.status")
    List<Object[]> countSitesByStatus();

    @Query("SELECT c.name, COUNT(s) FROM Site s JOIN s.client c GROUP BY c.id, c.name")
    List<Object[]> countSitesByClient();

    @Query("SELECT COUNT(s) FROM Site s WHERE s.status = :status")
    Long countByStatus(@Param("status") StatutSite status);

    // =========================================================
    // RECHERCHES AVANCÉES
    // =========================================================

    @Query("SELECT s FROM Site s WHERE " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:clientId IS NULL OR s.client.id = :clientId) AND " +
           "(:search IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(s.reference) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:periodStart IS NULL OR s.endDate IS NULL OR s.endDate >= :periodStart) AND " +
           "(:periodEnd IS NULL OR s.startDate IS NULL OR s.startDate <= :periodEnd) AND " +
           "(:responsableId IS NULL OR " +
           "(s.chefProjet.id = :responsableId OR s.chefChantier.id = :responsableId " +
           "OR s.magasinier.id = :responsableId OR s.agentSaisie.id = :responsableId))")
    Page<Site> findSitesWithFilters(@Param("status") StatutSite status,
                                    @Param("clientId") Long clientId,
                                    @Param("search") String search,
                                    @Param("periodStart") LocalDateTime periodStart,
                                    @Param("periodEnd") LocalDateTime periodEnd,
                                    @Param("responsableId") Long responsableId,
                                    Pageable pageable);

    // =========================================================
    // NOUVELLES MÉTHODES POUR TRAVAUX ET AFFECTATION
    // =========================================================

    @Query("SELECT s FROM Site s LEFT JOIN FETCH s.client " +
           "LEFT JOIN FETCH s.chefProjet " +
           "LEFT JOIN FETCH s.magasinier " +
           "LEFT JOIN FETCH s.agentSaisie " +
           "LEFT JOIN FETCH s.chefChantier " +
           "WHERE s.id = :id")
    Optional<Site> findByIdWithRelations(@Param("id") Long id);

    @Query("SELECT s FROM Site s LEFT JOIN FETCH s.travaux WHERE s.id = :id")
    Optional<Site> findByIdWithTravaux(@Param("id") Long id);

    @Query("SELECT s FROM Site s LEFT JOIN FETCH s.affectations WHERE s.id = :id")
    Optional<Site> findByIdWithAffectations(@Param("id") Long id);

    @Query("SELECT s FROM Site s LEFT JOIN FETCH s.travaux t LEFT JOIN FETCH t.taches WHERE s.id = :id")
    Optional<Site> findByIdWithAllRelations(@Param("id") Long id);

    @Query("SELECT s FROM Site s " +
           "LEFT JOIN FETCH s.travaux t " +
           "LEFT JOIN FETCH t.taches " +
           "LEFT JOIN FETCH s.affectations a " +
           "LEFT JOIN FETCH a.ouvrier " +
           "WHERE s.id = :id")
    Optional<Site> findByIdWithAll(@Param("id") Long id);

    @Query("SELECT DISTINCT s FROM Site s LEFT JOIN FETCH s.travaux t LEFT JOIN FETCH t.taches")
    List<Site> findAllWithTravauxAndTaches();

}