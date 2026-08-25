import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import useAffectations from '../hooks/useAffectations';
import { getChantiers } from '../api/chantierApi';
import { getOuvriers } from '../api/ouvrierApi';

const STATUTS = ['PLANIFIEE', 'EN_COURS', 'TERMINEE', 'ANNULEE', 'EN_ATTENTE_VALIDATION', 'REJETEE'];

const STATUT_LABELS = {
  PLANIFIEE: 'Planifiée',
  EN_COURS: 'En cours',
  TERMINEE: 'Terminée',
  ANNULEE: 'Annulée',
  EN_ATTENTE_VALIDATION: 'En attente de validation',
  REJETEE: 'Rejetée',
};

const STATUT_BADGE_CLASS = {
  PLANIFIEE: 'badge badge--neutral',
  EN_COURS: 'badge badge--success',
  TERMINEE: 'badge badge--neutral',
  ANNULEE: 'badge badge--danger',
  EN_ATTENTE_VALIDATION: 'badge badge--warning',
  REJETEE: 'badge badge--danger',
};

const PAGE_SIZE = 10;

const AffectationsPage = () => {
  const { affectations, loading, error, fetchAffectations, removeAffectation } = useAffectations();

  const [chantiers, setChantiers] = useState([]);
  const [chantierFilter, setChantierFilter] = useState('');
  const [statutFilter, setStatutFilter] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  useEffect(() => {
    getChantiers().then((data) => setChantiers(data.content || data)).catch(() => {});
    getOuvriers().catch(() => {});
  }, []);

  useEffect(() => {
    const params = { page, size: PAGE_SIZE };
    if (chantierFilter) params.chantierId = chantierFilter;
    if (statutFilter) params.statut = statutFilter;

    fetchAffectations(params).then((data) => {
      if (data && typeof data.totalPages === 'number') {
        setTotalPages(data.totalPages);
        setTotalElements(data.totalElements);
      }
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, chantierFilter, statutFilter]);

  const handleDelete = async (id) => {
    if (window.confirm('Supprimer cette affectation ?')) {
      await removeAffectation(id);
    }
  };

  const ouvrierName = (o) => (o ? `${o.firstName} ${o.lastName}` : '—');

  return (
    <div className="affectations-page">
      <div className="page-header">
        <h1>
          📌 Affectations
          {totalElements > 0 && <span className="counter-badge">{totalElements}</span>}
        </h1>
        <Link to="/affectations/nouveau" className="btn-primary">
          + Nouvelle affectation
        </Link>
      </div>

      <div className="filters">
        <select
          className="filter-select"
          value={chantierFilter}
          onChange={(e) => {
            setChantierFilter(e.target.value);
            setPage(0);
          }}
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
          onChange={(e) => {
            setStatutFilter(e.target.value);
            setPage(0);
          }}
        >
          <option value="">Tous les statuts</option>
          {STATUTS.map((s) => (
            <option key={s} value={s}>
              {STATUT_LABELS[s]}
            </option>
          ))}
        </select>
      </div>

      {error && <div className="error-banner">{error}</div>}

      {loading ? (
        <div className="loading">Chargement...</div>
      ) : affectations.length === 0 ? (
        <div className="empty-state">
          <p>Aucune affectation trouvée</p>
          <Link to="/affectations/nouveau" className="btn-primary">
            Créer la première affectation
          </Link>
        </div>
      ) : (
        <div className="table-container">
          <table className="ouvriers-table">
            <thead>
              <tr>
                <th>Ouvrier</th>
                <th>Chantier</th>
                <th>Date début</th>
                <th>Date fin</th>
                <th>Statut</th>
                <th className="col-actions">Actions</th>
              </tr>
            </thead>
            <tbody>
              {affectations.map((a) => (
                <tr key={a.id}>
                  <td>
                    {ouvrierName(a.ouvrier)}
                    {a.ouvrier?.cin && <div className="activity-meta">{a.ouvrier.cin}</div>}
                  </td>
                  <td>
                    {a.chantier?.name || '—'}
                    {a.chantier?.reference && <div className="activity-meta">{a.chantier.reference}</div>}
                  </td>
                  <td>{a.dateDebut}</td>
                  <td>{a.dateFin || '—'}</td>
                  <td>
                    <span className={STATUT_BADGE_CLASS[a.statut] || 'badge badge--neutral'}>
                      {STATUT_LABELS[a.statut] || a.statut}
                    </span>
                  </td>
                  <td className="col-actions">
                    <div className="row-actions">
                      <Link to={`/affectations/${a.id}`} className="icon-btn icon-btn--view" title="Voir">
                        👁
                      </Link>
                      <Link to={`/affectations/${a.id}/modifier`} className="icon-btn icon-btn--edit" title="Modifier">
                        ✎
                      </Link>
                      <button
                        className="icon-btn icon-btn--danger"
                        title="Supprimer"
                        onClick={() => handleDelete(a.id)}
                      >
                        🗑
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {totalPages > 1 && (
            <div className="pagination">
              <div className="pagination-info">
                Page <strong>{page + 1}</strong> sur <strong>{totalPages}</strong>
              </div>
              <div className="pagination-controls">
                <button
                  className={`pagination-btn ${page === 0 ? 'pagination-btn--disabled' : ''}`}
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                  disabled={page === 0}
                >
                  ‹
                </button>
                {Array.from({ length: totalPages }, (_, i) => (
                  <button
                    key={i}
                    className={`pagination-btn ${i === page ? 'pagination-btn--active' : ''}`}
                    onClick={() => setPage(i)}
                  >
                    {i + 1}
                  </button>
                ))}
                <button
                  className={`pagination-btn ${page === totalPages - 1 ? 'pagination-btn--disabled' : ''}`}
                  onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                  disabled={page === totalPages - 1}
                >
                  ›
                </button>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default AffectationsPage;