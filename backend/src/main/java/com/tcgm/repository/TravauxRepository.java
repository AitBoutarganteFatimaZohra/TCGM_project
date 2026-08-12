package com.tcgm.repository;

import com.tcgm.model.Travaux;
import com.tcgm.model.enums.StatutTravaux;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TravauxRepository extends JpaRepository<Travaux, Long> {

    Optional<Travaux> findByCode(String code);

    boolean existsByCode(String code);

    Page<Travaux> findByChantierId(Long chantierId, Pageable pageable);

    Page<Travaux> findByStatut(StatutTravaux statut, Pageable pageable);

    Page<Travaux> findByChantierIdAndStatut(Long chantierId, StatutTravaux statut, Pageable pageable);

    @Query("SELECT t FROM Travaux t WHERE t.chantier.id = :chantierId AND t.statut IN ('EN_COURS', 'PLANIFIE')")
    List<Travaux> findActiveTravauxByChantier(@Param("chantierId") Long chantierId);

    @Query("SELECT COUNT(t) FROM Travaux t WHERE t.chantier.id = :chantierId AND t.statut = 'TERMINE'")
    long countTerminesByChantier(@Param("chantierId") Long chantierId);
}