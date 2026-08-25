package com.tcgm.repository;

import com.tcgm.model.Ressource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RessourceRepository extends JpaRepository<Ressource, Long> {

    List<Ressource> findBySiteId(Long siteId);

    List<Ressource> findBySiteIdAndStatut(Long siteId, Ressource.StatutRessource statut);

    List<Ressource> findBySiteIdAndType(Long siteId, Ressource.TypeRessource type);

    List<Ressource> findBySiteIdAndNomContainingIgnoreCase(Long siteId, String nom);
}