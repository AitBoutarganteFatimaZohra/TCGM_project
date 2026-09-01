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
  isoString
    ? new Date(isoString).toLocaleDateString('fr-FR')
    : 'N/A';

const TravauxPage = () => {
  const {
    travaux,
    loading,
    error,
    fetchTravaux,
    removeTravaux,
  } = useTravaux();

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
    return (
      <div className="travaux-loading">
        <div className="travaux-spinner"></div>
        <p>Chargement des travaux...</p>
      </div>
    );
  }

  return (
    <div className="travaux-page">

      {/* ================= HEADER ================= */}
      <div className="travaux-header">
        <div className="travaux-header-content">
          <div className="travaux-title-wrapper">
            <div className="travaux-title-icon">
              🔧
            </div>

            <div>
              <h1>Travaux</h1>
              <p>Gérez et suivez les travaux de vos chantiers</p>
            </div>
          </div>

          <Link
            to="/travaux/nouveau"
            className="travaux-btn-primary"
          >
            <span className="btn-icon">+</span>
            Nouveaux travaux
          </Link>
        </div>
      </div>

      {/* ================= FILTRES ================= */}
      <div className="travaux-filters-card">

        <div className="travaux-filters-header">
          <div>
            <h3>Filtres</h3>
            <p>Affinez la liste des travaux</p>
          </div>

          {(chantierFilter || statutFilter) && (
            <button
              className="clear-filters-btn"
              onClick={() => {
                setChantierFilter('');
                setStatutFilter('');
              }}
            >
              Réinitialiser
            </button>
          )}
        </div>

        <div className="travaux-filters">

          <div className="filter-group">
            <label htmlFor="chantier-filter">
              Chantier
            </label>

            <div className="select-wrapper">
              <span className="select-icon">🏗️</span>

              <select
                id="chantier-filter"
                className="travaux-filter-select"
                value={chantierFilter}
                onChange={(e) =>
                  setChantierFilter(e.target.value)
                }
              >
                <option value="">
                  Tous les chantiers
                </option>

                {chantiers.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="filter-group">
            <label htmlFor="statut-filter">
              Statut
            </label>

            <div className="select-wrapper">
              <span className="select-icon">📊</span>

              <select
                id="statut-filter"
                className="travaux-filter-select"
                value={statutFilter}
                onChange={(e) =>
                  setStatutFilter(e.target.value)
                }
              >
                <option value="">
                  Tous les statuts
                </option>

                {STATUTS.map((s) => (
                  <option key={s.value} value={s.value}>
                    {s.label}
                  </option>
                ))}
              </select>
            </div>
          </div>

        </div>
      </div>

      {/* ================= ERREUR ================= */}
      {error && (
        <div className="travaux-error">
          <span className="error-icon">!</span>

          <div>
            <strong>Une erreur est survenue</strong>
            <p>{error}</p>
          </div>
        </div>
      )}

      {/* ================= CONTENU ================= */}
      {travaux.length === 0 ? (

        <div className="travaux-empty">

          <div className="empty-icon">
            🔧
          </div>

          <h2>Aucun travaux trouvé</h2>

          <p>
            Aucun travail ne correspond aux critères
            sélectionnés.
          </p>

          <Link
            to="/travaux/nouveau"
            className="travaux-btn-primary"
          >
            <span className="btn-icon">+</span>
            Créer les premiers travaux
          </Link>

        </div>

      ) : (

        <div className="travaux-table-card">

          {/* TABLE HEADER */}
          <div className="table-card-header">

            <div>
              <h2>Liste des travaux</h2>

              <p>
                {travaux.length}{' '}
                {travaux.length > 1
                  ? 'travaux'
                  : 'travail'}
              </p>
            </div>

            {loading && (
              <div className="table-loading">
                <div className="small-spinner"></div>
                Mise à jour...
              </div>
            )}

          </div>

          {/* TABLE */}
          <div className="table-responsive">

            <table className="travaux-table">

              <thead>
                <tr>
                  <th>Code</th>
                  <th>Intitulé</th>
                  <th>Chantier</th>
                  <th>Priorité</th>
                  <th>Fin prévue</th>
                  <th>Tâches</th>
                  <th>Statut</th>
                  <th className="actions-column">
                    Actions
                  </th>
                </tr>
              </thead>

              <tbody>

                {travaux.map((t) => {

                  const statut =
                    STATUTS.find(
                      (s) => s.value === t.statut
                    );

                  const totalTaches =
                    t.totalTaches ?? 0;

                  const totalTerminees =
                    t.totalTachesTerminees ?? 0;

                  const progress =
                    totalTaches > 0
                      ? Math.round(
                          (totalTerminees /
                            totalTaches) *
                            100
                        )
                      : 0;

                  return (
                    <tr key={t.id}>

                      {/* CODE */}
                      <td>
                        <span className="travaux-code">
                          {t.code}
                        </span>
                      </td>

                      {/* INTITULE */}
                      <td>
                        <div className="travaux-name">
                          <strong>
                            {t.intitule}
                          </strong>
                        </div>
                      </td>

                      {/* CHANTIER */}
                      <td>
                        <div className="chantier-cell">
                          <span className="chantier-icon">
                            🏗️
                          </span>

                          <span>
                            {t.chantier?.name || 'N/A'}
                          </span>
                        </div>
                      </td>

                      {/* PRIORITE */}
                      <td>
                        <span
                          className={`priority-badge priority-${String(
                            t.priorite || ''
                          ).toLowerCase()}`}
                        >
                          {t.priorite ?? '—'}
                        </span>
                      </td>

                      {/* DATE */}
                      <td>
                        <span className="date-cell">
                          📅 {formatDate(t.dateFinPrevue)}
                        </span>
                      </td>

                      {/* TACHES */}
                      <td>

                        <div className="tasks-cell">

                          <div className="tasks-numbers">
                            <strong>
                              {totalTerminees}
                            </strong>
                            <span>
                              / {totalTaches}
                            </span>
                          </div>

                          <div className="progress-bar">
                            <div
                              className="progress-fill"
                              style={{
                                width: `${progress}%`,
                              }}
                            ></div>
                          </div>

                        </div>

                      </td>

                      {/* STATUT */}
                      <td>

                        <span
                          className={`travaux-status status-${(
                            t.statut || ''
                          ).toLowerCase()}`}
                        >
                          <span className="status-dot"></span>

                          {statut?.label ||
                            t.statut ||
                            'N/A'}
                        </span>

                      </td>

                      {/* ACTIONS */}
                      <td className="actions-cell">

                        <div className="action-buttons">

                          <Link
                            to={`/travaux/${t.id}`}
                            className="action-btn action-view"
                            title="Voir les détails"
                          >
                            👁️
                          </Link>

                          <Link
                            to={`/travaux/edit/${t.id}`}
                            className="action-btn action-edit"
                            title="Modifier"
                          >
                            ✏️
                          </Link>

                          <button
                            className="action-btn action-delete"
                            title="Supprimer"
                            onClick={() =>
                              handleDelete(
                                t.id,
                                t.code
                              )
                            }
                          >
                            🗑️
                          </button>

                        </div>

                      </td>

                    </tr>
                  );
                })}

              </tbody>

            </table>

          </div>

        </div>

      )}

    </div>
  );
};

export default TravauxPage;