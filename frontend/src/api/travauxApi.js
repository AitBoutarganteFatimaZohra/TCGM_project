// travauxApi.js
import axiosInstance from './axiosConfig';

// =============================================================
// TRAVAUX - CRUD DE BASE
// =============================================================

/**
 * Récupère la liste des travaux avec pagination et filtres
 * @param {Object} params - Paramètres de requête (page, size, search, statut, chantierId, etc.)
 * @returns {Promise} - Promesse avec les données des travaux
 */
export const getTravaux = async (params = {}) => {
  const response = await axiosInstance.get('/travaux', { params });
  return response.data;
};

/**
 * Récupère un travail par son ID
 * @param {number} id - ID du travail
 * @returns {Promise} - Promesse avec les données du travail
 */
export const getTravauxById = async (id) => {
  const response = await axiosInstance.get(`/travaux/${id}`);
  return response.data;
};

/**
 * Crée un nouveau travail
 * @param {Object} data - Données du travail à créer
 * @returns {Promise} - Promesse avec les données du travail créé
 */
export const createTravaux = async (data) => {
  const response = await axiosInstance.post('/travaux', data);
  return response.data;
};

/**
 * Met à jour un travail existant
 * @param {number} id - ID du travail à modifier
 * @param {Object} data - Données à mettre à jour
 * @returns {Promise} - Promesse avec les données du travail mis à jour
 */
export const updateTravaux = async (id, data) => {
  const response = await axiosInstance.put(`/travaux/${id}`, data);
  return response.data;
};

/**
 * Supprime un travail
 * @param {number} id - ID du travail à supprimer
 * @returns {Promise} - Promesse avec le résultat de la suppression
 */
export const deleteTravaux = async (id) => {
  const response = await axiosInstance.delete(`/travaux/${id}`);
  return response.data;
};

// =============================================================
// TRAVAUX - FONCTIONS SPÉCIFIQUES
// =============================================================

/**
 * Met à jour le statut d'un travail
 * @param {number} id - ID du travail
 * @param {string} statut - Nouveau statut (EN_ATTENTE, EN_COURS, TERMINE, ANNULE)
 * @returns {Promise} - Promesse avec les données du travail mis à jour
 */
export const updateTravauxStatut = async (id, statut) => {
  const response = await axiosInstance.patch(`/travaux/${id}/statut`, null, {
    params: { statut },
  });
  return response.data;
};

/**
 * Récupère les travaux d'un chantier spécifique
 * @param {number} chantierId - ID du chantier
 * @param {Object} params - Paramètres de requête (page, size, statut, etc.)
 * @returns {Promise} - Promesse avec les données des travaux du chantier
 */
export const getTravauxByChantier = async (chantierId, params = {}) => {
  const response = await axiosInstance.get(`/travaux/chantier/${chantierId}`, { params });
  return response.data;
};