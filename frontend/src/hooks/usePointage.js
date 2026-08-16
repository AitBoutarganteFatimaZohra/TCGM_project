import { useState } from 'react';
import { getDossiersPointage, getDossierPointageById, createDossierPointage, validerDossierPointage } from '../api/pointageApi';

const usePointage = () => {
  const [dossiers, setDossiers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchDossiers = async (params = {}) => {
    setLoading(true);
    setError(null);
    try {
      const data = await getDossiersPointage(params);
      setDossiers(data.content || data);
      return data;
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors du chargement des dossiers de pointage');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const fetchDossierById = async (id) => {
    setLoading(true);
    setError(null);
    try {
      return await getDossierPointageById(id);
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors du chargement du dossier');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const addDossier = async (data) => {
    setLoading(true);
    setError(null);
    try {
      const newDossier = await createDossierPointage(data);
      setDossiers(prev => [newDossier, ...prev]);
      return newDossier;
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la création du dossier');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const validateDossier = async (id, data) => {
    setLoading(true);
    setError(null);
    try {
      const validated = await validerDossierPointage(id, data);
      setDossiers(prev => prev.map(d => d.id === id ? validated : d));
      return validated;
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la validation du dossier');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return {
    dossiers,
    loading,
    error,
    fetchDossiers,
    fetchDossierById,
    addDossier,
    validateDossier,
  };
};

export default usePointage;