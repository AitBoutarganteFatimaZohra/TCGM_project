import axiosInstance from './axiosConfig';

// =============================================================
// POINTAGE
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

export const validerDossierPointage = async (id, data) => {
  const response = await axiosInstance.post(`/pointage/dossiers/${id}/valider`, data);
  return response.data;
};