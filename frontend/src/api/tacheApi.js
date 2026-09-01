import axiosInstance from './axiosConfig';
import { getTravauxByChantier } from './travauxApi';

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

// ⚠️ NOUVEAU
export const proposerModificationTache = async (id, { status, plannedDate } = {}) => {
  const response = await axiosInstance.post(`/taches/${id}/proposer-modification`, {
    status: status || undefined,
    plannedDate: plannedDate || undefined,
  });
  return response.data;
};

export const validerModificationTache = async (id) => {
  const response = await axiosInstance.post(`/taches/${id}/valider-modification`);
  return response.data;
};

export const rejeterModificationTache = async (id, motif) => {
  const response = await axiosInstance.post(`/taches/${id}/rejeter-modification`, { motif });
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


export const getTachesBySite = async (siteId) => {
  if (!siteId) return [];

  const travauxData = await getTravauxByChantier(siteId, { size: 100 });
  const travauxList = travauxData.content || travauxData || [];

  if (travauxList.length === 0) return [];

  const results = await Promise.all(
    travauxList.map((t) => getTaches({ travauxId: t.id, size: 100 }).catch(() => ({ content: [] })))
  );

  const taches = results.flatMap((r) => r.content || r || []);

  const seen = new Set();
  return taches.filter((t) => {
    if (seen.has(t.id)) return false;
    seen.add(t.id);
    return true;
  });
};