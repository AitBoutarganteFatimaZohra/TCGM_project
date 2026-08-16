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