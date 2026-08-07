package com.tcgm.repository;

import com.tcgm.model.LignePointage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LignePointageRepository extends JpaRepository<LignePointage, Long> {

    /**
     * Trouver les lignes par dossier de pointage
     */
    List<LignePointage> findByDossierId(Long dossierId);

    /**
     * Trouver les lignes par ouvrier
     */
    List<LignePointage> findByOuvrierId(Long ouvrierId);

    /**
     * Trouver les lignes par tâche
     */
    List<LignePointage> findByTacheId(Long tacheId);

    /**
     * Trouver les lignes par ouvrier et période
     */
    @Query("SELECT l FROM LignePointage l " +
           "WHERE l.ouvrier.id = :ouvrierId AND l.dossier.date BETWEEN :startDate AND :endDate")
    List<LignePointage> findLignesByOuvrierAndPeriod(@Param("ouvrierId") Long ouvrierId,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

    /**
     * Trouver les lignes par site et période
     */
    @Query("SELECT l FROM LignePointage l " +
           "WHERE l.dossier.site.id = :siteId AND l.dossier.date BETWEEN :startDate AND :endDate")
    List<LignePointage> findLignesBySiteAndPeriod(@Param("siteId") Long siteId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    /**
     * Compter les heures travaillées par ouvrier sur une période
     */
    @Query("SELECT SUM(TIMESTAMPDIFF(HOUR, l.startTime, l.endTime)) FROM LignePointage l " +
           "WHERE l.ouvrier.id = :ouvrierId AND l.dossier.date BETWEEN :startDate AND :endDate")
    Long sumHoursByOuvrierAndPeriod(@Param("ouvrierId") Long ouvrierId,
                                    @Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);

    /**
     * Compter les heures travaillées par site sur une période
     */
    @Query("SELECT SUM(TIMESTAMPDIFF(HOUR, l.startTime, l.endTime)) FROM LignePointage l " +
           "WHERE l.dossier.site.id = :siteId AND l.dossier.date BETWEEN :startDate AND :endDate")
    Long sumHoursBySiteAndPeriod(@Param("siteId") Long siteId,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);

    /**
     * Compter les heures travaillées par tâche sur une période
     */
    @Query("SELECT SUM(TIMESTAMPDIFF(HOUR, l.startTime, l.endTime)) FROM LignePointage l " +
           "WHERE l.tache.id = :tacheId AND l.dossier.date BETWEEN :startDate AND :endDate")
    Long sumHoursByTacheAndPeriod(@Param("tacheId") Long tacheId,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);

    /**
     * Trouver les lignes par ouvrier et date
     */
    @Query("SELECT l FROM LignePointage l " +
           "WHERE l.ouvrier.id = :ouvrierId AND l.dossier.date = :date")
    List<LignePointage> findLignesByOuvrierAndDate(@Param("ouvrierId") Long ouvrierId,
                                                    @Param("date") LocalDate date);

    /**
     * Compter les présences par ouvrier sur une période
     */
    @Query("SELECT COUNT(DISTINCT l.dossier.date) FROM LignePointage l " +
           "WHERE l.ouvrier.id = :ouvrierId AND l.dossier.date BETWEEN :startDate AND :endDate")
    Long countPresencesByOuvrierAndPeriod(@Param("ouvrierId") Long ouvrierId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);
}