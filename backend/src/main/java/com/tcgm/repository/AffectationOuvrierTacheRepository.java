package com.tcgm.repository;

import com.tcgm.model.AffectationOuvrierTache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AffectationOuvrierTacheRepository extends JpaRepository<AffectationOuvrierTache, Long> {

    /**
     * Trouver les affectations d'un ouvrier
     */
    List<AffectationOuvrierTache> findByOuvrierId(Long ouvrierId);

    /**
     * Trouver les affectations d'une tâche
     */
    List<AffectationOuvrierTache> findByTacheId(Long tacheId);

    /**
     * Trouver une affectation spécifique
     */
    Optional<AffectationOuvrierTache> findByOuvrierIdAndTacheId(Long ouvrierId, Long tacheId);

    /**
     * Vérifier si un ouvrier est affecté à une tâche
     */
    @Query("SELECT COUNT(a) > 0 FROM AffectationOuvrierTache a " +
           "WHERE a.ouvrier.id = :ouvrierId AND a.tache.id = :tacheId")
    boolean isOuvrierAffectedToTache(@Param("ouvrierId") Long ouvrierId,
                                     @Param("tacheId") Long tacheId);

    /**
     * Compter les ouvriers affectés à une tâche
     */
    @Query("SELECT COUNT(a) FROM AffectationOuvrierTache a WHERE a.tache.id = :tacheId")
    long countOuvriersByTache(@Param("tacheId") Long tacheId);

    /**
     * Compter les tâches d'un ouvrier
     */
    @Query("SELECT COUNT(a) FROM AffectationOuvrierTache a WHERE a.ouvrier.id = :ouvrierId")
    long countTachesByOuvrier(@Param("ouvrierId") Long ouvrierId);
}