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