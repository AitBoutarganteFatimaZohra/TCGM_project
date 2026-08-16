import { useState, useEffect } from 'react';
import { getClients, createClient, updateClient, deleteClient, getClientById } from '../api/clientApi';

const useClients = () => {
  const [clients, setClients] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchClients = async (params = {}) => {
    setLoading(true);
    setError(null);
    try {
      const data = await getClients(params);
      setClients(data.content || data);
      return data;
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors du chargement des clients');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const fetchClientById = async (id) => {
    setLoading(true);
    setError(null);
    try {
      return await getClientById(id);
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors du chargement du client');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const addClient = async (data) => {
    setLoading(true);
    setError(null);
    try {
      const newClient = await createClient(data);
      setClients(prev => [newClient, ...prev]);
      return newClient;
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la création du client');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const editClient = async (id, data) => {
    setLoading(true);
    setError(null);
    try {
      const updated = await updateClient(id, data);
      setClients(prev => prev.map(c => c.id === id ? updated : c));
      return updated;
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la modification du client');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const removeClient = async (id) => {
    setLoading(true);
    setError(null);
    try {
      await deleteClient(id);
      setClients(prev => prev.filter(c => c.id !== id));
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la suppression du client');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchClients();
  }, []);

  return {
    clients,
    loading,
    error,
    fetchClients,
    fetchClientById,
    addClient,
    editClient,
    removeClient,
  };
};

export default useClients;