import { useState } from 'react';
import {
  getDossiersPointage,
  getDossierPointageById,
  createDossierPointage,
  updateDossierPointage,
  deleteDossierPointage,
  soumettreDossierPointage,
  validerDossierPointage,
  rejeterDossierPointage,
  addLignePointage,
  removeLignePointage,
} from '../api/pointageApi';

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
      setDossiers((prev) => [newDossier, ...prev]);
      return newDossier;
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la création du dossier');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const editDossier = async (id, data) => {
    setLoading(true);
    setError(null);
    try {
      const updated = await updateDossierPointage(id, data);
      setDossiers((prev) => prev.map((d) => (d.id === id ? updated : d)));
      return updated;
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la modification du dossier');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const removeDossier = async (id) => {
    setLoading(true);
    setError(null);
    try {
      await deleteDossierPointage(id);
      setDossiers((prev) => prev.filter((d) => d.id !== id));
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la suppression du dossier');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  // ✅ NOUVEAU
  const submitDossier = async (id) => {
    setError(null);
    try {
      const updated = await soumettreDossierPointage(id);
      setDossiers((prev) => prev.map((d) => (d.id === id ? updated : d)));
      return updated;
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la soumission du dossier');
      throw err;
    }
  };

  const validateDossier = async (id, data) => {
    setLoading(true);
    setError(null);
    try {
      const validated = await validerDossierPointage(id, data);
      setDossiers((prev) => prev.map((d) => (d.id === id ? validated : d)));
      return validated;
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la validation du dossier');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const rejectDossier = async (id, data) => {
    setLoading(true);
    setError(null);
    try {
      const rejected = await rejeterDossierPointage(id, data);
      setDossiers((prev) => prev.map((d) => (d.id === id ? rejected : d)));
      return rejected;
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors du rejet du dossier');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const addLigne = async (dossierId, data) => {
    setLoading(true);
    setError(null);
    try {
      return await addLignePointage(dossierId, data);
    } catch (err) {
      setError(err.response?.data?.message || "Erreur lors de l'ajout de la ligne");
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const removeLigne = async (ligneId) => {
    setLoading(true);
    setError(null);
    try {
      await removeLignePointage(ligneId);
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la suppression de la ligne');
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
    editDossier,
    removeDossier,
    submitDossier,
    validateDossier,
    rejectDossier,
    addLigne,
    removeLigne,
  };
};

export default usePointage;