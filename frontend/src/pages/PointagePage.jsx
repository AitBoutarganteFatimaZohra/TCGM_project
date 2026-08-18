import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import usePointage from '../hooks/usePointage';
import { getChantiers } from '../api/chantierApi';

const STATUTS = ['EN_ATTENTE', 'VALIDE', 'REJETE'];

const STATUT_LABELS = {
  EN_ATTENTE: 'En attente',
  VALIDE: 'Validé',
  REJETE: 'Rejeté',
};

const STATUT_BADGE_CLASS = {
  EN_ATTENTE: 'badge badge--neutral',
  VALIDE: 'badge badge--success',
  REJETE: 'badge badge--danger',
};

const PAGE_SIZE = 10;

const PointagePage = () => {
  const { dossiers, loading, error, fetchDossiers, removeDossier } = usePointage();

  const [chantiers, setChantiers] = useState([]);
  const [siteFilter, setSiteFilter] = useState('');
  const [dateFilter, setDateFilter] = useState('');
  const [statutFilter, setStatutFilter] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  useEffect(() => {
    getChantiers().then((data) => setChantiers(data.content || data)).catch(() => {});
  }, []);

  useEffect(() => {
    const params = { page, size: PAGE_SIZE };
    if (siteFilter) params.siteId = siteFilter;
    if (dateFilter) params.date = dateFilter;
    if (statutFilter) params.status = statutFilter;

    fetchDossiers(params).then((data) => {
      if (data && typeof data.totalPages === 'number') {
        setTotalPages(data.totalPages);
        setTotalElements(data.totalElements);
      }
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, siteFilter, dateFilter, statutFilter]);

  const handleDelete = async (id) => {
    if (window.confirm('Supprimer ce dossier de pointage ?')) {
      await removeDossier(id);
    }
  };

  return (
    <div className="pointage-page">
      <div className="page-header">
        <h1>
          🕐 Pointage
          {totalElements > 0 && <span className="counter-badge">{totalElements}</span>}
        </h1>
        <Link to="/pointage/nouveau" className="btn-primary">
          + Nouveau pointage
        </Link>
      </div>

      <div className="filters">
        <select
          className="filter-select"
          value={siteFilter}
          onChange={(e) => {
            setSiteFilter(e.target.value);
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

        <input
          type="date"
          className="filter-select"
          value={dateFilter}
          onChange={(e) => {
            setDateFilter(e.target.value);
            setPage(0);
          }}
        />

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
      ) : dossiers.length === 0 ? (
        <div className="empty-state">
          <p>Aucun pointage trouvé</p>
          <Link to="/pointage/nouveau" className="btn-primary">
            Créer le premier pointage
          </Link>
        </div>
      ) : (
        <div className="table-container">
          <table className="ouvriers-table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Chantier</th>
                <th>Ouvriers</th>
                <th>Heures</th>
                <th>Statut</th>
                <th className="col-actions">Actions</th>
              </tr>
            </thead>
            <tbody>
              {dossiers.map((d) => (
                <tr key={d.id}>
                  <td>{d.date}</td>
                  <td>
                    {d.site?.name || '—'}
                    {d.site?.reference && <div className="activity-meta">{d.site.reference}</div>}
                  </td>
                  <td>{d.totalOuvriers ?? 0}</td>
                  <td>{d.totalHeures ?? 0} h</td>
                  <td>
                    <span className={STATUT_BADGE_CLASS[d.status] || 'badge badge--neutral'}>
                      {STATUT_LABELS[d.status] || d.status}
                    </span>
                  </td>
                  <td className="col-actions">
                    <div className="row-actions">
                      <Link to={`/pointage/${d.id}`} className="icon-btn icon-btn--view" title="Voir">
                        👁
                      </Link>
                      {d.status === 'EN_ATTENTE' && (
                        <>
                          <Link to={`/pointage/${d.id}/modifier`} className="icon-btn icon-btn--edit" title="Modifier">
                            ✎
                          </Link>
                          <button
                            className="icon-btn icon-btn--danger"
                            title="Supprimer"
                            onClick={() => handleDelete(d.id)}
                          >
                            🗑
                          </button>
                        </>
                      )}
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

export default PointagePage;