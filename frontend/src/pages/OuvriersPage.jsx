import { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import useOuvriers from '../hooks/useOuvriers';

const OuvriersPage = () => {
  const { ouvriers, loading, error, fetchOuvriers, removeOuvrier } = useOuvriers();
  const [search, setSearch] = useState('');
  const [specialiteFilter, setSpecialiteFilter] = useState('');
  const [activeFilter, setActiveFilter] = useState('');
  const [hasLoadedOnce, setHasLoadedOnce] = useState(false);
  const debounceRef = useRef(null);

  useEffect(() => {
    clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      fetchOuvriers({
        search: search || undefined,
        specialite: specialiteFilter || undefined,
        active: activeFilter === '' ? undefined : activeFilter === 'true',
      }).finally(() => setHasLoadedOnce(true));
    }, 400);

    return () => clearTimeout(debounceRef.current);
  }, [search, specialiteFilter, activeFilter]);

  const handleDelete = async (id, nom) => {
    if (window.confirm(`Supprimer l'ouvrier « ${nom} » ?`)) {
      try {
        await removeOuvrier(id);
      } catch (err) {
        alert('Erreur lors de la suppression');
      }
    }
  };

  const getSiteActuel = (ouvrier) => {
    const affectationActive = ouvrier.affectations?.find((a) => a.active);
    return affectationActive?.siteName || null;
  };

  if (loading && !hasLoadedOnce) {
    return <div className="loading">Chargement des ouvriers...</div>;
  }

  return (
    <div className="ouvriers-page">
      <div className="page-header">
        <h1>
          👷 Ouvriers
          <span className="counter-badge">{ouvriers.length}</span>
        </h1>
        <Link to="/ouvriers/nouveau" className="btn-primary">
          + Nouvel ouvrier
        </Link>
      </div>

      <div className="filters">
        <div className="search-wrapper">
          <span className="search-icon">🔍</span>
          <input
            type="text"
            className="search-input"
            placeholder="Rechercher par nom ou CIN..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <input
          type="text"
          className="filter-select"
          placeholder="Spécialité"
          value={specialiteFilter}
          onChange={(e) => setSpecialiteFilter(e.target.value)}
        />
        <select
          className="filter-select"
          value={activeFilter}
          onChange={(e) => setActiveFilter(e.target.value)}
        >
          <option value="">Tous les statuts</option>
          <option value="true">Actif</option>
          <option value="false">Inactif</option>
        </select>
      </div>

      {error && <div className="error-banner">❌ {error}</div>}

      {ouvriers.length === 0 ? (
        <div className="empty-state">
          <p>Aucun ouvrier trouvé</p>
          <Link to="/ouvriers/nouveau" className="btn-primary">
            Créer le premier ouvrier
          </Link>
        </div>
      ) : (
        <div className="table-container">
          <table className="ouvriers-table">
            <thead>
              <tr>
                <th>Photo</th>
                <th>Nom</th>
                <th>CIN</th>
                <th>Spécialité</th>
                <th>Téléphone</th>
                <th>Site actuel</th>
                <th>Statut</th>
                <th className="col-actions">Actions</th>
              </tr>
            </thead>
            <tbody>
              {ouvriers.map((ouvrier) => {
                const siteActuel = getSiteActuel(ouvrier);
                return (
                  <tr key={ouvrier.id}>
                    <td>
                      {ouvrier.photoUrl ? (
                        <img 
                          src={ouvrier.photoUrl} 
                          alt={`${ouvrier.firstName} ${ouvrier.lastName}`}
                          className="ouvrier-thumbnail"
                        />
                      ) : (
                        <div className="ouvrier-thumbnail-placeholder">
                          {ouvrier.firstName?.charAt(0)}{ouvrier.lastName?.charAt(0)}
                        </div>
                      )}
                    </td>
                    <td>
                      <strong>{ouvrier.firstName} {ouvrier.lastName}</strong>
                    </td>
                    <td className="cell-mono">{ouvrier.cin}</td>
                    <td>
                      {ouvrier.specialite ? (
                        <span className="badge-specialite">{ouvrier.specialite}</span>
                      ) : (
                        <span className="badge-specialite badge-specialite--empty">—</span>
                      )}
                    </td>
                    <td>{ouvrier.phone || '—'}</td>
                    <td>
                      {siteActuel ? (
                        <span className="badge-site">{siteActuel}</span>
                      ) : (
                        <span className="badge-site badge-site--disponible">Disponible</span>
                      )}
                    </td>
                    <td>
                      <span className={`badge ${ouvrier.active ? 'badge--success' : 'badge--danger'}`}>
                        {ouvrier.active ? 'Actif' : 'Inactif'}
                      </span>
                    </td>
                    <td className="col-actions">
                      <div className="row-actions">
                        <Link to={`/ouvriers/${ouvrier.id}`} className="icon-btn icon-btn--view" title="Voir">
                          👁
                        </Link>
                        <Link to={`/ouvriers/${ouvrier.id}/modifier`} className="icon-btn icon-btn--edit" title="Modifier">
                          ✎
                        </Link>
                        <button
                          type="button"
                          className="icon-btn icon-btn--danger"
                          title="Supprimer"
                          onClick={() => handleDelete(ouvrier.id, `${ouvrier.firstName} ${ouvrier.lastName}`)}
                        >
                          🗑
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>

          <div className="pagination">
            <div className="pagination-info">
              Affichage de <strong>1</strong> à <strong>{ouvriers.length}</strong> sur <strong>{ouvriers.length}</strong> ouvriers
            </div>
            <div className="pagination-controls">
              <button className="pagination-btn pagination-btn--disabled">Précédent</button>
              <button className="pagination-btn pagination-btn--active">1</button>
              <button className="pagination-btn">2</button>
              <button className="pagination-btn">3</button>
              <button className="pagination-btn">Suivant</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default OuvriersPage;