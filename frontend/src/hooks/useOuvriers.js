import { useState, useEffect } from 'react';
import { getOuvriers, createOuvrier, updateOuvrier, deleteOuvrier, getOuvrierById } from '../api/ouvrierApi';

const useOuvriers = () => {
  const [ouvriers, setOuvriers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchOuvriers = async (params = {}) => {
    setLoading(true);
    setError(null);
    try {
      const data = await getOuvriers(params);
      setOuvriers(data.content || data);
      return data;
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors du chargement des ouvriers');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const fetchOuvrierById = async (id) => {
    setLoading(true);
    setError(null);
    try {
      return await getOuvrierById(id);
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors du chargement de l\'ouvrier');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const addOuvrier = async (data) => {
    setLoading(true);
    setError(null);
    try {
      const newOuvrier = await createOuvrier(data);
      setOuvriers(prev => [newOuvrier, ...prev]);
      return newOuvrier;
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la création de l\'ouvrier');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const editOuvrier = async (id, data) => {
    setLoading(true);
    setError(null);
    try {
      const updated = await updateOuvrier(id, data);
      setOuvriers(prev => prev.map(o => o.id === id ? updated : o));
      return updated;
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la modification de l\'ouvrier');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const removeOuvrier = async (id) => {
    setLoading(true);
    setError(null);
    try {
      await deleteOuvrier(id);
      setOuvriers(prev => prev.filter(o => o.id !== id));
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la suppression de l\'ouvrier');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOuvriers();
  }, []);

  return {
    ouvriers,
    loading,
    error,
    fetchOuvriers,
    fetchOuvrierById,
    addOuvrier,
    editOuvrier,
    removeOuvrier,
  };
};

export default useOuvriers;