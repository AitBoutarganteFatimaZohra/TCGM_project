import axiosInstance from './axiosConfig';

// =============================================================
// CLIENTS
// =============================================================

export const getClients = async (params = {}) => {
  const response = await axiosInstance.get('/clients', { params });
  return response.data;
};

export const getClientById = async (id) => {
  const response = await axiosInstance.get(`/clients/${id}`);
  return response.data;
};

export const createClient = async (data) => {
  const response = await axiosInstance.post('/clients', data);
  return response.data;
};

export const updateClient = async (id, data) => {
  const response = await axiosInstance.put(`/clients/${id}`, data);
  return response.data;
};

export const deleteClient = async (id) => {
  const response = await axiosInstance.delete(`/clients/${id}`);
  return response.data;
};