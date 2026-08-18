import { useState, useEffect, useCallback } from 'react';
import {
  getDashboardStats,
  getSitesStats,
  getOuvriersStats,
  getTachesStats,
  getClientsStats,
  getUsersStats,
} from '../api/statistiqueApi';

const useStatistiques = () => {
  const [dashboardStats, setDashboardStats] = useState(null);
  const [sitesStats, setSitesStats] = useState(null);
  const [ouvriersStats, setOuvriersStats] = useState(null);
  const [tachesStats, setTachesStats] = useState(null);
  const [clientsStats, setClientsStats] = useState(null);
  const [usersStats, setUsersStats] = useState(null);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchAllStats = useCallback(async () => {
    setLoading(true);
    setError(null);

    // Accessible à tous les utilisateurs connectés
    try {
      const dashboard = await getDashboardStats();
      setDashboardStats(dashboard);
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors du chargement des statistiques');
    }

    // Sections restreintes par rôle : on tente chacune séparément
    // et on ignore silencieusement un 403 (utilisateur non autorisé pour cette section)
    const results = await Promise.allSettled([
      getSitesStats(),
      getOuvriersStats(),
      getTachesStats(),
      getClientsStats(),
      getUsersStats(),
    ]);

    if (results[0].status === 'fulfilled') setSitesStats(results[0].value);
    if (results[1].status === 'fulfilled') setOuvriersStats(results[1].value);
    if (results[2].status === 'fulfilled') setTachesStats(results[2].value);
    if (results[3].status === 'fulfilled') setClientsStats(results[3].value);
    if (results[4].status === 'fulfilled') setUsersStats(results[4].value);

    setLoading(false);
  }, []);

  useEffect(() => {
    fetchAllStats();
  }, [fetchAllStats]);

  return {
    dashboardStats,
    sitesStats,
    ouvriersStats,
    tachesStats,
    clientsStats,
    usersStats,
    loading,
    error,
    refetch: fetchAllStats,
  };
};

export default useStatistiques;