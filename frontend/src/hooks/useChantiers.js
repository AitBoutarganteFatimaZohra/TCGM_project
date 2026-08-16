import { useState, useEffect } from 'react';
import { getChantiers, createChantier, updateChantier, deleteChantier, getChantierById } from '../api/chantierApi';

/**
 * Hook personnalisé pour gérer les chantiers
 */
const useChantiers = () => {
  const [chantiers, setChantiers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Récupérer tous les chantiers
  const fetchChantiers = async (params = {}) => {
    setLoading(true);
    setError(null);
    try {
      const data = await getChantiers(params);
      setChantiers(data.content || data);
      return data;
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors du chargement des chantiers');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  // Récupérer un chantier par ID
  const fetchChantierById = async (id) => {
    setLoading(true);
    setError(null);
    try {
      const data = await getChantierById(id);
      return data;
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors du chargement du chantier');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  // Créer un chantier
  const addChantier = async (data) => {
    setLoading(true);
    setError(null);
    try {
      const newChantier = await createChantier(data);
      setChantiers(prev => [newChantier, ...prev]);
      return newChantier;
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la création du chantier');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  // Modifier un chantier
  const editChantier = async (id, data) => {
    setLoading(true);
    setError(null);
    try {
      const updated = await updateChantier(id, data);
      setChantiers(prev => prev.map(c => c.id === id ? updated : c));
      return updated;
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la modification du chantier');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  // Supprimer un chantier
  const removeChantier = async (id) => {
    setLoading(true);
    setError(null);
    try {
      await deleteChantier(id);
      setChantiers(prev => prev.filter(c => c.id !== id));
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la suppression du chantier');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  // Charger les chantiers au montage
  useEffect(() => {
    fetchChantiers();
  }, []);

  return {
    chantiers,
    loading,
    error,
    fetchChantiers,
    fetchChantierById,
    addChantier,
    editChantier,
    removeChantier,
  };
};

export default useChantiers;