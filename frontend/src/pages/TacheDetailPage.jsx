import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import useTaches from '../hooks/useTaches';
import { updateTacheStatus, retirerOuvrier } from '../api/tacheApi';

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
    default:
      return 'badge--info';
  }
};

const formatDateTime = (dateStr) => {
  if (!dateStr) return '—';
  return new Date(dateStr).toLocaleString('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

const TacheDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { fetchTacheById, removeTache, loading, error } = useTaches();

  const [tache, setTache] = useState(null);
  const [notFound, setNotFound] = useState(false);
  const [statusUpdating, setStatusUpdating] = useState(false);

  const loadTache = async () => {
    try {
      const data = await fetchTacheById(id);
      setTache(data);
    } catch (err) {
      setNotFound(true);
    }
  };

  useEffect(() => {
    loadTache();
  }, [id]);

  const handleStatusChange = async (newStatus) => {
    setStatusUpdating(true);
    try {
      await updateTacheStatus(id, newStatus);
      await loadTache();
    } catch (err) {
      alert('Erreur lors du changement de statut');
    } finally {
      setStatusUpdating(false);
    }
  };

  const handleDelete = async () => {
    if (window.confirm(`Supprimer la tâche « ${tache.title} » ?`)) {
      try {
        await removeTache(id);
        navigate('/taches');
      } catch (err) {
        alert('Erreur lors de la suppression');
      }
    }
  };

  const handleRetirerOuvrier = async (ouvrierId, nom) => {
    if (window.confirm(`Retirer ${nom} de cette tâche ?`)) {
      try {
        await retirerOuvrier(id, ouvrierId);
        await loadTache();
      } catch (err) {
        alert("Erreur lors du retrait de l'ouvrier");
      }
    }
  };

  if (loading && !tache) {
    return <div className="loading">Chargement de la tâche...</div>;
  }

  if (notFound) {
    return (
      <div className="empty-state">
        <p>Tâche introuvable</p>
        <Link to="/taches" className="btn-primary">
          Retour à la liste
        </Link>
      </div>
    );
  }

  if (!tache) return null;

  return (
    <div className="tache-detail-page">
      <div className="page-header">
        <h1>
          ✅ {tache.title}
          <span className={`badge ${getStatutBadgeClass(tache.status)}`} style={{ marginLeft: 12 }}>
            {STATUTS.find((s) => s.value === tache.status)?.label || tache.status}
          </span>
        </h1>
        <div className="header-actions">
          <Link to="/taches" className="btn-secondary">
            ← Retour
          </Link>
          <Link to={`/taches/${id}/modifier`} className="btn-secondary">
            ✎ Modifier
          </Link>
          <button type="button" className="btn-danger" onClick={handleDelete}>
            🗑 Supprimer
          </button>
        </div>
      </div>

      {error && <div className="error-banner">❌ {error}</div>}

      <div className="detail-grid">
        <div className="detail-card">
          <h2>Informations</h2>
          <div className="detail-row">
            <span className="detail-label">Description</span>
            <span className="detail-value">{tache.description || '—'}</span>
          </div>
          <div className="detail-row">
            <span className="detail-label">Chantier</span>
            <span className="detail-value">{tache.site?.name || '—'}</span>
          </div>
          <div className="detail-row">
            <span className="detail-label">Adresse</span>
            <span className="detail-value">{tache.site?.address || '—'}</span>
          </div>
          <div className="detail-row">
            <span className="detail-label">Priorité</span>
            <span className="detail-value">
              <span className="badge-priorite">P{tache.priority}</span>
            </span>
          </div>
          <div className="detail-row">
            <span className="detail-label">Date prévue</span>
            <span className="detail-value">{formatDateTime(tache.plannedDate)}</span>
          </div>
          <div className="detail-row">
            <span className="detail-label">Date de fin</span>
            <span className="detail-value">{formatDateTime(tache.completedDate)}</span>
          </div>
          <div className="detail-row">
            <span className="detail-label">Créée le</span>
            <span className="detail-value">{formatDateTime(tache.createdAt)}</span>
          </div>
        </div>

        <div className="detail-card">
          <h2>Statut</h2>
          <div className="status-actions">
            {STATUTS.map((s) => (
              <button
                key={s.value}
                type="button"
                className={`btn-status ${tache.status === s.value ? 'btn-status--active' : ''}`}
                disabled={statusUpdating || tache.status === s.value}
                onClick={() => handleStatusChange(s.value)}
              >
                {s.label}
              </button>
            ))}
          </div>
        </div>

        <div className="detail-card detail-card--full">
          <h2>
            Ouvriers affectés
            <span className="counter-badge">{tache.totalOuvriers ?? 0}</span>
          </h2>
          {tache.ouvriers && tache.ouvriers.length > 0 ? (
            <table className="taches-table">
              <thead>
                <tr>
                  <th>Nom</th>
                  <th>CIN</th>
                  <th>Spécialité</th>
                  <th>Affecté le</th>
                  <th className="col-actions">Actions</th>
                </tr>
              </thead>
              <tbody>
                {tache.ouvriers.map((o) => (
                  <tr key={o.id}>
                    <td>
                      <strong>{o.firstName} {o.lastName}</strong>
                    </td>
                    <td className="cell-mono">{o.cin}</td>
                    <td>
                      {o.specialite ? (
                        <span className="badge-specialite">{o.specialite}</span>
                      ) : (
                        '—'
                      )}
                    </td>
                    <td>{formatDateTime(o.assignedAt)}</td>
                    <td className="col-actions">
                      <button
                        type="button"
                        className="icon-btn icon-btn--danger"
                        title="Retirer"
                        onClick={() => handleRetirerOuvrier(o.id, `${o.firstName} ${o.lastName}`)}
                      >
                        🗑
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <p className="empty-inline">Aucun ouvrier affecté à cette tâche</p>
          )}
        </div>
      </div>
    </div>
  );
};

export default TacheDetailPage;