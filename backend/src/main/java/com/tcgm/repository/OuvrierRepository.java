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
    // RECHERCHES PAR CHANTIER
    // =========================================================

    /**
     * Trouver les ouvriers affectés à un chantier
     * avec une affectation EN_COURS.
     */
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


    /**
     * Trouver les ouvriers actifs affectés à un chantier.
     */
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


    /**
     * Trouver les ouvriers non affectés à un chantier.
     */
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

    /**
     * Recherche d'ouvriers avec filtres.
     */
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


    /**
     * Recherche d'ouvriers par chantier avec filtres.
     */
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

    /**
     * Trouver un ouvrier avec ses affectations.
     */
    @Query("""
        SELECT DISTINCT o
        FROM Ouvrier o
        LEFT JOIN FETCH o.affectations
        WHERE o.id = :id
        """)
    Optional<Ouvrier> findByIdWithAffectations(
            @Param("id") Long id
    );


    /**
     * Trouver un ouvrier avec ses affectations et les chantiers.
     */
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


    /**
     * Trouver les ouvriers ayant une affectation en cours.
     */
    @Query("""
        SELECT DISTINCT o
        FROM Ouvrier o
        JOIN o.affectations a
        WHERE a.statut = com.tcgm.model.enums.StatutAffectation.EN_COURS
        """)
    List<Ouvrier> findOuvriersWithAffectationEnCours();


    /**
     * Trouver les ouvriers ayant une affectation en cours
     * sur un chantier spécifique.
     */
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


    /**
     * Vérifier si un ouvrier possède une affectation en cours.
     */
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
    // STATISTIQUES
    // =========================================================

    /**
     * Compter les ouvriers par spécialité.
     */
    @Query("""
        SELECT o.specialite, COUNT(o)
        FROM Ouvrier o
        GROUP BY o.specialite
        """)
    List<Object[]> countOuvriersBySpecialite();


    /**
     * Compter les ouvriers actifs.
     */
    @Query("""
        SELECT COUNT(o)
        FROM Ouvrier o
        WHERE o.active = true
        """)
    long countActiveOuvriers();


    /**
     * Compter les ouvriers par statut actif/inactif.
     */
    @Query("""
        SELECT o.active, COUNT(o)
        FROM Ouvrier o
        GROUP BY o.active
        """)
    List<Object[]> countOuvriersByStatus();


    // =========================================================
    // STATISTIQUES PAR CHANTIER
    // =========================================================

    /**
     * Compter les ouvriers affectés à un chantier.
     */
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


    /**
     * Compter les ouvriers actifs affectés à un chantier.
     */
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


    /**
     * Compter les ouvriers par spécialité sur un chantier.
     */
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

    /**
     * Trouver les spécialités les plus courantes.
     */
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