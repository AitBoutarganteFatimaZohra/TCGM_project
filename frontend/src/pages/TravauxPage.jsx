import { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import useTravaux from '../hooks/useTravaux';
import useChantiers from '../hooks/useChantiers';

const STATUTS = [
  { value: 'PLANIFIE', label: 'Planifié' },
  { value: 'EN_COURS', label: 'En cours' },
  { value: 'TERMINE', label: 'Terminé' },
  { value: 'SUSPENDU', label: 'Suspendu' },
];

const formatDate = (isoString) =>
  isoString ? new Date(isoString).toLocaleDateString('fr-FR') : 'N/A';

const TravauxPage = () => {
  const { travaux, loading, error, fetchTravaux, removeTravaux } = useTravaux();
  const { chantiers } = useChantiers();

  const [chantierFilter, setChantierFilter] = useState('');
  const [statutFilter, setStatutFilter] = useState('');
  const [hasLoadedOnce, setHasLoadedOnce] = useState(false);
  const debounceRef = useRef(null);

  useEffect(() => {
    clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      fetchTravaux({
        chantierId: chantierFilter || undefined,
        statut: statutFilter || undefined,
      }).finally(() => setHasLoadedOnce(true));
    }, 300);

    return () => clearTimeout(debounceRef.current);
  }, [chantierFilter, statutFilter]);

  const handleDelete = async (id, code) => {
    if (window.confirm(`Supprimer les travaux « ${code} » ?`)) {
      try {
        await removeTravaux(id);
      } catch (err) {
        alert('Erreur lors de la suppression');
      }
    }
  };

  if (loading && !hasLoadedOnce) {
    return <div className="loading">Chargement des travaux...</div>;
  }

  return (
    <div className="travaux-page">
      <div className="page-header">
        <h1>🔧 Travaux</h1>
        <Link to="/travaux/nouveau" className="btn-primary">
          + Nouveaux travaux
        </Link>
      </div>

      <div className="filters">
        <select
          className="filter-select"
          value={chantierFilter}
          onChange={(e) => setChantierFilter(e.target.value)}
        >
          <option value="">Tous les chantiers</option>
          {chantiers.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
        <select
          className="filter-select"
          value={statutFilter}
          onChange={(e) => setStatutFilter(e.target.value)}
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

      {travaux.length === 0 ? (
        <div className="empty-state">
          <p>Aucun travaux trouvé</p>
          <Link to="/travaux/nouveau" className="btn-primary">
            Créer les premiers travaux
          </Link>
        </div>
      ) : (
        <div className="table-card">
          <table>
            <thead>
              <tr>
                <th>Code</th>
                <th>Intitulé</th>
                <th>Chantier</th>
                <th>Priorité</th>
                <th>Fin prévue</th>
                <th>Tâches</th>
                <th>Statut</th>
                <th className="col-actions">Actions</th>
              </tr>
            </thead>
            <tbody>
              {travaux.map((t) => (
                <tr key={t.id}>
                  <td className="cell-mono">{t.code}</td>
                  <td><strong>{t.intitule}</strong></td>
                  <td>{t.chantier?.name || 'N/A'}</td>
                  <td>{t.priorite ?? '—'}</td>
                  <td>{formatDate(t.dateFinPrevue)}</td>
                  <td>
                    {t.totalTachesTerminees ?? 0} / {t.totalTaches ?? 0}
                  </td>
                  <td>
                    <span className={`status-badge status-${(t.statut || '').toLowerCase()}`}>
                      {STATUTS.find((s) => s.value === t.statut)?.label || t.statut}
                    </span>
                  </td>
                  <td className="col-actions">
                    <Link to={`/travaux/${t.id}`} className="btn-view">Voir</Link>
                    <Link to={`/travaux/edit/${t.id}`} className="btn-edit">Modifier</Link>
                    <button className="btn-delete" onClick={() => handleDelete(t.id, t.code)}>
                      Supprimer
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default TravauxPage;