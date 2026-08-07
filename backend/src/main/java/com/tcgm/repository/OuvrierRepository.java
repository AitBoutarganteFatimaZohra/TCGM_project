package com.tcgm.repository;

import com.tcgm.model.Ouvrier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OuvrierRepository extends JpaRepository<Ouvrier, Long> {

    // =========================================================
    // RECHERCHES DE BASE
    // =========================================================

    /**
     * Trouver un ouvrier par son CIN
     */
    Optional<Ouvrier> findByCin(String cin);

    /**
     * Vérifier si un CIN existe déjà
     */
    boolean existsByCin(String cin);

    /**
     * Trouver les ouvriers par spécialité
     */
    List<Ouvrier> findBySpecialite(String specialite);

    /**
     * Trouver les ouvriers actifs
     */
    List<Ouvrier> findByActiveTrue();

    /**
     * Trouver les ouvriers inactifs
     */
    List<Ouvrier> findByActiveFalse();

    // =========================================================
    // RECHERCHES PAR SITE
    // =========================================================

    /**
     * Trouver les ouvriers affectés à un site
     */
    @Query("SELECT DISTINCT o FROM Ouvrier o JOIN o.affectationsSites a " +
           "WHERE a.site.id = :siteId AND a.active = true")
    Page<Ouvrier> findOuvriersBySite(@Param("siteId") Long siteId, Pageable pageable);

    /**
     * Trouver les ouvriers actifs affectés à un site
     */
    @Query("SELECT DISTINCT o FROM Ouvrier o JOIN o.affectationsSites a " +
           "WHERE a.site.id = :siteId AND o.active = true AND a.active = true")
    List<Ouvrier> findActiveOuvriersBySite(@Param("siteId") Long siteId);

    /**
     * Trouver les ouvriers non affectés à un site
     */
    @Query("SELECT o FROM Ouvrier o WHERE o.id NOT IN " +
           "(SELECT a.ouvrier.id FROM AffectationOuvrierSite a " +
           "WHERE a.site.id = :siteId AND a.active = true)")
    Page<Ouvrier> findOuvriersNotAffectedToSite(@Param("siteId") Long siteId, Pageable pageable);

    // =========================================================
    // RECHERCHES AVEC FILTRES
    // =========================================================

    /**
     * Recherche d'ouvriers avec filtres
     */
    @Query("SELECT o FROM Ouvrier o WHERE " +
           "(:specialite IS NULL OR o.specialite = :specialite) AND " +
           "(:active IS NULL OR o.active = :active) AND " +
           "(:search IS NULL OR LOWER(o.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(o.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(o.cin) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Ouvrier> findOuvriersWithFilters(@Param("specialite") String specialite,
                                          @Param("active") Boolean active,
                                          @Param("search") String search,
                                          Pageable pageable);

    /**
     * Recherche d'ouvriers par site avec filtres
     */
    @Query("SELECT DISTINCT o FROM Ouvrier o JOIN o.affectationsSites a " +
           "WHERE a.site.id = :siteId AND a.active = true AND " +
           "(:specialite IS NULL OR o.specialite = :specialite) AND " +
           "(:search IS NULL OR LOWER(o.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(o.lastName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Ouvrier> findOuvriersBySiteWithFilters(@Param("siteId") Long siteId,
                                                 @Param("specialite") String specialite,
                                                 @Param("search") String search,
                                                 Pageable pageable);

    // =========================================================
    // STATISTIQUES
    // =========================================================

    /**
     * Compter les ouvriers par spécialité
     */
    @Query("SELECT o.specialite, COUNT(o) FROM Ouvrier o GROUP BY o.specialite")
    List<Object[]> countOuvriersBySpecialite();

    /**
     * Compter les ouvriers actifs
     */
    @Query("SELECT COUNT(o) FROM Ouvrier o WHERE o.active = true")
    long countActiveOuvriers();

    /**
     * Compter les ouvriers par statut
     */
    @Query("SELECT o.active, COUNT(o) FROM Ouvrier o GROUP BY o.active")
    List<Object[]> countOuvriersByStatus();

    /**
     * Compter les ouvriers affectés à un site
     */
    @Query("SELECT COUNT(DISTINCT o) FROM Ouvrier o JOIN o.affectationsSites a " +
           "WHERE a.site.id = :siteId AND a.active = true")
    long countOuvriersBySite(@Param("siteId") Long siteId);

    /**
     * Compter les ouvriers actifs affectés à un site
     */
    @Query("SELECT COUNT(DISTINCT o) FROM Ouvrier o JOIN o.affectationsSites a " +
           "WHERE a.site.id = :siteId AND a.active = true AND o.active = true")
    long countActiveOuvriersBySite(@Param("siteId") Long siteId);

    /**
     * Compter les ouvriers par spécialité sur un site
     */
    @Query("SELECT o.specialite, COUNT(DISTINCT o) FROM Ouvrier o JOIN o.affectationsSites a " +
           "WHERE a.site.id = :siteId AND a.active = true GROUP BY o.specialite")
    List<Object[]> countOuvriersBySpecialiteOnSite(@Param("siteId") Long siteId);

    /**
     * Trouver les 10 spécialités les plus courantes
     */
    @Query("SELECT o.specialite, COUNT(o) FROM Ouvrier o " +
           "WHERE o.specialite IS NOT NULL GROUP BY o.specialite " +
           "ORDER BY COUNT(o) DESC")
    List<Object[]> findTopSpecialites(Pageable pageable);
}