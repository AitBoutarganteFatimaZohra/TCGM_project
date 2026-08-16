import { useState, useEffect } from 'react';
import { getAffectations, createAffectation, updateAffectation, deleteAffectation, getAffectationById } from '../api/affectationApi';

const useAffectations = () => {
  const [affectations, setAffectations] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchAffectations = async (params = {}) => {
    setLoading(true);
    setError(null);
    try {
      const data = await getAffectations(params);
      setAffectations(data.content || data);
      return data;
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors du chargement des affectations');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const fetchAffectationById = async (id) => {
    setLoading(true);
    setError(null);
    try {
      return await getAffectationById(id);
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors du chargement de l\'affectation');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const addAffectation = async (data) => {
    setLoading(true);
    setError(null);
    try {
      const newAffectation = await createAffectation(data);
      setAffectations(prev => [newAffectation, ...prev]);
      return newAffectation;
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la création de l\'affectation');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const editAffectation = async (id, data) => {
    setLoading(true);
    setError(null);
    try {
      const updated = await updateAffectation(id, data);
      setAffectations(prev => prev.map(a => a.id === id ? updated : a));
      return updated;
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la modification de l\'affectation');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const removeAffectation = async (id) => {
    setLoading(true);
    setError(null);
    try {
      await deleteAffectation(id);
      setAffectations(prev => prev.filter(a => a.id !== id));
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la suppression de l\'affectation');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAffectations();
  }, []);

  return {
    affectations,
    loading,
    error,
    fetchAffectations,
    fetchAffectationById,
    addAffectation,
    editAffectation,
    removeAffectation,
  };
};

export default useAffectations;