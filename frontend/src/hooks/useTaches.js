import { useState, useEffect } from 'react';
import { getTaches, createTache, updateTache, deleteTache, getTacheById } from '../api/tacheApi';

const useTaches = () => {
  const [taches, setTaches] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchTaches = async (params = {}) => {
    setLoading(true);
    setError(null);
    try {
      const data = await getTaches(params);
      setTaches(data.content || data);
      return data;
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors du chargement des tâches');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const fetchTacheById = async (id) => {
    setLoading(true);
    setError(null);
    try {
      return await getTacheById(id);
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors du chargement de la tâche');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const addTache = async (data) => {
    setLoading(true);
    setError(null);
    try {
      const newTache = await createTache(data);
      setTaches(prev => [newTache, ...prev]);
      return newTache;
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la création de la tâche');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const editTache = async (id, data) => {
    setLoading(true);
    setError(null);
    try {
      const updated = await updateTache(id, data);
      setTaches(prev => prev.map(t => t.id === id ? updated : t));
      return updated;
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la modification de la tâche');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const removeTache = async (id) => {
    setLoading(true);
    setError(null);
    try {
      await deleteTache(id);
      setTaches(prev => prev.filter(t => t.id !== id));
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la suppression de la tâche');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTaches();
  }, []);

  return {
    taches,
    loading,
    error,
    fetchTaches,
    fetchTacheById,
    addTache,
    editTache,
    removeTache,
  };
};

export default useTaches;