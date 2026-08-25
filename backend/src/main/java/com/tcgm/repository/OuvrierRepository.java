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

    Optional<Ouvrier> findByCin(String cin);

    boolean existsByCin(String cin);

    List<Ouvrier> findBySpecialite(String specialite);

    List<Ouvrier> findByActiveTrue();

    List<Ouvrier> findByActiveFalse();


    // =========================================================
    // RECHERCHES PAR CHANTIER
    // =========================================================

    @Query("""
        SELECT DISTINCT o
        FROM Ouvrier o
        JOIN o.affectations a
        WHERE a.chantier.id = :chantierId
        AND a.statut = com.tcgm.model.enums.StatutAffectation.EN_COURS
        """)
    Page<Ouvrier> findOuvriersByChantier(
            @Param("chantierId") Long chantierId,
            Pageable pageable
    );


    @Query("""
        SELECT DISTINCT o
        FROM Ouvrier o
        JOIN o.affectations a
        WHERE a.chantier.id = :chantierId
        AND a.statut = com.tcgm.model.enums.StatutAffectation.EN_COURS
        AND o.active = true
        """)
    List<Ouvrier> findActiveOuvriersByChantier(
            @Param("chantierId") Long chantierId
    );


    @Query("""
        SELECT o
        FROM Ouvrier o
        WHERE o.id NOT IN (
            SELECT a.ouvrier.id
            FROM Affectation a
            WHERE a.chantier.id = :chantierId
            AND a.statut = com.tcgm.model.enums.StatutAffectation.EN_COURS
        )
        """)
    Page<Ouvrier> findOuvriersNotAffectedToChantier(
            @Param("chantierId") Long chantierId,
            Pageable pageable
    );


    // =========================================================
    // RECHERCHES AVEC FILTRES
    // =========================================================

    @Query("""
        SELECT o
        FROM Ouvrier o
        WHERE (:specialite IS NULL OR o.specialite = :specialite)
        AND (:active IS NULL OR o.active = :active)
        AND (
            :search IS NULL
            OR LOWER(o.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(o.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(o.cin) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        """)
    Page<Ouvrier> findOuvriersWithFilters(
            @Param("specialite") String specialite,
            @Param("active") Boolean active,
            @Param("search") String search,
            Pageable pageable
    );


    @Query("""
        SELECT DISTINCT o
        FROM Ouvrier o
        JOIN o.affectations a
        WHERE a.chantier.id = :chantierId
        AND a.statut = com.tcgm.model.enums.StatutAffectation.EN_COURS
        AND (:specialite IS NULL OR o.specialite = :specialite)
        AND (
            :search IS NULL
            OR LOWER(o.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(o.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        """)
    Page<Ouvrier> findOuvriersByChantierWithFilters(
            @Param("chantierId") Long chantierId,
            @Param("specialite") String specialite,
            @Param("search") String search,
            Pageable pageable
    );


    // =========================================================
    // RECHERCHES AVEC AFFECTATIONS
    // =========================================================

    @Query("""
        SELECT DISTINCT o
        FROM Ouvrier o
        LEFT JOIN FETCH o.affectations
        WHERE o.id = :id
        """)
    Optional<Ouvrier> findByIdWithAffectations(
            @Param("id") Long id
    );


    @Query("""
        SELECT DISTINCT o
        FROM Ouvrier o
        LEFT JOIN FETCH o.affectations a
        LEFT JOIN FETCH a.chantier
        WHERE o.id = :id
        """)
    Optional<Ouvrier> findByIdWithAll(
            @Param("id") Long id
    );


    @Query("""
        SELECT DISTINCT o
        FROM Ouvrier o
        JOIN o.affectations a
        WHERE a.statut = com.tcgm.model.enums.StatutAffectation.EN_COURS
        """)
    List<Ouvrier> findOuvriersWithAffectationEnCours();


    @Query("""
        SELECT DISTINCT o
        FROM Ouvrier o
        JOIN o.affectations a
        WHERE a.chantier.id = :chantierId
        AND a.statut = com.tcgm.model.enums.StatutAffectation.EN_COURS
        """)
    List<Ouvrier> findOuvriersWithAffectationEnCoursByChantier(
            @Param("chantierId") Long chantierId
    );


    @Query("""
        SELECT COUNT(a) > 0
        FROM Affectation a
        WHERE a.ouvrier.id = :ouvrierId
        AND a.statut = com.tcgm.model.enums.StatutAffectation.EN_COURS
        """)
    boolean hasAffectationEnCours(
            @Param("ouvrierId") Long ouvrierId
    );


    // =========================================================
    // ✅ NOUVEAU : OUVRIERS DISPONIBLES POUR UNE NOUVELLE AFFECTATION
    // =========================================================

    /**
     * Ouvriers actifs n'ayant AUCUNE affectation EN_COURS actuellement —
     * donc libres à assigner. Contrairement à findOuvriersByChantier* qui
     * montre "l'équipe déjà sur place", celle-ci sert au formulaire de
     * création d'affectation, où l'on cherche justement quelqu'un de
     * disponible (nouvel ouvrier, ou ouvrier dont la précédente
     * affectation vient de se terminer/être annulée).
     */
    @Query("""
        SELECT o
        FROM Ouvrier o
        WHERE o.active = true
        AND o.id NOT IN (
            SELECT a.ouvrier.id
            FROM Affectation a
            WHERE a.statut = com.tcgm.model.enums.StatutAffectation.EN_COURS
        )
        AND (
            :search IS NULL
            OR LOWER(o.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(o.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(o.cin) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        """)
    Page<Ouvrier> findOuvriersDisponibles(
            @Param("search") String search,
            Pageable pageable
    );


    // =========================================================
    // STATISTIQUES
    // =========================================================

    @Query("""
        SELECT o.specialite, COUNT(o)
        FROM Ouvrier o
        GROUP BY o.specialite
        """)
    List<Object[]> countOuvriersBySpecialite();


    @Query("""
        SELECT COUNT(o)
        FROM Ouvrier o
        WHERE o.active = true
        """)
    long countActiveOuvriers();


    @Query("""
        SELECT o.active, COUNT(o)
        FROM Ouvrier o
        GROUP BY o.active
        """)
    List<Object[]> countOuvriersByStatus();


    // =========================================================
    // STATISTIQUES PAR CHANTIER
    // =========================================================

    @Query("""
        SELECT COUNT(DISTINCT o)
        FROM Ouvrier o
        JOIN o.affectations a
        WHERE a.chantier.id = :chantierId
        AND a.statut = com.tcgm.model.enums.StatutAffectation.EN_COURS
        """)
    long countOuvriersByChantier(
            @Param("chantierId") Long chantierId
    );


    @Query("""
        SELECT COUNT(DISTINCT o)
        FROM Ouvrier o
        JOIN o.affectations a
        WHERE a.chantier.id = :chantierId
        AND a.statut = com.tcgm.model.enums.StatutAffectation.EN_COURS
        AND o.active = true
        """)
    long countActiveOuvriersByChantier(
            @Param("chantierId") Long chantierId
    );


    @Query("""
        SELECT o.specialite, COUNT(DISTINCT o)
        FROM Ouvrier o
        JOIN o.affectations a
        WHERE a.chantier.id = :chantierId
        AND a.statut = com.tcgm.model.enums.StatutAffectation.EN_COURS
        GROUP BY o.specialite
        """)
    List<Object[]> countOuvriersBySpecialiteOnChantier(
            @Param("chantierId") Long chantierId
    );


    // =========================================================
    // TOP SPÉCIALITÉS
    // =========================================================

    @Query("""
        SELECT o.specialite, COUNT(o)
        FROM Ouvrier o
        WHERE o.specialite IS NOT NULL
        GROUP BY o.specialite
        ORDER BY COUNT(o) DESC
        """)
    List<Object[]> findTopSpecialites(Pageable pageable);


    @Query("""
        SELECT DISTINCT o
        FROM Ouvrier o
        JOIN o.affectations a
        WHERE a.chantier.id IN :chantierIds
        AND a.statut = com.tcgm.model.enums.StatutAffectation.EN_COURS
        """)
    Page<Ouvrier> findOuvriersByChantierIn(
            @Param("chantierIds") List<Long> chantierIds,
            Pageable pageable
    );

    @Query("""
        SELECT DISTINCT o
        FROM Ouvrier o
        JOIN o.affectations a
        WHERE a.chantier.id IN :chantierIds
        AND a.statut = com.tcgm.model.enums.StatutAffectation.EN_COURS
        AND (:specialite IS NULL OR o.specialite = :specialite)
        AND (
            :search IS NULL
            OR LOWER(o.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(o.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        """)
    Page<Ouvrier> findOuvriersByChantierInWithFilters(
            @Param("chantierIds") List<Long> chantierIds,
            @Param("specialite") String specialite,
            @Param("search") String search,
            Pageable pageable
    );
}