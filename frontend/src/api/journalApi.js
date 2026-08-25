import axiosInstance from './axiosConfig';

export const getJournal = async (params = {}) => {
  const response = await axiosInstance.get('/journal', { params });
  return response.data;
};

export const getJournalById = async (id) => {
  const response = await axiosInstance.get(`/journal/${id}`);
  return response.data;
};

export const validateJournalEntry = async (id) => {
  const response = await axiosInstance.put(`/journal/${id}/validate`);
  return response.data;
};

export const rejectJournalEntry = async (id) => {
  const response = await axiosInstance.put(`/journal/${id}/reject`);
  return response.data;
};

// ⚠️ Export du journal (cahier des charges §6.7).
// responseType 'blob' est indispensable : la réponse est un fichier
// binaire (PDF/XLSX), pas du JSON.
export const exportJournal = async (format, params = {}) => {
  const response = await axiosInstance.get('/journal/export', {
    params: { format, ...params },
    responseType: 'blob',
  });
  return response.data;
};

// ✅ CORRIGÉ : /journal/site/${siteId} n'existe pas côté backend.
// On réutilise GET /journal (le seul endpoint réellement exposé par
// JournalController) avec siteId en query param. Le backend scope déjà
// le résultat aux actions du Magasinier connecté, donc aucun risque de
// fuite de données d'un autre site.
export const getJournalBySite = async (siteId, params = {}) => {
  const response = await axiosInstance.get('/journal', {
    params: { siteId, ...params },
  });
  return response.data;
};