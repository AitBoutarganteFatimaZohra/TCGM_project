package com.tcgm.repository;

import com.tcgm.model.DossierPointage;
import com.tcgm.model.enums.StatutPointage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DossierPointageRepository extends JpaRepository<DossierPointage, Long> {

    /**
     * Trouver les dossiers de pointage par site
     */
    Page<DossierPointage> findBySiteId(Long siteId, Pageable pageable);

    /**
     * Trouver les dossiers de pointage par statut
     */
    Page<DossierPointage> findByStatus(StatutPointage status, Pageable pageable);

    /**
     * Trouver les dossiers de pointage par site et date
     */
    Optional<DossierPointage> findBySiteIdAndDate(Long siteId, LocalDate date);

    /**
     * Trouver les dossiers de pointage par site et statut (Pagination)
     */
    Page<DossierPointage> findBySiteIdAndStatus(Long siteId, StatutPointage status, Pageable pageable);

    /**
     * Compter les dossiers de pointage par site et statut
     */
    long countBySiteIdAndStatus(Long siteId, StatutPointage status);

    /**
     * Trouver les dossiers de pointage par période
     */
    @Query("SELECT d FROM DossierPointage d " +
           "WHERE d.date BETWEEN :startDate AND :endDate")
    List<DossierPointage> findDossiersByPeriod(@Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    /**
     * Trouver les dossiers de pointage par site et période
     */
    @Query("SELECT d FROM DossierPointage d " +
           "WHERE d.site.id = :siteId AND d.date BETWEEN :startDate AND :endDate")
    List<DossierPointage> findDossiersBySiteAndPeriod(@Param("siteId") Long siteId,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);

    /**
     * Trouver les dossiers de pointage en attente de validation pour un site
     */
    @Query("SELECT d FROM DossierPointage d " +
           "WHERE d.site.id = :siteId AND d.status = 'EN_ATTENTE'")
    List<DossierPointage> findPendingDossiersBySite(@Param("siteId") Long siteId);

    /**
     * Compter les dossiers de pointage par statut
     */
    @Query("SELECT d.status, COUNT(d) FROM DossierPointage d GROUP BY d.status")
    List<Object[]> countDossiersByStatus();

    /**
     * Compter les dossiers de pointage par site
     */
    @Query("SELECT d.site.id, COUNT(d) FROM DossierPointage d GROUP BY d.site.id")
    List<Object[]> countDossiersBySite();

    /**
     * Compter les dossiers de pointage par statut pour un site
     */
    @Query("SELECT d.status, COUNT(d) FROM DossierPointage d " +
           "WHERE d.site.id = :siteId GROUP BY d.status")
    List<Object[]> countDossiersByStatusForSite(@Param("siteId") Long siteId);

    /**
     * Trouver le dernier dossier de pointage pour un site
     */
    @Query("SELECT d FROM DossierPointage d " +
           "WHERE d.site.id = :siteId ORDER BY d.date DESC")
    List<DossierPointage> findLastDossiersBySite(@Param("siteId") Long siteId, Pageable pageable);

    /**
     * Vérifier si un dossier de pointage existe pour une date et un site
     */
    @Query("SELECT COUNT(d) > 0 FROM DossierPointage d " +
           "WHERE d.site.id = :siteId AND d.date = :date")
    boolean existsBySiteIdAndDate(@Param("siteId") Long siteId, @Param("date") LocalDate date);
}