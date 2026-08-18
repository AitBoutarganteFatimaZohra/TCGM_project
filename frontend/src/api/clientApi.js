import axiosInstance from './axiosConfig';

// =============================================================
// CLIENTS
// =============================================================

/**
 * Récupérer tous les clients
 * GET /api/clients
 */
export const getClients = async (params = {}) => {
  const response = await axiosInstance.get('/clients', { params });
  return response.data;
};

/**
 * Récupérer un client par son ID
 * GET /api/clients/{id}
 */
export const getClientById = async (id) => {
  const response = await axiosInstance.get(`/clients/${id}`);
  return response.data;
};

/**
 * Récupérer les sites (chantiers) d'un client
 * GET /api/clients/{id}/sites
 */
export const getClientSites = async (id) => {
  const response = await axiosInstance.get(`/clients/${id}/sites`);
  return response.data;
};

/**
 * Créer un client
 * POST /api/clients
 */
export const createClient = async (data) => {
  const response = await axiosInstance.post('/clients', data);
  return response.data;
};

/**
 * Modifier un client
 * PUT /api/clients/{id}
 */
export const updateClient = async (id, data) => {
  const response = await axiosInstance.put(`/clients/${id}`, data);
  return response.data;
};

/**
 * Supprimer un client
 * DELETE /api/clients/{id}
 */
export const deleteClient = async (id) => {
  const response = await axiosInstance.delete(`/clients/${id}`);
  return response.data;
};