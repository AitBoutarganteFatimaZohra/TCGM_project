import axiosInstance from './axiosConfig';

export const getChantiers = async (params = {}) => {
  const response = await axiosInstance.get('/sites', { params });
  return response.data;
};

export const getChantierById = async (id) => {
  const response = await axiosInstance.get(`/sites/${id}`);
  return response.data;
};

export const createChantier = async (data) => {
  const response = await axiosInstance.post('/sites', data);
  return response.data;
};

export const updateChantier = async (id, data) => {
  const response = await axiosInstance.put(`/sites/${id}`, data);
  return response.data;
};

export const deleteChantier = async (id) => {
  const response = await axiosInstance.delete(`/sites/${id}`);
  return response.data;
};

export const getMySites = async (params = {}) => {
  const response = await axiosInstance.get('/sites/my-sites', { params });
  return response.data;
};


// ⚠️ NOUVEAU
export const validerModificationSite = async (id) => {
  const response = await axiosInstance.post(`/sites/${id}/valider-modification`);
  return response.data;
};

export const rejeterModificationSite = async (id, motif) => {
  const response = await axiosInstance.post(`/sites/${id}/rejeter-modification`, { motif });
  return response.data;
};