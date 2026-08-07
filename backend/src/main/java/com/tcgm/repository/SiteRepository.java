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

    /**
     * Trouver les sites par statut
     */
    Page<Site> findByStatus(StatutSite status, Pageable pageable);

    /**
     * Trouver les sites par client
     */
    Page<Site> findByClientId(Long clientId, Pageable pageable);

    /**
     * Trouver les sites par statut et client
     */
    Page<Site> findByStatusAndClientId(StatutSite status, Long clientId, Pageable pageable);

    /**
     * Trouver un site par sa référence
     */
    Optional<Site> findByReference(String reference);

    /**
     * Vérifier si une référence existe
     */
    boolean existsByReference(String reference);

    // =========================================================
    // RECHERCHES PAR RESPONSABLE
    // =========================================================

    /**
     * Trouver les sites dont l'utilisateur est chef de projet
     */
    @Query("SELECT s FROM Site s WHERE s.chefProjet.id = :userId")
    List<Site> findByChefProjetId(@Param("userId") Long userId);

    /**
     * Trouver les sites dont l'utilisateur est chef de chantier
     */
    @Query("SELECT s FROM Site s WHERE s.chefChantier.id = :userId")
    List<Site> findByChefChantierId(@Param("userId") Long userId);

    /**
     * Trouver les sites dont l'utilisateur est magasinier
     */
    @Query("SELECT s FROM Site s WHERE s.magasinier.id = :userId")
    List<Site> findByMagasinierId(@Param("userId") Long userId);

    /**
     * Trouver les sites dont l'utilisateur est agent de saisie
     */
    @Query("SELECT s FROM Site s WHERE s.agentSaisie.id = :userId")
    List<Site> findByAgentSaisieId(@Param("userId") Long userId);

    /**
     * Trouver les sites où l'utilisateur a un rôle (projet, chantier, magasinier, saisie)
     */
    @Query("SELECT DISTINCT s FROM Site s WHERE s.chefProjet.id = :userId " +
           "OR s.chefChantier.id = :userId " +
           "OR s.magasinier.id = :userId " +
           "OR s.agentSaisie.id = :userId")
    List<Site> findSitesByUserId(@Param("userId") Long userId);

    // =========================================================
    // RECHERCHES AVEC PÉRIODE
    // =========================================================

    /**
     * Trouver les sites avec des dates de début/fin dans une période
     */
    @Query("SELECT s FROM Site s WHERE s.startDate BETWEEN :start AND :end " +
           "OR s.endDate BETWEEN :start AND :end")
    List<Site> findSitesInPeriod(@Param("start") LocalDateTime start, 
                                 @Param("end") LocalDateTime end);

    /**
     * Trouver les sites qui se terminent bientôt (dans les 7 jours)
     */
    @Query("SELECT s FROM Site s WHERE s.endDate BETWEEN :today AND :soon")
    List<Site> findSitesEndingSoon(@Param("today") LocalDateTime today, 
                                   @Param("soon") LocalDateTime soon);

    /**
     * Trouver les sites en cours
     */
    @Query("SELECT s FROM Site s WHERE s.status = 'EN_COURS'")
    List<Site> findActiveSites();

    // =========================================================
    // STATISTIQUES
    // =========================================================

    /**
     * Compter les sites par statut
     */
    @Query("SELECT s.status, COUNT(s) FROM Site s GROUP BY s.status")
    List<Object[]> countSitesByStatus();

    /**
     * Compter les sites par client
     */
    @Query("SELECT c.name, COUNT(s) FROM Site s JOIN s.client c GROUP BY c.id, c.name")
    List<Object[]> countSitesByClient();

    /**
     * Compter le nombre total de sites par statut
     */
    @Query("SELECT COUNT(s) FROM Site s WHERE s.status = :status")
    Long countByStatus(@Param("status") StatutSite status);

    // =========================================================
    // RECHERCHES AVANCÉES
    // =========================================================

    /**
     * Recherche de sites avec filtres multiples
     */
    @Query("SELECT s FROM Site s WHERE " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:clientId IS NULL OR s.client.id = :clientId) AND " +
           "(:search IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(s.reference) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Site> findSitesWithFilters(@Param("status") StatutSite status,
                                    @Param("clientId") Long clientId,
                                    @Param("search") String search,
                                    Pageable pageable);

    /**
     * Trouver les sites avec leurs relations (fetch eager)
     */
    @Query("SELECT s FROM Site s LEFT JOIN FETCH s.client " +
           "LEFT JOIN FETCH s.chefProjet " +
           "LEFT JOIN FETCH s.magasinier " +
           "LEFT JOIN FETCH s.agentSaisie " +
           "LEFT JOIN FETCH s.chefChantier " +
           "WHERE s.id = :id")
    Optional<Site> findByIdWithRelations(@Param("id") Long id);

    /**
     * Trouver les sites avec leurs tâches
     */
    @Query("SELECT s FROM Site s LEFT JOIN FETCH s.taches WHERE s.id = :id")
    Optional<Site> findByIdWithTaches(@Param("id") Long id);

    /**
     * Trouver les sites avec leurs affectations d'ouvriers
     */
    @Query("SELECT s FROM Site s LEFT JOIN FETCH s.affectationsOuvriers WHERE s.id = :id")
    Optional<Site> findByIdWithOuvriers(@Param("id") Long id);
}