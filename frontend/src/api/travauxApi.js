import axiosInstance from './axiosConfig';

// =============================================================
// TRAVAUX
// =============================================================

export const getTravaux = async (params = {}) => {
  const response = await axiosInstance.get('/travaux', { params });
  return response.data;
};

export const getTravauxById = async (id) => {
  const response = await axiosInstance.get(`/travaux/${id}`);
  return response.data;
};

export const createTravaux = async (data) => {
  const response = await axiosInstance.post('/travaux', data);
  return response.data;
};

export const updateTravaux = async (id, data) => {
  const response = await axiosInstance.put(`/travaux/${id}`, data);
  return response.data;
};

export const deleteTravaux = async (id) => {
  const response = await axiosInstance.delete(`/travaux/${id}`);
  return response.data;
};