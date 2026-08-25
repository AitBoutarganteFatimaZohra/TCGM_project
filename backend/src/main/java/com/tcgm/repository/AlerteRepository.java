package com.tcgm.repository;

import com.tcgm.model.Alerte;
import com.tcgm.model.enums.StatutAlerte;
import com.tcgm.model.enums.TypeAlerte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlerteRepository extends JpaRepository<Alerte, Long> {

    /**
     * Utilisé par le job de détection pour savoir si une alerte de ce type
     * est déjà active pour ce site (évite les doublons).
     */
    Optional<Alerte> findBySiteIdAndTypeAndStatut(Long siteId, TypeAlerte type, StatutAlerte statut);

    /**
     * Toutes les alertes actives d'un statut donné (tous sites confondus).
     */
    List<Alerte> findByStatut(StatutAlerte statut);

    /**
     * Alertes actives pour les sites dont l'utilisateur est Chef de Projet.
     */
    @Query("SELECT a FROM Alerte a WHERE a.site.chefProjet.id = :userId AND a.statut = :statut " +
           "ORDER BY a.createdAt DESC")
    List<Alerte> findBySiteChefProjetIdAndStatut(@Param("userId") Long userId,
                                                  @Param("statut") StatutAlerte statut);

    /**
     * Alertes actives pour les sites dont l'utilisateur est Chef de Chantier
     * (utile si on étend le dashboard Chef de Chantier plus tard).
     */
    @Query("SELECT a FROM Alerte a WHERE a.site.chefChantier.id = :userId AND a.statut = :statut " +
           "ORDER BY a.createdAt DESC")
    List<Alerte> findBySiteChefChantierIdAndStatut(@Param("userId") Long userId,
                                                    @Param("statut") StatutAlerte statut);
}