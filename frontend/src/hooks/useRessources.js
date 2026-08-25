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

  const removeRessource = useCallback(async (id) => {
    setLoading(true);
    setError(null);
    try {
      await deleteRessource(id);
      setRessources((prev) => prev.filter((r) => r.id !== id));
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