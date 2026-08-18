import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import useClients from '../hooks/useClients';

const STATUS_LABELS = {
  PLANIFIE: 'Planifié',
  EN_COURS: 'En cours',
  TERMINE: 'Terminé',
  SUSPENDU: 'Suspendu',
};

const ClientDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { fetchClientById, fetchClientSites, removeClient, loading } = useClients();
  const [client, setClient] = useState(null);
  const [sites, setSites] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchClientById(id)
      .then(setClient)
      .catch(() => setError("Impossible de charger ce client."));

    fetchClientSites(id)
      .then(setSites)
      .catch(() => setSites([]));
  }, [id]);

  const handleDelete = async () => {
    if (window.confirm('Supprimer ce client ?')) {
      try {
        await removeClient(id);
        navigate('/clients');
      } catch (err) {
        alert(err.response?.data?.message || 'Erreur lors de la suppression');
      }
    }
  };

  if (loading && !client) {
    return <div className="loading">Chargement du client...</div>;
  }

  if (error) {
    return <div className="error-banner">❌ {error}</div>;
  }

  if (!client) {
    return null;
  }

  return (
    <div className="clients-page">
      <div className="page-header">
        <h1>👥 {client.name}</h1>
        <div className="chantier-footer" style={{ borderTop: 'none', paddingTop: 0 }}>
          <Link to={`/clients/edit/${id}`} className="btn-edit">Modifier</Link>
          <button onClick={handleDelete} className="btn-delete">Supprimer</button>
          <Link to="/clients" className="btn-view">Retour</Link>
        </div>
      </div>

      <div className="chantier-card" style={{ maxWidth: 620, marginBottom: 20 }}>
        <p><strong>Contact:</strong> {client.contact || 'N/A'}</p>
        <p><strong>Adresse:</strong> {client.address || 'N/A'}</p>
        <p><strong>Téléphone:</strong> {client.phone || 'N/A'}</p>
        <p><strong>E-mail:</strong> {client.email || 'N/A'}</p>
        <p><strong>ICE:</strong> {client.ice || 'N/A'}</p>
        <p><strong>RC:</strong> {client.rc || 'N/A'}</p>
      </div>

      <h3 className="page-title" style={{ fontSize: 16, marginBottom: 10 }}>
        Sites ({sites.length})
      </h3>

      {sites.length === 0 ? (
        <div className="empty-state">
          <p>Aucun site pour ce client</p>
        </div>
      ) : (
        <div className="chantiers-grid">
          {sites.map((site) => (
            <div key={site.id} className="chantier-card">
              <div className="chantier-header">
                <h3>{site.name}</h3>
                <span className={`status-badge status-${(site.status || '').toLowerCase()}`}>
                  {STATUS_LABELS[site.status] || site.status}
                </span>
              </div>
              <div className="chantier-body">
                <p><strong>Référence:</strong> {site.reference || 'N/A'}</p>
                <p><strong>Chef de projet:</strong> {site.chefProjet?.firstName} {site.chefProjet?.lastName}</p>
              </div>
              <div className="chantier-footer">
                <Link to={`/chantiers/${site.id}`} className="btn-view">
                  Voir le chantier
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default ClientDetailPage;