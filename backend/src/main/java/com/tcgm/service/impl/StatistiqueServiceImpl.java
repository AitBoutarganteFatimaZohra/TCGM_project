package com.tcgm.service.impl;

import com.tcgm.model.Site;
import com.tcgm.repository.*;
import com.tcgm.security.SecurityUtils;
import com.tcgm.service.StatistiqueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
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
    private final SecurityUtils securityUtils;

    // =========================================================
    // DASHBOARD - point d'entrée, bascule selon le rôle
    // =========================================================
    @Override
    public Map<String, Object> getDashboardStats() {
        if (securityUtils.isChefChantier()) {
            return getDashboardStatsForChefChantier();
        }
        // ✅ NOUVEAU : Agent de Saisie (§1 cahier des charges)
        if (securityUtils.isAgentSaisie()) {
            return getDashboardStatsForAgentSaisie();
        }
        return getDashboardStatsGlobal();
    }

    // =========================================================
    // DASHBOARD - GLOBAL (Admin / Chef de Projet / Magasinier)
    // =========================================================
    private Map<String, Object> getDashboardStatsGlobal() {
        log.debug("Récupération des statistiques du dashboard (vue globale)");

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

    // =========================================================
    // DASHBOARD - CHEF DE CHANTIER (scopé sur son/ses chantier(s))
    // =========================================================
    private Map<String, Object> getDashboardStatsForChefChantier() {
        List<Long> chantierIds = securityUtils.getChantierIdsAsChefChantier();
        log.debug("Récupération des statistiques du dashboard (Chef de Chantier) pour les chantiers: {}", chantierIds);

        Map<String, Object> stats = new HashMap<>();

        if (chantierIds.isEmpty()) {
            stats.put("totalChantiers", 0L);
            stats.put("ouvriersActifs", 0L);
            stats.put("totalTaches", 0L);
            stats.put("pointagesEnAttente", 0L);
            stats.put("tachesByStatus", new HashMap<String, Long>());
            stats.put("pointagesByStatus", new HashMap<String, Long>());
            return stats;
        }

        long ouvriersActifs = 0;
        long totalTaches = 0;
        Map<String, Long> tachesByStatus = new HashMap<>();
        Map<String, Long> pointagesByStatus = new HashMap<>();

        for (Long chantierId : chantierIds) {
            ouvriersActifs += ouvrierRepository.countActiveOuvriersByChantier(chantierId);
            totalTaches += tacheRepository.countByChantierId(chantierId);

            for (Object[] row : tacheRepository.countTachesByStatusForChantier(chantierId)) {
                String key = row[0].toString();
                tachesByStatus.merge(key, (Long) row[1], Long::sum);
            }

            for (Object[] row : dossierRepository.countDossiersByStatusForSite(chantierId)) {
                String key = row[0].toString();
                pointagesByStatus.merge(key, (Long) row[1], Long::sum);
            }
        }

        long pointagesEnAttente = pointagesByStatus.getOrDefault("EN_ATTENTE", 0L);

        stats.put("totalChantiers", (long) chantierIds.size());
        stats.put("ouvriersActifs", ouvriersActifs);
        stats.put("totalTaches", totalTaches);
        stats.put("pointagesEnAttente", pointagesEnAttente);
        stats.put("tachesByStatus", tachesByStatus);
        stats.put("pointagesByStatus", pointagesByStatus);

        return stats;
    }

    // =========================================================
    // DASHBOARD - AGENT DE SAISIE (scopé sur son chantier unique)
    // §1 cahier des charges :
    //   - totalSites = toujours 1
    //   - totalPointages = pointages de son site
    //   - totalUsers = utilisateurs associés à son site (chef de projet,
    //     chef de chantier, magasinier, agent de saisie = lui-même)
    //   - ouvriersActifs = ouvriers actifs de son chantier
    //   - totalTaches = tâches en cours sur son chantier
    //   - sitesByStatus = son chantier unique avec son statut
    //   - tachesByStatus = répartition des tâches de son chantier
    //   - ouvriersBySpecialite = composition de son équipe
    // =========================================================
    private Map<String, Object> getDashboardStatsForAgentSaisie() {
        Long siteId = securityUtils.getSiteIdAsAgentSaisie();
        log.debug("Récupération des statistiques du dashboard (Agent de Saisie) pour le site: {}", siteId);

        Map<String, Object> stats = new HashMap<>();

        if (siteId == null) {
            stats.put("totalSites", 0L);
            stats.put("totalPointages", 0L);
            stats.put("totalUsers", 0L);
            stats.put("ouvriersActifs", 0L);
            stats.put("totalTaches", 0L);
            stats.put("sitesByStatus", new HashMap<String, Long>());
            stats.put("tachesByStatus", new HashMap<String, Long>());
            stats.put("ouvriersBySpecialite", new HashMap<String, Long>());
            return stats;
        }

        Site site = siteRepository.findById(siteId).orElse(null);

        stats.put("totalSites", 1L);
        stats.put("totalPointages", dossierRepository.findBySiteId(siteId, Pageable.unpaged()).getTotalElements());

        long totalUsers = 0;
        Map<String, Long> sitesByStatus = new HashMap<>();
        if (site != null) {
            if (site.getChefProjet() != null) totalUsers++;
            if (site.getChefChantier() != null) totalUsers++;
            if (site.getMagasinier() != null) totalUsers++;
            if (site.getAgentSaisie() != null) totalUsers++;
            if (site.getStatus() != null) {
                sitesByStatus.put(site.getStatus().name(), 1L);
            }
        }
        stats.put("totalUsers", totalUsers);
        stats.put("sitesByStatus", sitesByStatus);

        stats.put("ouvriersActifs", ouvrierRepository.countActiveOuvriersByChantier(siteId));

        Map<String, Long> tachesByStatus = new HashMap<>();
        for (Object[] row : tacheRepository.countTachesByStatusForChantier(siteId)) {
            tachesByStatus.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("tachesByStatus", tachesByStatus);
        // "tâches en cours" affiché en KPI = uniquement le statut EN_COURS
        stats.put("totalTaches", tachesByStatus.getOrDefault("EN_COURS", 0L));

        Map<String, Long> ouvriersBySpecialite = new HashMap<>();
        for (Object[] row : ouvrierRepository.countOuvriersBySpecialiteOnChantier(siteId)) {
            ouvriersBySpecialite.put(row[0] != null ? row[0].toString() : "Non spécifié", (Long) row[1]);
        }
        stats.put("ouvriersBySpecialite", ouvriersBySpecialite);

        return stats;
    }

    // =========================================================
    // Le reste des méthodes reste inchangé
    // =========================================================

    @Override
    public Map<String, Object> getSitesStats() {
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
        Map<String, Object> stats = new HashMap<>();

        if (!siteRepository.existsById(siteId)) {
            stats.put("error", "Site non trouvé");
            return stats;
        }

        stats.put("siteId", siteId);

        long totalTaches = tacheRepository.countByChantierId(siteId);
        long tachesTerminees = tacheRepository.countCompletedTachesByChantier(siteId);

        stats.put("totalTaches", totalTaches);
        stats.put("tachesTerminees", tachesTerminees);
        stats.put("totalOuvriers", ouvrierRepository.countOuvriersByChantier(siteId));
        stats.put("totalPointages", dossierRepository.findBySiteId(siteId, Pageable.unpaged()).getTotalElements());

        var tachesByStatus = tacheRepository.countTachesByStatusForChantier(siteId);
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

        stats.put("tauxAvancement", totalTaches > 0 ? (tachesTerminees * 100.0 / totalTaches) : 0);

        return stats;
    }

    @Override
    public Map<String, Object> getOuvriersStats() {
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
        Map<String, Object> stats = new HashMap<>();

        stats.put("siteId", siteId);

        long totalTaches = tacheRepository.countByChantierId(siteId);
        long tachesTerminees = tacheRepository.countCompletedTachesByChantier(siteId);

        stats.put("totalTaches", totalTaches);
        stats.put("tachesTerminees", tachesTerminees);

        var byStatus = tacheRepository.countTachesByStatusForChantier(siteId);
        Map<String, Long> statusStats = new HashMap<>();
        for (Object[] row : byStatus) {
            statusStats.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("tachesByStatus", statusStats);

        stats.put("tauxAvancement", totalTaches > 0 ? (tachesTerminees * 100.0 / totalTaches) : 0);

        return stats;
    }

    @Override
    public Map<String, Object> getClientsStats() {
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
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalUsers", userRepository.count());
        stats.put("usersActifs", userRepository.findEnabledUsers().size());

        return stats;
    }
}