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

export const getOuvriersStats = async () => {
  const response = await axiosInstance.get('/statistiques/ouvriers');
  return response.data;
};

export const getOuvriersStatsBySite = async (siteId) => {
  const response = await axiosInstance.get(`/statistiques/ouvriers/site/${siteId}`);
  return response.data;
};

export const getPointageStats = async (siteId) => {
  const response = await axiosInstance.get(`/statistiques/pointage/site/${siteId}`);
  return response.data;
};

export const getTachesStats = async () => {
  const response = await axiosInstance.get('/statistiques/taches');
  return response.data;
};

export const getTachesStatsBySite = async (siteId) => {
  const response = await axiosInstance.get(`/statistiques/taches/site/${siteId}`);
  return response.data;
};

export const getClientsStats = async () => {
  const response = await axiosInstance.get('/statistiques/clients');
  return response.data;
};

export const getUsersStats = async () => {
  const response = await axiosInstance.get('/statistiques/users');
  return response.data;
};