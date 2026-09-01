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

    Page<DossierPointage> findBySiteId(Long siteId, Pageable pageable);

    Page<DossierPointage> findByStatus(StatutPointage status, Pageable pageable);

    Optional<DossierPointage> findBySiteIdAndDate(Long siteId, LocalDate date);

    Page<DossierPointage> findBySiteIdAndStatus(Long siteId, StatutPointage status, Pageable pageable);

    long countBySiteIdAndStatus(Long siteId, StatutPointage status);

    @Query("SELECT d FROM DossierPointage d " +
           "WHERE d.date BETWEEN :startDate AND :endDate")
    List<DossierPointage> findDossiersByPeriod(@Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    @Query("SELECT d FROM DossierPointage d " +
           "WHERE d.site.id = :siteId AND d.date BETWEEN :startDate AND :endDate")
    List<DossierPointage> findDossiersBySiteAndPeriod(@Param("siteId") Long siteId,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);

    @Query("SELECT d FROM DossierPointage d " +
           "WHERE d.site.id = :siteId AND d.status = 'EN_ATTENTE'")
    List<DossierPointage> findPendingDossiersBySite(@Param("siteId") Long siteId);

    @Query("SELECT d.status, COUNT(d) FROM DossierPointage d GROUP BY d.status")
    List<Object[]> countDossiersByStatus();

    @Query("SELECT d.site.id, COUNT(d) FROM DossierPointage d GROUP BY d.site.id")
    List<Object[]> countDossiersBySite();

    @Query("SELECT d.status, COUNT(d) FROM DossierPointage d " +
           "WHERE d.site.id = :siteId GROUP BY d.status")
    List<Object[]> countDossiersByStatusForSite(@Param("siteId") Long siteId);

    @Query("SELECT d FROM DossierPointage d " +
           "WHERE d.site.id = :siteId ORDER BY d.date DESC")
    List<DossierPointage> findLastDossiersBySite(@Param("siteId") Long siteId, Pageable pageable);

    @Query("SELECT COUNT(d) > 0 FROM DossierPointage d " +
           "WHERE d.site.id = :siteId AND d.date = :date")
    boolean existsBySiteIdAndDate(@Param("siteId") Long siteId, @Param("date") LocalDate date);

    // ⚠️ NOUVEAU : requête unifiée avec scoping par rôle (siteIds = null →
    // pas de restriction). Remplace la logique de branchement manuel dans
    // le service pour getAllDossiersPointage.
    @Query("SELECT d FROM DossierPointage d WHERE " +
           "(:siteId IS NULL OR d.site.id = :siteId) AND " +
           "(:status IS NULL OR d.status = :status) AND " +
           "(:siteIds IS NULL OR d.site.id IN :siteIds)")
    Page<DossierPointage> findDossiersWithFilters(@Param("siteId") Long siteId,
                                                   @Param("status") StatutPointage status,
                                                   @Param("siteIds") List<Long> siteIds,
                                                   Pageable pageable);
}