package com.tcgm.repository;

import com.tcgm.model.Affectation;
import com.tcgm.model.enums.StatutAffectation;
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
public interface AffectationRepository extends JpaRepository<Affectation, Long> {

    Page<Affectation> findByChantierId(Long chantierId, Pageable pageable);

    Page<Affectation> findByOuvrierId(Long ouvrierId, Pageable pageable);

    Page<Affectation> findByStatut(StatutAffectation statut, Pageable pageable);

    @Query("SELECT a FROM Affectation a WHERE a.ouvrier.id = :ouvrierId AND a.statut = 'EN_COURS'")
    Optional<Affectation> findAffectationEnCoursByOuvrier(@Param("ouvrierId") Long ouvrierId);

    @Query("SELECT a FROM Affectation a WHERE a.chantier.id = :chantierId AND a.statut = 'EN_COURS'")
    List<Affectation> findAffectationsEnCoursByChantier(@Param("chantierId") Long chantierId);

    @Query("SELECT COUNT(a) > 0 FROM Affectation a WHERE a.ouvrier.id = :ouvrierId AND a.statut = 'EN_COURS'")
    boolean hasAffectationEnCours(@Param("ouvrierId") Long ouvrierId);

    long countByChantierIdAndStatut(Long chantierId, StatutAffectation statut);

    long countByOuvrierIdAndStatut(Long ouvrierId, StatutAffectation statut);
}