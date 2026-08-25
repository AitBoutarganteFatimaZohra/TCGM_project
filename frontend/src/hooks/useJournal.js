import { useState, useEffect, useCallback } from 'react';
import {
  getJournal,
  getJournalById,
  validateJournalEntry,
  rejectJournalEntry,
  exportJournal as exportJournalApi,
} from '../api/journalApi';

const useJournal = () => {
  const [journal, setJournal] = useState([]);
  const [pagination, setPagination] = useState({
    totalElements: 0,
    totalPages: 0,
    number: 0,
    size: 10,
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchJournal = useCallback(async (params = {}) => {
    setLoading(true);
    setError(null);
    try {
      const data = await getJournal(params);
      setJournal(data.content || []);
      setPagination({
        totalElements: data.totalElements ?? 0,
        totalPages: data.totalPages ?? 0,
        number: data.number ?? 0,
        size: data.size ?? 10,
      });
      return data;
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors du chargement du journal');
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const fetchJournalById = async (id) => {
    setLoading(true);
    setError(null);
    try {
      return await getJournalById(id);
    } catch (err) {
      setError(err.response?.data?.message || "Erreur lors du chargement de l'entrée du journal");
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const validateEntry = async (id) => {
    setError(null);
    try {
      const updated = await validateJournalEntry(id);
      setJournal((prev) => prev.map((entry) => (entry.id === id ? updated : entry)));
      return updated;
    } catch (err) {
      setError(err.response?.data?.message || "Erreur lors de la validation de l'entrée");
      throw err;
    }
  };

  const rejectEntry = async (id) => {
    setError(null);
    try {
      const updated = await rejectJournalEntry(id);
      setJournal((prev) => prev.map((entry) => (entry.id === id ? updated : entry)));
      return updated;
    } catch (err) {
      setError(err.response?.data?.message || "Erreur lors du rejet de l'entrée");
      throw err;
    }
  };

  // ⚠️ NOUVEAU : télécharge le fichier généré côté backend.
  const exportJournal = async (format, params = {}) => {
    setError(null);
    try {
      const blobData = await exportJournalApi(format, params);
      const extension = format === 'excel' ? 'xlsx' : 'pdf';
      const blob = new Blob([blobData]);
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `journal_tcgm_${new Date().toISOString().slice(0, 10)}.${extension}`;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      setError("Erreur lors de l'export du journal");
      throw err;
    }
  };

  useEffect(() => {
    fetchJournal();
  }, [fetchJournal]);

  return {
    journal,
    pagination,
    loading,
    error,
    fetchJournal,
    fetchJournalById,
    validateEntry,
    rejectEntry,
    exportJournal,
  };
};

export default useJournal;