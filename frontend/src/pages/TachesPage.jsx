import { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import useTaches from '../hooks/useTaches';
import useTravaux from '../hooks/useTravaux';

const STATUTS = [
  { value: 'PLANIFIEE', label: 'Planifiée' },
  { value: 'EN_COURS', label: 'En cours' },
  { value: 'TERMINEE', label: 'Terminée' },
];

const getStatutBadgeClass = (status) => {
  switch (status) {
    case 'TERMINEE':
      return 'badge--success';
    case 'EN_COURS':
      return 'badge--warning';
    case 'PLANIFIEE':
    default:
      return 'badge--info';
  }
};

const getStatutLabel = (status) => {
  return STATUTS.find((s) => s.value === status)?.label || status || '—';
};

const getTravauxLabel = (t) => {
  const nom = t.title || t.description || t.reference || `Travaux #${t.id}`;
  const chantier = t.chantier?.name || t.chantierName;
  return chantier ? `${nom} — ${chantier}` : nom;
};

const formatDate = (dateStr) => {
  if (!dateStr) return '—';
  const d = new Date(dateStr);
  return d.toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' });
};

const TachesPage = () => {
  const { taches, loading, error, fetchTaches, removeTache } = useTaches();
  const { travaux } = useTravaux();

  const [search, setSearch] = useState('');
  const [travauxFilter, setTravauxFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [hasLoadedOnce, setHasLoadedOnce] = useState(false);
  const debounceRef = useRef(null);

  useEffect(() => {
    clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      fetchTaches({
        search: search || undefined,
        travauxId: travauxFilter || undefined,
        status: statusFilter || undefined,
      }).finally(() => setHasLoadedOnce(true));
    }, 400);

    return () => clearTimeout(debounceRef.current);
  }, [search, travauxFilter, statusFilter]);

  const handleDelete = async (id, titre) => {
    if (window.confirm(`Supprimer la tâche « ${titre} » ?`)) {
      try {
        await removeTache(id);
      } catch (err) {
        alert('Erreur lors de la suppression');
      }
    }
  };

  if (loading && !hasLoadedOnce) {
    return <div className="loading">Chargement des tâches...</div>;
  }

  return (
    <div className="taches-page">
      <div className="page-header">
        <h1>
          ✅ Tâches
          <span className="counter-badge">{taches.length}</span>
        </h1>
        <Link to="/taches/nouveau" className="btn-primary">
          + Nouvelle tâche
        </Link>
      </div>

      <div className="filters">
        <div className="search-wrapper">
          <span className="search-icon">🔍</span>
          <input
            type="text"
            className="search-input"
            placeholder="Rechercher par titre ou description..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <select
          className="filter-select"
          value={travauxFilter}
          onChange={(e) => setTravauxFilter(e.target.value)}
        >
          <option value="">Tous les travaux</option>
          {travaux.map((t) => (
            <option key={t.id} value={t.id}>
              {getTravauxLabel(t)}
            </option>
          ))}
        </select>
        <select
          className="filter-select"
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
        >
          <option value="">Tous les statuts</option>
          {STATUTS.map((s) => (
            <option key={s.value} value={s.value}>
              {s.label}
            </option>
          ))}
        </select>
      </div>

      {error && <div className="error-banner">❌ {error}</div>}

      {taches.length === 0 ? (
        <div className="empty-state">
          <p>Aucune tâche trouvée</p>
          <Link to="/taches/nouveau" className="btn-primary">
            Créer la première tâche
          </Link>
        </div>
      ) : (
        <div className="table-container">
          <table className="taches-table">
            <thead>
              <tr>
                <th>Titre</th>
                <th>Chantier</th>
                <th>Date prévue</th>
                <th>Priorité</th>
                <th>Ouvriers</th>
                <th>Statut</th>
                <th className="col-actions">Actions</th>
              </tr>
            </thead>
            <tbody>
              {taches.map((tache) => (
                <tr key={tache.id}>
                  <td>
                    <strong>{tache.title}</strong>
                    {tache.description && (
                      <div className="cell-subtext">{tache.description}</div>
                    )}
                  </td>
                  <td>
                    {tache.site ? (
                      <span className="badge-site">{tache.site.name}</span>
                    ) : (
                      <span className="badge-site badge-site--disponible">—</span>
                    )}
                  </td>
                  <td>{formatDate(tache.plannedDate)}</td>
                  <td>
                    <span className="badge-priorite">P{tache.priority ?? '—'}</span>
                  </td>
                  <td>{tache.totalOuvriers ?? 0}</td>
                  <td>
                    <span className={`badge ${getStatutBadgeClass(tache.status)}`}>
                      {getStatutLabel(tache.status)}
                    </span>
                  </td>
                  <td className="col-actions">
                    <div className="row-actions">
                      <Link to={`/taches/${tache.id}`} className="icon-btn icon-btn--view" title="Voir">
                        👁
                      </Link>
                      <Link to={`/taches/${tache.id}/modifier`} className="icon-btn icon-btn--edit" title="Modifier">
                        ✎
                      </Link>
                      <button
                        type="button"
                        className="icon-btn icon-btn--danger"
                        title="Supprimer"
                        onClick={() => handleDelete(tache.id, tache.title)}
                      >
                        🗑
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          <div className="pagination">
            <div className="pagination-info">
              Affichage de <strong>1</strong> à <strong>{taches.length}</strong> sur <strong>{taches.length}</strong> tâches
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

export default TachesPage;