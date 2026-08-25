import axiosInstance from './axiosConfig';

// =============================================================
// OUVRIERS
// =============================================================

export const getOuvriers = async (params = {}) => {
  const response = await axiosInstance.get('/ouvriers', { params });
  return response.data;
};

export const getOuvrierById = async (id) => {
  const response = await axiosInstance.get(`/ouvriers/${id}`);
  return response.data;
};

export const createOuvrier = async (data) => {
  const response = await axiosInstance.post('/ouvriers', data);
  return response.data;
};

export const updateOuvrier = async (id, data) => {
  const response = await axiosInstance.put(`/ouvriers/${id}`, data);
  return response.data;
};

export const deleteOuvrier = async (id) => {
  const response = await axiosInstance.delete(`/ouvriers/${id}`);
  return response.data;
};


export const getOuvriersDisponibles = async (params = {}) => {
  const response = await axiosInstance.get('/ouvriers/disponibles', { params });
  return response.data;
};