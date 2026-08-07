package com.tcgm.service.impl;

import com.tcgm.repository.*;
import com.tcgm.service.StatistiqueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatistiqueServiceImpl implements StatistiqueService {

    private final SiteRepository siteRepository;
    private final OuvrierRepository ouvrierRepository;
    private final TacheRepository tacheRepository;
    private final DossierPointageRepository dossierRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    @Override
    public Map<String, Object> getDashboardStats() {
        log.debug("Récupération des statistiques du dashboard");

        Map<String, Object> stats = new HashMap<>();

        // Statistiques générales
        stats.put("totalSites", siteRepository.count());
        stats.put("totalOuvriers", ouvrierRepository.count());
        stats.put("totalTaches", tacheRepository.count());
        stats.put("totalClients", clientRepository.count());
        stats.put("totalUsers", userRepository.count());
        stats.put("totalPointages", dossierRepository.count());

        // Ouvriers actifs
        stats.put("ouvriersActifs", ouvrierRepository.countActiveOuvriers());

        // Statistiques par statut de site
        var sitesByStatus = siteRepository.countSitesByStatus();
        Map<String, Long> sitesStats = new HashMap<>();
        for (Object[] row : sitesByStatus) {
            sitesStats.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("sitesByStatus", sitesStats);

        // Statistiques par statut de tâche
        var tachesByStatus = tacheRepository.countTachesByStatus();
        Map<String, Long> tachesStats = new HashMap<>();
        for (Object[] row : tachesByStatus) {
            tachesStats.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("tachesByStatus", tachesStats);

        // Statistiques par statut de pointage
        var pointagesByStatus = dossierRepository.countDossiersByStatus();
        Map<String, Long> pointagesStats = new HashMap<>();
        for (Object[] row : pointagesByStatus) {
            pointagesStats.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("pointagesByStatus", pointagesStats);

        return stats;
    }

    @Override
    public Map<String, Object> getSitesStats() {
        log.debug("Récupération des statistiques des sites");

        Map<String, Object> stats = new HashMap<>();

        stats.put("totalSites", siteRepository.count());
        stats.put("sitesEnCours", siteRepository.countByStatus(com.tcgm.model.enums.StatutSite.EN_COURS));
        stats.put("sitesTermines", siteRepository.countByStatus(com.tcgm.model.enums.StatutSite.TERMINE));
        stats.put("sitesPlanifies", siteRepository.countByStatus(com.tcgm.model.enums.StatutSite.PLANIFIE));
        stats.put("sitesSuspendus", siteRepository.countByStatus(com.tcgm.model.enums.StatutSite.SUSPENDU));

        // Répartition par client
        var sitesByClient = siteRepository.countSitesByClient();
        Map<String, Long> clientsStats = new HashMap<>();
        for (Object[] row : sitesByClient) {
            clientsStats.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("sitesByClient", clientsStats);

        return stats;
    }

    @Override
    public Map<String, Object> getSiteStats(Long siteId) {
        log.debug("Récupération des statistiques du site {}", siteId);

        Map<String, Object> stats = new HashMap<>();

        // Vérifier que le site existe
        if (!siteRepository.existsById(siteId)) {
            stats.put("error", "Site non trouvé");
            return stats;
        }

        stats.put("siteId", siteId);
        stats.put("totalTaches", tacheRepository.findBySiteId(siteId, org.springframework.data.domain.Pageable.unpaged()).getTotalElements());
        stats.put("tachesTerminees", tacheRepository.countCompletedTachesBySite(siteId));
        stats.put("totalOuvriers", ouvrierRepository.countOuvriersBySite(siteId));
        stats.put("totalPointages", dossierRepository.findBySiteId(siteId, org.springframework.data.domain.Pageable.unpaged()).getTotalElements());

        // Tâches par statut
        var tachesByStatus = tacheRepository.countTachesByStatusForSite(siteId);
        Map<String, Long> tachesStats = new HashMap<>();
        for (Object[] row : tachesByStatus) {
            tachesStats.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("tachesByStatus", tachesStats);

        // Pointages par statut
        var pointagesByStatus = dossierRepository.countDossiersByStatusForSite(siteId);
        Map<String, Long> pointagesStats = new HashMap<>();
        for (Object[] row : pointagesByStatus) {
            pointagesStats.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("pointagesByStatus", pointagesStats);

        // Taux d'avancement
        long totalTaches = (long) stats.get("totalTaches");
        long tachesTerminees = (long) stats.get("tachesTerminees");
        stats.put("tauxAvancement", totalTaches > 0 ? (tachesTerminees * 100.0 / totalTaches) : 0);

        return stats;
    }

    @Override
    public Map<String, Object> getOuvriersStats() {
        log.debug("Récupération des statistiques des ouvriers");

        Map<String, Object> stats = new HashMap<>();

        stats.put("totalOuvriers", ouvrierRepository.count());
        stats.put("ouvriersActifs", ouvrierRepository.countActiveOuvriers());
        stats.put("ouvriersInactifs", ouvrierRepository.count() - ouvrierRepository.countActiveOuvriers());

        // Ouvriers par spécialité
        var bySpecialite = ouvrierRepository.countOuvriersBySpecialite();
        Map<String, Long> specialitesStats = new HashMap<>();
        for (Object[] row : bySpecialite) {
            specialitesStats.put(row[0] != null ? row[0].toString() : "Non spécifié", (Long) row[1]);
        }
        stats.put("ouvriersBySpecialite", specialitesStats);

        return stats;
    }

    @Override
    public Map<String, Object> getOuvriersStatsBySite(Long siteId) {
        log.debug("Récupération des statistiques des ouvriers du site {}", siteId);

        Map<String, Object> stats = new HashMap<>();

        stats.put("siteId", siteId);
        stats.put("totalOuvriers", ouvrierRepository.countOuvriersBySite(siteId));
        stats.put("ouvriersActifs", ouvrierRepository.countActiveOuvriersBySite(siteId));

        // Ouvriers par spécialité sur le site
        var bySpecialite = ouvrierRepository.countOuvriersBySpecialiteOnSite(siteId);
        Map<String, Long> specialitesStats = new HashMap<>();
        for (Object[] row : bySpecialite) {
            specialitesStats.put(row[0] != null ? row[0].toString() : "Non spécifié", (Long) row[1]);
        }
        stats.put("ouvriersBySpecialite", specialitesStats);

        return stats;
    }

    @Override
    public Map<String, Object> getPointageStats(Long siteId) {
        log.debug("Récupération des statistiques de pointage du site {}", siteId);

        Map<String, Object> stats = new HashMap<>();

        stats.put("siteId", siteId);
        stats.put("totalDossiers", dossierRepository.findBySiteId(siteId, org.springframework.data.domain.Pageable.unpaged()).getTotalElements());

        // Pointages par statut
        var byStatus = dossierRepository.countDossiersByStatusForSite(siteId);
        Map<String, Long> statusStats = new HashMap<>();
        for (Object[] row : byStatus) {
            statusStats.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("pointagesByStatus", statusStats);

        return stats;
    }

    @Override
    public Map<String, Object> getTachesStats() {
        log.debug("Récupération des statistiques des tâches");

        Map<String, Object> stats = new HashMap<>();

        stats.put("totalTaches", tacheRepository.count());

        // Tâches par statut
        var byStatus = tacheRepository.countTachesByStatus();
        Map<String, Long> statusStats = new HashMap<>();
        for (Object[] row : byStatus) {
            statusStats.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("tachesByStatus", statusStats);

        // Tâches par priorité
        var byPriority = tacheRepository.countTachesByPriority();
        Map<String, Long> priorityStats = new HashMap<>();
        for (Object[] row : byPriority) {
            priorityStats.put("Priorité " + row[0].toString(), (Long) row[1]);
        }
        stats.put("tachesByPriority", priorityStats);

        return stats;
    }

    @Override
    public Map<String, Object> getTachesStatsBySite(Long siteId) {
        log.debug("Récupération des statistiques des tâches du site {}", siteId);

        Map<String, Object> stats = new HashMap<>();

        stats.put("siteId", siteId);
        stats.put("totalTaches", tacheRepository.findBySiteId(siteId, org.springframework.data.domain.Pageable.unpaged()).getTotalElements());
        stats.put("tachesTerminees", tacheRepository.countCompletedTachesBySite(siteId));

        // Tâches par statut
        var byStatus = tacheRepository.countTachesByStatusForSite(siteId);
        Map<String, Long> statusStats = new HashMap<>();
        for (Object[] row : byStatus) {
            statusStats.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("tachesByStatus", statusStats);

        // Taux d'avancement
        long totalTaches = (long) stats.get("totalTaches");
        long tachesTerminees = (long) stats.get("tachesTerminees");
        stats.put("tauxAvancement", totalTaches > 0 ? (tachesTerminees * 100.0 / totalTaches) : 0);

        return stats;
    }

    @Override
    public Map<String, Object> getClientsStats() {
        log.debug("Récupération des statistiques des clients");

        Map<String, Object> stats = new HashMap<>();

        stats.put("totalClients", clientRepository.count());

        // Clients avec le plus de sites
        var sitesByClient = siteRepository.countSitesByClient();
        Map<String, Long> clientsStats = new HashMap<>();
        for (Object[] row : sitesByClient) {
            clientsStats.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("sitesByClient", clientsStats);

        return stats;
    }

    @Override
    public Map<String, Object> getUsersStats() {
        log.debug("Récupération des statistiques des utilisateurs");

        Map<String, Object> stats = new HashMap<>();

        stats.put("totalUsers", userRepository.count());
        stats.put("usersActifs", userRepository.findEnabledUsers().size());

        return stats;
    }
}