import axiosInstance from './axiosConfig';

// =============================================================
// ALERTES
// =============================================================

/**
 * Récupérer les alertes actives des chantiers du Chef de Projet connecté
 * GET /api/alertes/my
 */
export const getMyAlertes = async () => {
  const response = await axiosInstance.get('/alertes/my');
  return response.data;
};

/**
 * Marquer une alerte comme résolue
 * PATCH /api/alertes/{id}/resolve
 */
export const resolveAlerte = async (id) => {
  const response = await axiosInstance.patch(`/alertes/${id}/resolve`);
  return response.data;
};