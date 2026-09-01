import { useState, useCallback } from 'react';
import {
  getRessourcesBySite,
  getRessourceById,
  createRessource,
  updateRessource,
  updateRessourceStatut,
  deleteRessource,
} from '../api/ressourceApi';

const useRessources = () => {
  const [ressources, setRessources] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchRessources = useCallback(async (siteId, filters = {}) => {
    setLoading(true);
    setError(null);
    try {
      const data = await getRessourcesBySite(siteId, filters);
      setRessources(data);
      return data;
    } catch (err) {
      setError(err?.response?.data?.message || 'Erreur lors du chargement des ressources');
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const fetchRessourceById = useCallback(async (id) => {
    setLoading(true);
    setError(null);
    try {
      return await getRessourceById(id);
    } catch (err) {
      setError(err?.response?.data?.message || 'Erreur lors du chargement de la ressource');
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const addRessource = useCallback(async (payload) => {
    setLoading(true);
    setError(null);
    try {
      return await createRessource(payload);
    } catch (err) {
      setError(err?.response?.data?.message || 'Erreur lors de la création');
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const editRessource = useCallback(async (id, payload) => {
    setLoading(true);
    setError(null);
    try {
      return await updateRessource(id, payload);
    } catch (err) {
      setError(err?.response?.data?.message || 'Erreur lors de la modification');
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const changeStatut = useCallback(async (id, statut) => {
    setError(null);
    try {
      return await updateRessourceStatut(id, statut);
    } catch (err) {
      setError(err?.response?.data?.message || 'Erreur lors du changement de statut');
      throw err;
    }
  }, []);

  /**
   * 🔧 CORRIGÉ : retourne maintenant le résultat de deleteRessource(id).
   * - Magasinier : la ressource est placée en attente de validation → le
   *   backend renvoie son état à jour (200 + corps) → `result` est un objet.
   * - Admin : suppression immédiate → le backend renvoie 204 sans contenu
   *   → `result` est undefined/null → l'appelant peut détecter ce cas et
   *   rediriger directement, au lieu de recharger une ressource qui n'existe
   *   plus.
   * On ne retire la ligne de `ressources` (liste) que si la ressource a
   * réellement été supprimée (result falsy) ; sinon on la met à jour avec
   * son nouvel état "en attente" pour refléter le badge dans la liste.
   */
  const removeRessource = useCallback(async (id) => {
    setLoading(true);
    setError(null);
    try {
      const result = await deleteRessource(id);
      if (!result) {
        setRessources((prev) => prev.filter((r) => r.id !== id));
      } else {
        setRessources((prev) => prev.map((r) => (r.id === id ? result : r)));
      }
      return result;
    } catch (err) {
      setError(err?.response?.data?.message || 'Erreur lors de la suppression');
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  return {
    ressources,
    loading,
    error,
    fetchRessources,
    fetchRessourceById,
    addRessource,
    editRessource,
    changeStatut,
    removeRessource,
  };
};

export default useRessources;