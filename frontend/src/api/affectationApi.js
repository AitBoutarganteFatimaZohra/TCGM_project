import axiosInstance from './axiosConfig';

// =============================================================
// AFFECTATIONS
// =============================================================

export const getAffectations = async (params = {}) => {
  const response = await axiosInstance.get('/affectations', { params });
  return response.data;
};

export const getAffectationById = async (id) => {
  const response = await axiosInstance.get(`/affectations/${id}`);
  return response.data;
};

export const createAffectation = async (data) => {
  const response = await axiosInstance.post('/affectations', data);
  return response.data;
};

export const updateAffectation = async (id, data) => {
  const response = await axiosInstance.put(`/affectations/${id}`, data);
  return response.data;
};

export const deleteAffectation = async (id) => {
  const response = await axiosInstance.delete(`/affectations/${id}`);
  return response.data;
};

// =============================================================
// CIRCUIT DE VALIDATION : Chef de Chantier -> Chef de Projet
// =============================================================

/**
 * Étape 2a (Chef de Projet) : valide une affectation en attente.
 */
export const validerAffectation = async (id) => {
  const { data } = await axiosInstance.post(`/affectations/${id}/valider`);
  return data;
};

/**
 * Étape 2b (Chef de Projet) : rejette une affectation en attente.
 * motif est optionnel.
 */
export const rejeterAffectation = async (id, motif) => {
  const { data } = await axiosInstance.post(`/affectations/${id}/rejeter`, motif ? { motif } : {});
  return data;
};