package com.tcgm.repository;

import com.tcgm.model.Ressource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RessourceRepository extends JpaRepository<Ressource, Long> {

    List<Ressource> findBySiteId(Long siteId);

    List<Ressource> findBySiteIdAndStatut(Long siteId, Ressource.StatutRessource statut);

    List<Ressource> findBySiteIdAndType(Long siteId, Ressource.TypeRessource type);

    List<Ressource> findBySiteIdAndNomContainingIgnoreCase(Long siteId, String nom);

    /**
     * ✅ CORRIGÉ — notifications : ressources avec une action en attente de
     * validation (niveau 1 ou niveau 2), sur un ensemble de sites donné.
     * L'entité Ressource n'a pas de champ "pendingStatut" — l'état
     * d'attente est porté par validationStatus (StatutValidationRessource),
     * qui vaut EN_ATTENTE_CHEF_CHANTIER ou EN_ATTENTE_CHEF_PROJET tant
     * qu'une action (création/modification/changement de statut/suppression)
     * n'a pas été tranchée.
     */
    @Query("""
        SELECT COUNT(r)
        FROM Ressource r
        WHERE r.site.id IN :siteIds
        AND r.validationStatus IN (
            com.tcgm.model.enums.StatutValidationRessource.EN_ATTENTE_CHEF_CHANTIER,
            com.tcgm.model.enums.StatutValidationRessource.EN_ATTENTE_CHEF_PROJET
        )
        """)
    long countPendingBySiteIds(@Param("siteIds") List<Long> siteIds);
}