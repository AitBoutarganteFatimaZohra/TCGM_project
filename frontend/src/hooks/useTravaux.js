import { useState, useEffect } from 'react';
import { getTravaux, createTravaux, updateTravaux, deleteTravaux, getTravauxById } from '../api/travauxApi';

const useTravaux = () => {
  const [travaux, setTravaux] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchTravaux = async (params = {}) => {
    setLoading(true);
    setError(null);
    try {
      const data = await getTravaux(params);
      setTravaux(data.content || data);
      return data;
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors du chargement des travaux');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const fetchTravauxById = async (id) => {
    setLoading(true);
    setError(null);
    try {
      return await getTravauxById(id);
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors du chargement des travaux');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const addTravaux = async (data) => {
    setLoading(true);
    setError(null);
    try {
      const newTravaux = await createTravaux(data);
      setTravaux(prev => [newTravaux, ...prev]);
      return newTravaux;
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la création des travaux');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const editTravaux = async (id, data) => {
    setLoading(true);
    setError(null);
    try {
      const updated = await updateTravaux(id, data);
      setTravaux(prev => prev.map(t => t.id === id ? updated : t));
      return updated;
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la modification des travaux');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const removeTravaux = async (id) => {
    setLoading(true);
    setError(null);
    try {
      await deleteTravaux(id);
      setTravaux(prev => prev.filter(t => t.id !== id));
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la suppression des travaux');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTravaux();
  }, []);

  return {
    travaux,
    loading,
    error,
    fetchTravaux,
    fetchTravauxById,
    addTravaux,
    editTravaux,
    removeTravaux,
  };
};

export default useTravaux;