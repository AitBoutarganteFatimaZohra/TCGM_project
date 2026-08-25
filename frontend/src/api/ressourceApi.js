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

export const updateRessourceStatut = async (id, statut) => {
  const { data } = await axiosInstance.patch(`${BASE_URL}/${id}/statut`, { statut });
  return data;
};

/**
 * ⚠️ CHANGEMENT : ne supprime plus immédiatement côté backend — place la
 * ressource en attente de validation de suppression et renvoie son état à
 * jour (au lieu d'un 204 vide). Le composant appelant doit désormais gérer
 * la réponse (ex: afficher "en attente de validation" au lieu de retirer
 * la ligne de la liste).
 */
export const deleteRessource = async (id) => {
  const { data } = await axiosInstance.delete(`${BASE_URL}/${id}`);
  return data;
};

// =============================================================
// CIRCUIT DE VALIDATION : Magasinier -> Chef de Chantier -> Chef de Projet
// =============================================================

/**
 * Valide l'action en attente (niveau 1 ou niveau 2 selon l'état actuel de
 * la ressource — géré côté backend). Si l'action validée était une
 * suppression, la ressource n'existe plus (réponse vide) : gérez ce cas
 * dans l'appelant (redirection vers la liste, par ex.).
 */
export const validerRessource = async (id) => {
  const response = await axiosInstance.post(`${BASE_URL}/${id}/valider`);
  return response.data; // undefined si la ressource a été supprimée (204)
};

/**
 * Rejette l'action en attente. Au niveau 1 (Chef de Chantier), déclenche
 * automatiquement l'escalade vers le Chef de Projet (recours). Au niveau 2
 * (Chef de Projet), le rejet est définitif. motif est optionnel.
 */
export const rejeterRessource = async (id, motif) => {
  const { data } = await axiosInstance.post(`${BASE_URL}/${id}/rejeter`, motif ? { motif } : {});
  return data;
};