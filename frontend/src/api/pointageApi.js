import axiosInstance from './axiosConfig';

// =============================================================
// POINTAGE — DOSSIERS
// =============================================================

export const getDossiersPointage = async (params = {}) => {
  const response = await axiosInstance.get('/pointage/dossiers', { params });
  return response.data;
};

export const getDossierPointageById = async (id) => {
  const response = await axiosInstance.get(`/pointage/dossiers/${id}`);
  return response.data;
};

export const createDossierPointage = async (data) => {
  const response = await axiosInstance.post('/pointage/dossiers', data);
  return response.data;
};

export const updateDossierPointage = async (id, data) => {
  const response = await axiosInstance.put(`/pointage/dossiers/${id}`, data);
  return response.data;
};

export const deleteDossierPointage = async (id) => {
  const response = await axiosInstance.delete(`/pointage/dossiers/${id}`);
  return response.data;
};

export const validerDossierPointage = async (id, data) => {
  const response = await axiosInstance.post(`/pointage/dossiers/${id}/valider`, data);
  return response.data;
};

export const rejeterDossierPointage = async (id, data) => {
  const response = await axiosInstance.post(`/pointage/dossiers/${id}/rejeter`, data);
  return response.data;
};

export const getTodayPointage = async (siteId) => {
  const response = await axiosInstance.get(`/pointage/dossiers/site/${siteId}/today`);
  return response.data;
};

export const getPointageStatistiques = async (siteId) => {
  const response = await axiosInstance.get(`/pointage/statistiques/site/${siteId}`);
  return response.data;
};

// =============================================================
// POINTAGE — LIGNES
// =============================================================

export const addLignePointage = async (dossierId, data) => {
  const response = await axiosInstance.post(`/pointage/dossiers/${dossierId}/lignes`, data);
  return response.data;
};

export const removeLignePointage = async (ligneId) => {
  const response = await axiosInstance.delete(`/pointage/lignes/${ligneId}`);
  return response.data;
};