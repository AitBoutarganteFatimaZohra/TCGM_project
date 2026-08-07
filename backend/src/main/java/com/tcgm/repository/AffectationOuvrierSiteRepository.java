package com.tcgm.repository;

import com.tcgm.model.AffectationOuvrierSite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AffectationOuvrierSiteRepository extends JpaRepository<AffectationOuvrierSite, Long> {

    /**
     * Trouver l'affectation active d'un ouvrier sur un site
     */
    @Query("SELECT a FROM AffectationOuvrierSite a " +
           "WHERE a.ouvrier.id = :ouvrierId AND a.site.id = :siteId AND a.active = true")
    Optional<AffectationOuvrierSite> findActiveAffectation(@Param("ouvrierId") Long ouvrierId,
                                                            @Param("siteId") Long siteId);

    /**
     * Trouver toutes les affectations d'un ouvrier
     */
    List<AffectationOuvrierSite> findByOuvrierId(Long ouvrierId);

    /**
     * Trouver les affectations actives d'un ouvrier
     */
    @Query("SELECT a FROM AffectationOuvrierSite a " +
           "WHERE a.ouvrier.id = :ouvrierId AND a.active = true")
    List<AffectationOuvrierSite> findActiveAffectationsByOuvrier(@Param("ouvrierId") Long ouvrierId);

    /**
     * Trouver les affectations actives d'un site
     */
    @Query("SELECT a FROM AffectationOuvrierSite a " +
           "WHERE a.site.id = :siteId AND a.active = true")
    List<AffectationOuvrierSite> findActiveAffectationsBySite(@Param("siteId") Long siteId);

    /**
     * Vérifier si un ouvrier est affecté à un site
     */
    @Query("SELECT COUNT(a) > 0 FROM AffectationOuvrierSite a " +
           "WHERE a.ouvrier.id = :ouvrierId AND a.site.id = :siteId AND a.active = true")
    boolean isOuvrierAffectedToSite(@Param("ouvrierId") Long ouvrierId,
                                    @Param("siteId") Long siteId);

    /**
     * Trouver les affectations qui se terminent bientôt
     */
    @Query("SELECT a FROM AffectationOuvrierSite a " +
           "WHERE a.endDate BETWEEN :today AND :soon AND a.active = true")
    List<AffectationOuvrierSite> findAffectationsEndingSoon(@Param("today") LocalDate today,
                                                             @Param("soon") LocalDate soon);

    /**
     * Trouver les affectations par période
     */
    @Query("SELECT a FROM AffectationOuvrierSite a " +
           "WHERE a.startDate >= :startDate AND a.startDate <= :endDate")
    List<AffectationOuvrierSite> findAffectationsByPeriod(@Param("startDate") LocalDate startDate,
                                                           @Param("endDate") LocalDate endDate);

    /**
     * Compter les ouvriers actifs sur un site
     */
    @Query("SELECT COUNT(a) FROM AffectationOuvrierSite a " +
           "WHERE a.site.id = :siteId AND a.active = true")
    long countActiveOuvriersBySite(@Param("siteId") Long siteId);

    /**
     * Compter les sites actifs d'un ouvrier
     */
    @Query("SELECT COUNT(a) FROM AffectationOuvrierSite a " +
           "WHERE a.ouvrier.id = :ouvrierId AND a.active = true")
    long countActiveSitesByOuvrier(@Param("ouvrierId") Long ouvrierId);
}