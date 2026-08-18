import axiosInstance from './axiosConfig';

// =============================================================
// JOURNAL
// =============================================================

export const getJournal = async (params = {}) => {
  const response = await axiosInstance.get('/journal', { params });
  return response.data;
};

export const getJournalById = async (id) => {
  const response = await axiosInstance.get(`/journal/${id}`);
  return response.data;
};

export const getJournalByEntity = async (entityType, entityId, params = {}) => {
  const response = await axiosInstance.get(`/journal/entities/${entityType}/${entityId}`, { params });
  return response.data;
};

export const getJournalStatistiques = async () => {
  const response = await axiosInstance.get('/journal/statistiques');
  return response.data;
};

export const exportJournal = async (params = {}) => {
  const response = await axiosInstance.get('/journal/export', { params });
  return response.data;
};