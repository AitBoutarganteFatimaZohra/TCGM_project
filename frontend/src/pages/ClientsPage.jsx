import { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import useClients from '../hooks/useClients';

const ClientsPage = () => {
  const { clients, loading, error, fetchClients, removeClient } = useClients();
  const [search, setSearch] = useState('');
  const [hasLoadedOnce, setHasLoadedOnce] = useState(false);
  const debounceRef = useRef(null);

  useEffect(() => {
    // Debounce : on attend que l'utilisateur arrête de taper avant d'appeler l'API
    clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      fetchClients({ search: search || undefined }).finally(() => setHasLoadedOnce(true));
    }, 400);

    return () => clearTimeout(debounceRef.current);
  }, [search]);

  const handleDelete = async (id) => {
    if (window.confirm('Êtes-vous sûr de vouloir supprimer ce client ?')) {
      try {
        await removeClient(id);
      } catch (err) {
        alert(err.response?.data?.message || 'Erreur lors de la suppression');
      }
    }
  };

  // Loading plein écran uniquement au tout premier chargement
  if (loading && !hasLoadedOnce) {
    return <div className="loading">Chargement des clients...</div>;
  }

  return (
    <div className="clients-page">
      <div className="page-header">
        <h1>👥 Clients</h1>
        <Link to="/clients/create" className="btn-primary">
          + Nouveau client
        </Link>
      </div>

      <div className="filters">
        <input
          type="text"
          placeholder="🔍 Rechercher un client (nom, contact, e-mail)..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="search-input"
        />
      </div>

      {error && <div className="error-banner">❌ {error}</div>}

      {clients.length === 0 ? (
        <div className="empty-state">
          <p>Aucun client trouvé</p>
          <Link to="/clients/create" className="btn-primary">
            Créer le premier client
          </Link>
        </div>
      ) : (
        <div className="clients-grid">
          {clients.map((client) => (
            <div key={client.id} className="client-card">
              <div className="client-header">
                <h3>{client.name}</h3>
                <span className="client-badge">
                  {client.totalSites ?? 0} site{(client.totalSites ?? 0) > 1 ? 's' : ''}
                </span>
              </div>
              <div className="client-body">
                <p><strong>Contact:</strong> {client.contact || 'N/A'}</p>
                <p><strong>Téléphone:</strong> {client.phone || 'N/A'}</p>
                <p><strong>E-mail:</strong> {client.email || 'N/A'}</p>
                <p><strong>ICE:</strong> {client.ice || 'N/A'}</p>
              </div>
              <div className="client-footer">
                <Link to={`/clients/${client.id}`} className="btn-view">
                  Voir
                </Link>
                <Link to={`/clients/edit/${client.id}`} className="btn-edit">
                  Modifier
                </Link>
                <button onClick={() => handleDelete(client.id)} className="btn-delete">
                  Supprimer
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default ClientsPage;