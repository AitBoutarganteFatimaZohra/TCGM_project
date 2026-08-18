import axiosInstance from './axiosConfig';

// =============================================================
// TÂCHES
// =============================================================

export const getTaches = async (params = {}) => {
  const response = await axiosInstance.get('/taches', { params });
  return response.data;
};

export const getTacheById = async (id) => {
  const response = await axiosInstance.get(`/taches/${id}`);
  return response.data;
};

export const createTache = async (data) => {
  const response = await axiosInstance.post('/taches', data);
  return response.data;
};

export const updateTache = async (id, data) => {
  const response = await axiosInstance.put(`/taches/${id}`, data);
  return response.data;
};

export const deleteTache = async (id) => {
  const response = await axiosInstance.delete(`/taches/${id}`);
  return response.data;
};

export const updateTacheStatus = async (id, status) => {
  const response = await axiosInstance.patch(`/taches/${id}/status`, null, {
    params: { status },
  });
  return response.data;
};

export const affecterOuvrier = async (tacheId, ouvrierId) => {
  const response = await axiosInstance.post(`/taches/${tacheId}/ouvriers/${ouvrierId}`);
  return response.data;
};

export const retirerOuvrier = async (tacheId, ouvrierId) => {
  const response = await axiosInstance.delete(`/taches/${tacheId}/ouvriers/${ouvrierId}`);
  return response.data;
};