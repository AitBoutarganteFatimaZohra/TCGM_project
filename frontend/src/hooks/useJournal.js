import { useState, useEffect, useCallback } from 'react';
import { getJournal, getJournalById } from '../api/journalApi';

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

  // Le journal est en lecture seule : uniquement fetch, pas de create/update/delete
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
  };
};

export default useJournal;