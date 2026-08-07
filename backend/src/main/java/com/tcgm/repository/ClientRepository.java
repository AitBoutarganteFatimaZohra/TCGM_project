package com.tcgm.repository;

import com.tcgm.model.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    /**
     * Trouver un client par son nom
     */
    Optional<Client> findByName(String name);

    /**
     * Vérifier si un client existe par son nom
     */
    boolean existsByName(String name);

    /**
     * Trouver un client par son email
     */
    Optional<Client> findByEmail(String email);

    /**
     * Vérifier si un email existe déjà
     */
    boolean existsByEmail(String email);

    /**
     * Trouver les clients par recherche (nom, contact, email)
     */
    @Query("SELECT c FROM Client c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.contact) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Client> searchClients(@Param("search") String search, Pageable pageable);

    /**
     * Trouver les clients avec leurs sites
     */
    @Query("SELECT DISTINCT c FROM Client c LEFT JOIN FETCH c.sites")
    List<Client> findAllWithSites();

    /**
     * Trouver un client avec ses sites
     */
    @Query("SELECT c FROM Client c LEFT JOIN FETCH c.sites WHERE c.id = :id")
    Optional<Client> findByIdWithSites(@Param("id") Long id);

    /**
     * Trouver les clients par ICE
     */
    Optional<Client> findByIce(String ice);

    /**
     * Trouver les clients par RC
     */
    Optional<Client> findByRc(String rc);

    /**
     * Compter le nombre de sites par client
     */
    @Query("SELECT c.id, COUNT(s) FROM Client c LEFT JOIN c.sites s GROUP BY c.id")
    List<Object[]> countSitesPerClient();

    /**
     * Trouver les clients avec au moins un site actif
     */
    @Query("SELECT DISTINCT c FROM Client c JOIN c.sites s WHERE s.status = 'EN_COURS'")
    List<Client> findClientsWithActiveSites();
}