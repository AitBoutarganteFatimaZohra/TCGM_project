import { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import useChantiers from '../hooks/useChantiers';

const ChantiersPage = () => {
  const { chantiers, loading, error, fetchChantiers, removeChantier } = useChantiers();
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [hasLoadedOnce, setHasLoadedOnce] = useState(false);
  const debounceRef = useRef(null);

  useEffect(() => {
    // Debounce : on attend que l'utilisateur arrête de taper avant d'appeler l'API
    clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      fetchChantiers({ search, status: statusFilter || undefined }).finally(() => setHasLoadedOnce(true));
    }, 400);

    return () => clearTimeout(debounceRef.current);
  }, [search, statusFilter]);

  const handleDelete = async (id) => {
    if (window.confirm('Êtes-vous sûr de vouloir supprimer ce chantier ?')) {
      try {
        await removeChantier(id);
      } catch (err) {
        alert('Erreur lors de la suppression');
      }
    }
  };

  // Loading plein écran uniquement au tout premier chargement
  if (loading && !hasLoadedOnce) {
    return <div className="loading">Chargement des chantiers...</div>;
  }

  return (
    <div className="chantiers-page">
      <div className="page-header">
        <h1>🏗️ Chantiers</h1>
        <Link to="/chantiers/create" className="btn-primary">
          + Nouveau chantier
        </Link>
      </div>

      <div className="filters">
        <input
          type="text"
          placeholder="🔍 Rechercher un chantier..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="search-input"
        />
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="filter-select"
        >
          <option value="">Tous les statuts</option>
          <option value="PLANIFIE">Planifié</option>
          <option value="EN_COURS">En cours</option>
          <option value="TERMINE">Terminé</option>
          <option value="SUSPENDU">Suspendu</option>
        </select>
      </div>

      {error && <div className="error-banner">❌ {error}</div>}

      {chantiers.length === 0 ? (
        <div className="empty-state">
          <p>Aucun chantier trouvé</p>
          <Link to="/chantiers/create" className="btn-primary">
            Créer le premier chantier
          </Link>
        </div>
      ) : (
        <div className="chantiers-grid">
          {chantiers.map((chantier) => (
            <div key={chantier.id} className="chantier-card">
              <div className="chantier-header">
                <h3>{chantier.name}</h3>
                <span className={`status-badge status-${chantier.status?.toLowerCase()}`}>
                  {chantier.status}
                </span>
              </div>
              <div className="chantier-body">
                <p><strong>Référence:</strong> {chantier.reference || 'N/A'}</p>
                <p><strong>Client:</strong> {chantier.client?.name || 'N/A'}</p>
                <p><strong>Chef de projet:</strong> {chantier.chefProjet?.firstName} {chantier.chefProjet?.lastName}</p>
                <p><strong>Tâches:</strong> {chantier.totalTaches || 0}</p>
                <p><strong>Ouvriers:</strong> {chantier.totalOuvriers || 0}</p>
              </div>
              <div className="chantier-footer">
                <Link to={`/chantiers/${chantier.id}`} className="btn-view">
                  Voir
                </Link>
                <Link to={`/chantiers/edit/${chantier.id}`} className="btn-edit">
                  Modifier
                </Link>
                <button onClick={() => handleDelete(chantier.id)} className="btn-delete">
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

export default ChantiersPage;