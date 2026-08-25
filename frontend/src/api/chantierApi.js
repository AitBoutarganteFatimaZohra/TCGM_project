import axiosInstance from './axiosConfig';

// =============================================================
// CHANTIERS / SITES
// =============================================================

/**
 * Récupérer tous les chantiers
 * GET /api/sites
 */
export const getChantiers = async (params = {}) => {
  const response = await axiosInstance.get('/sites', { params });
  return response.data;
};

/**
 * Récupérer un chantier par son ID
 * GET /api/sites/{id}
 */
export const getChantierById = async (id) => {
  const response = await axiosInstance.get(`/sites/${id}`);
  return response.data;
};

/**
 * Créer un chantier
 * POST /api/sites
 */
export const createChantier = async (data) => {
  const response = await axiosInstance.post('/sites', data);
  return response.data;
};

/**
 * Modifier un chantier
 * PUT /api/sites/{id}
 */
export const updateChantier = async (id, data) => {
  const response = await axiosInstance.put(`/sites/${id}`, data);
  return response.data;
};

/**
 * Supprimer un chantier
 * DELETE /api/sites/{id}
 */
export const deleteChantier = async (id) => {
  const response = await axiosInstance.delete(`/sites/${id}`);
  return response.data;
};

/**
 * Récupérer les sites de l'utilisateur connecté (quel que soit son rôle :
 * chef de projet, chef de chantier, magasinier, agent de saisie).
 * Renvoie une Page Spring Data -> response.data.content est le tableau.
 * GET /api/sites/my-sites
 */
export const getMySites = async (params = {}) => {
  const response = await axiosInstance.get('/sites/my-sites', { params });
  return response.data;
};