import axiosInstance from './axiosConfig';
import { getTravauxByChantier } from './travauxApi';

// =============================================================
// TÂCHES
// =============================================================

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

/**
 * Override direct réservé à l'Administrateur (contourne le circuit de
 * validation). Le Chef de Chantier et le Chef de Projet doivent utiliser
 * soumettreTache / validerTache / rejeterTache ci-dessous.
 */
export const updateTacheStatus = async (id, status) => {
  const response = await axiosInstance.patch(`/taches/${id}/status`, null, {
    params: { status },
  });
  return response.data;
};

// =============================================================
// CIRCUIT DE VALIDATION : Chef de Chantier -> Chef de Projet
// =============================================================

/**
 * Étape 1 (Chef de Chantier) : soumet un changement de statut et/ou de
 * date prévue à la validation. payload: { proposedStatus, proposedPlannedDate }
 */
export const soumettreTache = async (id, payload) => {
  const response = await axiosInstance.post(`/taches/${id}/soumettre`, payload);
  return response.data;
};

/**
 * Étape 2a (Chef de Projet) : valide la demande en attente.
 */
export const validerTache = async (id) => {
  const response = await axiosInstance.post(`/taches/${id}/valider`);
  return response.data;
};

/**
 * Étape 2b (Chef de Projet) : rejette la demande en attente.
 * motif est optionnel.
 */
export const rejeterTache = async (id, motif) => {
  const response = await axiosInstance.post(`/taches/${id}/rejeter`, motif ? { motif } : {});
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

// =============================================================
// TÂCHES — PAR SITE (via Travaux)
// =============================================================

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