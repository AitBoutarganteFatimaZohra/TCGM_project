package com.tcgm.service;

import java.util.Map;

public interface StatistiqueService {
    Map<String, Object> getDashboardStats();
    Map<String, Object> getSitesStats();
    Map<String, Object> getSiteStats(Long siteId);
    Map<String, Object> getOuvriersStats();
    Map<String, Object> getOuvriersStatsBySite(Long siteId);
    Map<String, Object> getPointageStats(Long siteId);
    Map<String, Object> getTachesStats();
    Map<String, Object> getTachesStatsBySite(Long siteId);
    Map<String, Object> getClientsStats();
    Map<String, Object> getUsersStats();
}