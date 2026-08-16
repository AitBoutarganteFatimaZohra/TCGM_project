import axiosInstance from './axiosConfig';

// =============================================================
// STATISTIQUES
// =============================================================

export const getDashboardStats = async () => {
  const response = await axiosInstance.get('/statistiques/dashboard');
  return response.data;
};

export const getSitesStats = async () => {
  const response = await axiosInstance.get('/statistiques/sites');
  return response.data;
};

export const getSiteStats = async (id) => {
  const response = await axiosInstance.get(`/statistiques/sites/${id}`);
  return response.data;
};