package com.tcgm.service.impl;

import com.tcgm.repository.*;
import com.tcgm.service.StatistiqueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
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

        stats.put("totalSites", siteRepository.count());
        stats.put("totalOuvriers", ouvrierRepository.count());
        stats.put("totalTaches", tacheRepository.count());
        stats.put("totalClients", clientRepository.count());
        stats.put("totalUsers", userRepository.count());
        stats.put("totalPointages", dossierRepository.count());
        stats.put("ouvriersActifs", ouvrierRepository.countActiveOuvriers());

        var sitesByStatus = siteRepository.countSitesByStatus();
        Map<String, Long> sitesStats = new HashMap<>();
        for (Object[] row : sitesByStatus) {
            sitesStats.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("sitesByStatus", sitesStats);

        var tachesByStatus = tacheRepository.countTachesByStatus();
        Map<String, Long> tachesStats = new HashMap<>();
        for (Object[] row : tachesByStatus) {
            tachesStats.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("tachesByStatus", tachesStats);

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

        if (!siteRepository.existsById(siteId)) {
            stats.put("error", "Site non trouvé");
            return stats;
        }

        stats.put("siteId", siteId);
        
        // Récupérer les travaux du site pour les statistiques
        // Pour l'instant, on utilise siteId comme travauxId car relation 1:1
        Long travauxId = siteId;
        
        stats.put("totalTaches", tacheRepository.findByTravauxId(travauxId, Pageable.unpaged()).getTotalElements());
        stats.put("tachesTerminees", tacheRepository.countCompletedTachesByTravaux(travauxId));
        stats.put("totalOuvriers", ouvrierRepository.countOuvriersByChantier(siteId));
        stats.put("totalPointages", dossierRepository.findBySiteId(siteId, Pageable.unpaged()).getTotalElements());

        var tachesByStatus = tacheRepository.countTachesByStatusForTravaux(travauxId);
        Map<String, Long> tachesStats = new HashMap<>();
        for (Object[] row : tachesByStatus) {
            tachesStats.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("tachesByStatus", tachesStats);

        var pointagesByStatus = dossierRepository.countDossiersByStatusForSite(siteId);
        Map<String, Long> pointagesStats = new HashMap<>();
        for (Object[] row : pointagesByStatus) {
            pointagesStats.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("pointagesByStatus", pointagesStats);

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
        log.debug("Récupération des statistiques des ouvriers du chantier {}", siteId);

        Map<String, Object> stats = new HashMap<>();

        stats.put("chantierId", siteId);
        stats.put("totalOuvriers", ouvrierRepository.countOuvriersByChantier(siteId));
        stats.put("ouvriersActifs", ouvrierRepository.countActiveOuvriersByChantier(siteId));

        var bySpecialite = ouvrierRepository.countOuvriersBySpecialiteOnChantier(siteId);
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
        stats.put("totalDossiers", dossierRepository.findBySiteId(siteId, Pageable.unpaged()).getTotalElements());

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

        var byStatus = tacheRepository.countTachesByStatus();
        Map<String, Long> statusStats = new HashMap<>();
        for (Object[] row : byStatus) {
            statusStats.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("tachesByStatus", statusStats);

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
        
        Long travauxId = siteId; // Relation 1:1 entre site et travaux pour l'instant
        
        stats.put("totalTaches", tacheRepository.findByTravauxId(travauxId, Pageable.unpaged()).getTotalElements());
        stats.put("tachesTerminees", tacheRepository.countCompletedTachesByTravaux(travauxId));

        var byStatus = tacheRepository.countTachesByStatusForTravaux(travauxId);
        Map<String, Long> statusStats = new HashMap<>();
        for (Object[] row : byStatus) {
            statusStats.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("tachesByStatus", statusStats);

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