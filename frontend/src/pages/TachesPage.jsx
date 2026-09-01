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
  return (
    STATUTS.find((s) => s.value === status)?.label ||
    status ||
    '—'
  );
};

const getTravauxLabel = (t) => {
  const nom =
    t.title ||
    t.description ||
    t.reference ||
    `Travaux #${t.id}`;

  const chantier =
    t.chantier?.name ||
    t.chantierName;

  return chantier
    ? `${nom} — ${chantier}`
    : nom;
};

const formatDate = (dateStr) => {
  if (!dateStr) return '—';

  const d = new Date(dateStr);

  return d.toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });
};

const TachesPage = () => {
  const {
    taches,
    loading,
    error,
    fetchTaches,
    removeTache,
  } = useTaches();

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
      }).finally(() => {
        setHasLoadedOnce(true);
      });
    }, 400);

    return () => clearTimeout(debounceRef.current);
  }, [search, travauxFilter, statusFilter]);

  const handleDelete = async (id, titre) => {
    if (
      window.confirm(
        `Supprimer la tâche « ${titre} » ?`
      )
    ) {
      try {
        await removeTache(id);
      } catch (err) {
        alert('Erreur lors de la suppression');
      }
    }
  };

  if (loading && !hasLoadedOnce) {
    return (
      <div className="taches-loading">
        <div className="taches-spinner"></div>
        <p>Chargement des tâches...</p>
      </div>
    );
  }

  return (
    <div className="taches-modern-page">

      {/* =====================================================
          HEADER
      ===================================================== */}

      <div className="taches-header">

        <div className="taches-header-left">

          <div className="taches-header-icon">
            ✓
          </div>

          <div>
            <div className="taches-breadcrumb">
              Gestion
              <span>›</span>
              Tâches
            </div>

            <div className="taches-title-line">

              <h1>
                Tâches
              </h1>

              <span className="taches-counter">
                {taches.length}
              </span>

            </div>

            <p className="taches-header-description">
              Gérez et suivez les tâches associées
              à vos travaux
            </p>
          </div>

        </div>


        <Link
          to="/taches/nouveau"
          className="taches-add-btn"
        >
          <span>＋</span>
          Nouvelle tâche
        </Link>

      </div>


      {/* =====================================================
          FILTERS
      ===================================================== */}

      <div className="taches-filter-card">

        <div className="taches-filter-title">
          <span>☰</span>
          Filtres
        </div>


        <div className="taches-filters">

          {/* RECHERCHE */}

          <div className="taches-search-wrapper">

            <span className="taches-search-icon">
              🔍
            </span>

            <input
              type="text"
              className="taches-search-input"
              placeholder="Rechercher une tâche..."
              value={search}
              onChange={(e) =>
                setSearch(e.target.value)
              }
            />

            {search && (
              <button
                type="button"
                className="taches-clear-search"
                onClick={() => setSearch('')}
              >
                ×
              </button>
            )}

          </div>


          {/* TRAVAUX */}

          <div className="taches-filter-select-wrapper">

            <label>
              Travaux
            </label>

            <select
              value={travauxFilter}
              onChange={(e) =>
                setTravauxFilter(e.target.value)
              }
            >

              <option value="">
                Tous les travaux
              </option>

              {travaux.map((t) => (
                <option
                  key={t.id}
                  value={t.id}
                >
                  {getTravauxLabel(t)}
                </option>
              ))}

            </select>

          </div>


          {/* STATUT */}

          <div className="taches-filter-select-wrapper">

            <label>
              Statut
            </label>

            <select
              value={statusFilter}
              onChange={(e) =>
                setStatusFilter(e.target.value)
              }
            >

              <option value="">
                Tous les statuts
              </option>

              {STATUTS.map((s) => (
                <option
                  key={s.value}
                  value={s.value}
                >
                  {s.label}
                </option>
              ))}

            </select>

          </div>


          {/* RESET */}

          {(search ||
            travauxFilter ||
            statusFilter) && (

            <button
              type="button"
              className="taches-reset-btn"
              onClick={() => {
                setSearch('');
                setTravauxFilter('');
                setStatusFilter('');
              }}
            >
              Réinitialiser
            </button>

          )}

        </div>

      </div>


      {/* =====================================================
          ERROR
      ===================================================== */}

      {error && (
        <div className="taches-error">

          <span className="taches-error-icon">
            !
          </span>

          <div>
            <strong>
              Une erreur est survenue
            </strong>

            <p>
              {error}
            </p>
          </div>

        </div>
      )}


      {/* =====================================================
          EMPTY STATE
      ===================================================== */}

      {taches.length === 0 ? (

        <div className="taches-empty">

          <div className="taches-empty-icon">
            ✓
          </div>

          <h2>
            Aucune tâche trouvée
          </h2>

          <p>
            {search ||
            travauxFilter ||
            statusFilter
              ? 'Aucune tâche ne correspond aux filtres sélectionnés.'
              : 'Commencez par créer votre première tâche.'}
          </p>

          {search ||
          travauxFilter ||
          statusFilter ? (

            <button
              type="button"
              className="taches-empty-secondary"
              onClick={() => {
                setSearch('');
                setTravauxFilter('');
                setStatusFilter('');
              }}
            >
              Réinitialiser les filtres
            </button>

          ) : (

            <Link
              to="/taches/nouveau"
              className="taches-empty-primary"
            >
              ＋ Créer la première tâche
            </Link>

          )}

        </div>

      ) : (

        /* =====================================================
           TABLE
        ===================================================== */

        <div className="taches-table-card">

          {/* TABLE HEADER */}

          <div className="taches-table-header">

            <div>
              <h2>
                Liste des tâches
              </h2>

              <p>
                {taches.length}{' '}
                {taches.length > 1
                  ? 'tâches'
                  : 'tâche'}{' '}
                trouvée
                {taches.length > 1
                  ? 's'
                  : ''}
              </p>
            </div>

            {loading && (
              <div className="taches-refresh">
                Actualisation...
              </div>
            )}

          </div>


          {/* TABLE */}

          <div className="taches-table-wrapper">

            <table className="taches-modern-table">

              <thead>

                <tr>

                  <th>
                    TÂCHE
                  </th>

                  <th>
                    CHANTIER
                  </th>

                  <th>
                    DATE PRÉVUE
                  </th>

                  <th>
                    PRIORITÉ
                  </th>

                  <th>
                    OUVRIERS
                  </th>

                  <th>
                    STATUT
                  </th>

                  <th className="taches-actions-column">
                    ACTIONS
                  </th>

                </tr>

              </thead>


              <tbody>

                {taches.map((tache) => {

                  const isPending =
                    !!(
                      tache.proposedStatus ||
                      tache.proposedPlannedDate
                    );

                  return (

                    <tr key={tache.id}>

                      {/* TACHE */}

                      <td>

                        <div className="tache-main">

                          <div className="tache-icon">
                            ✓
                          </div>

                          <div className="tache-info">

                            <strong>
                              {tache.title}
                            </strong>

                            {tache.description && (
                              <span>
                                {tache.description}
                              </span>
                            )}

                          </div>

                        </div>

                      </td>


                      {/* CHANTIER */}

                      <td>

                        {tache.site ? (

                          <div className="tache-site">

                            <span className="site-icon">
                              🏗
                            </span>

                            <span>
                              {tache.site.name}
                            </span>

                          </div>

                        ) : (

                          <span className="site-empty">
                            Non affecté
                          </span>

                        )}

                      </td>


                      {/* DATE */}

                      <td>

                        <div className="tache-date">

                          <span className="date-icon">
                            📅
                          </span>

                          {formatDate(
                            tache.plannedDate
                          )}

                        </div>

                      </td>


                      {/* PRIORITE */}

                      <td>

                        <span
                          className={`priority-badge priority-${tache.priority ?? 'none'}`}
                        >
                          {tache.priority
                            ? `P${tache.priority}`
                            : '—'}
                        </span>

                      </td>


                      {/* OUVRIERS */}

                      <td>

                        <div className="workers-count">

                          <span className="workers-icon">
                            👷
                          </span>

                          <strong>
                            {tache.totalOuvriers ?? 0}
                          </strong>

                        </div>

                      </td>


                      {/* STATUT */}

                      <td>

                        <div className="status-cell">

                          <span
                            className={`taches-status-badge ${getStatutBadgeClass(
                              tache.status
                            )}`}
                          >

                            <span className="status-dot"></span>

                            {getStatutLabel(
                              tache.status
                            )}

                          </span>


                          {isPending && (

                            <span className="pending-status">

                              {tache.proposedStatus &&
                              !tache.proposedPlannedDate &&
                                'Validation en attente'}

                              {!tache.proposedStatus &&
                              tache.proposedPlannedDate &&
                                'Date en attente'}

                              {tache.proposedStatus &&
                              tache.proposedPlannedDate &&
                                'Statut + date en attente'}

                            </span>

                          )}

                        </div>

                      </td>


                      {/* ACTIONS */}

                      <td>

                        <div className="tache-row-actions">

                          <Link
                            to={`/taches/${tache.id}`}
                            className="tache-action view"
                            title="Voir"
                          >
                            👁
                          </Link>

                          <Link
                            to={`/taches/${tache.id}/modifier`}
                            className="tache-action edit"
                            title="Modifier"
                          >
                            ✎
                          </Link>

                          <button
                            type="button"
                            className="tache-action delete"
                            title="Supprimer"
                            onClick={() =>
                              handleDelete(
                                tache.id,
                                tache.title
                              )
                            }
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

          </div>


          {/* TABLE FOOTER */}

          <div className="taches-table-footer">

            <span>
              Affichage de{' '}
              <strong>
                {taches.length}
              </strong>{' '}
              tâche
              {taches.length > 1
                ? 's'
                : ''}
            </span>

            <span>
              {loading
                ? 'Mise à jour...'
                : 'Données à jour'}
            </span>

          </div>

        </div>

      )}

    </div>
  );
};

export default TachesPage;