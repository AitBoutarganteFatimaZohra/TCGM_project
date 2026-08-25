import axiosInstance from './axiosConfig';

const BASE_URL = '/ressources';

export const getRessourcesBySite = async (siteId, { statut, type, search } = {}) => {
  const params = {};
  if (statut) params.statut = statut;
  if (type) params.type = type;
  if (search) params.search = search;

  const { data } = await axiosInstance.get(`${BASE_URL}/site/${siteId}`, { params });
  return data;
};

export const getRessourceById = async (id) => {
  const { data } = await axiosInstance.get(`${BASE_URL}/${id}`);
  return data;
};

export const createRessource = async (payload) => {
  const { data } = await axiosInstance.post(BASE_URL, payload);
  return data;
};

export const updateRessource = async (id, payload) => {
  const { data } = await axiosInstance.put(`${BASE_URL}/${id}`, payload);
  return data;
};

// Réservé Admin (override direct)
export const updateRessourceStatut = async (id, statut) => {
  const { data } = await axiosInstance.patch(`${BASE_URL}/${id}/statut`, { statut });
  return data;
};

// ⚠️ NOUVEAU
export const proposerStatutRessource = async (id, statut) => {
  const { data } = await axiosInstance.post(`${BASE_URL}/${id}/proposer-statut`, { statut });
  return data;
};

export const validerStatutRessource = async (id) => {
  const { data } = await axiosInstance.post(`${BASE_URL}/${id}/valider-statut`);
  return data;
};

export const rejeterStatutRessource = async (id, motif) => {
  const { data } = await axiosInstance.post(`${BASE_URL}/${id}/rejeter-statut`, { motif });
  return data;
};

export const deleteRessource = async (id) => {
  await axiosInstance.delete(`${BASE_URL}/${id}`);
};