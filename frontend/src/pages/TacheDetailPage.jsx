import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import useTaches from '../hooks/useTaches';
// ⚠️ Adaptez ce chemin/nom si votre hook d'authentification s'appelle
// différemment dans le projet (ex: '../context/AuthContext', '../hooks/useAuth'...).
// Il doit exposer l'utilisateur connecté avec sa propriété `role`
// (ex: 'ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER', ...), comme dans PointageDetailPage.
import useAuth from '../hooks/useAuth';
import { updateTacheStatus, retirerOuvrier, soumettreTache, validerTache, rejeterTache } from '../api/tacheApi';

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
    case 'EN_ATTENTE_VALIDATION':
      return 'badge--pending';
    default:
      return 'badge--info';
  }
};

const getStatutLabel = (status) => {
  if (status === 'EN_ATTENTE_VALIDATION') return 'En attente de validation';
  return STATUTS.find((s) => s.value === status)?.label || status || '—';
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
  const { user } = useAuth();
  const role = user?.role;

  const [tache, setTache] = useState(null);
  const [notFound, setNotFound] = useState(false);
  const [statusUpdating, setStatusUpdating] = useState(false);

  // Formulaire de soumission (Chef de Chantier)
  const [showSoumettreForm, setShowSoumettreForm] = useState(false);
  const [proposedStatus, setProposedStatus] = useState('');
  const [proposedPlannedDate, setProposedPlannedDate] = useState('');

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

  // Override direct — Admin uniquement
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

  // Étape 1 — Chef de Chantier soumet un changement pour validation
  const handleSoumettre = async (e) => {
    e.preventDefault();
    if (!proposedStatus && !proposedPlannedDate) {
      alert('Indiquez un nouveau statut et/ou une nouvelle date prévue');
      return;
    }
    setStatusUpdating(true);
    try {
      await soumettreTache(id, {
        proposedStatus: proposedStatus || undefined,
        proposedPlannedDate: proposedPlannedDate || undefined,
      });
      setShowSoumettreForm(false);
      setProposedStatus('');
      setProposedPlannedDate('');
      await loadTache();
    } catch (err) {
      alert(err?.response?.data?.message || 'Erreur lors de la soumission');
    } finally {
      setStatusUpdating(false);
    }
  };

  // Étape 2a — Chef de Projet valide
  const handleValider = async () => {
    if (!window.confirm('Valider cette demande de changement ?')) return;
    setStatusUpdating(true);
    try {
      await validerTache(id);
      await loadTache();
    } catch (err) {
      alert(err?.response?.data?.message || 'Erreur lors de la validation');
    } finally {
      setStatusUpdating(false);
    }
  };

  // Étape 2b — Chef de Projet rejette
  const handleRejeter = async () => {
    const motif = window.prompt('Motif du rejet (optionnel) :', '');
    if (motif === null) return; // annulé
    setStatusUpdating(true);
    try {
      await rejeterTache(id, motif);
      await loadTache();
    } catch (err) {
      alert(err?.response?.data?.message || 'Erreur lors du rejet');
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

  const isPending = tache.status === 'EN_ATTENTE_VALIDATION';
  const canSoumettre = (role === 'CHEF_CHANTIER' || role === 'ADMIN') && !isPending;
  const canValiderOuRejeter = (role === 'CHEF_PROJET' || role === 'ADMIN') && isPending;
  const canOverrideAdmin = role === 'ADMIN' && !isPending;

  return (
    <div className="tache-detail-page">
      <div className="page-header">
        <h1>
          ✅ {tache.title}
          <span className={`badge ${getStatutBadgeClass(tache.status)}`} style={{ marginLeft: 12 }}>
            {getStatutLabel(tache.status)}
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

      {isPending && (
        <div className="info-banner">
          ⏳ Cette tâche a une demande de changement en attente de validation par le Chef de Projet.
          {tache.proposedStatus && (
            <> Nouveau statut proposé : <strong>{getStatutLabel(tache.proposedStatus)}</strong>.</>
          )}
          {tache.proposedPlannedDate && (
            <> Nouvelle date proposée : <strong>{formatDateTime(tache.proposedPlannedDate)}</strong>.</>
          )}
        </div>
      )}

      {!isPending && tache.rejectionReason && (
        <div className="error-banner">
          ⚠️ Dernière demande rejetée. Motif : {tache.rejectionReason}
        </div>
      )}

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
          <h2>Statut &amp; validation</h2>

          {/* Override direct — Admin uniquement */}
          {canOverrideAdmin && (
            <>
              <p className="cell-subtext">Changement direct (Administrateur) :</p>
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
            </>
          )}

          {/* Étape 1 — Chef de Chantier */}
          {canSoumettre && role === 'CHEF_CHANTIER' && (
            <>
              {!showSoumettreForm ? (
                <button
                  type="button"
                  className="btn-primary"
                  onClick={() => {
                    setProposedStatus(tache.status);
                    setShowSoumettreForm(true);
                  }}
                >
                  Soumettre un changement pour validation
                </button>
              ) : (
                <form className="form-card" onSubmit={handleSoumettre}>
                  <div className="form-group">
                    <label className="form-label">Nouveau statut</label>
                    <select
                      className="form-select"
                      value={proposedStatus}
                      onChange={(e) => setProposedStatus(e.target.value)}
                    >
                      <option value="">— Ne pas changer —</option>
                      {STATUTS.map((s) => (
                        <option key={s.value} value={s.value}>
                          {s.label}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div className="form-group">
                    <label className="form-label">Nouvelle date prévue</label>
                    <input
                      type="datetime-local"
                      className="form-input"
                      value={proposedPlannedDate}
                      onChange={(e) => setProposedPlannedDate(e.target.value)}
                    />
                  </div>
                  <div className="form-actions">
                    <button
                      type="button"
                      className="btn-secondary"
                      onClick={() => setShowSoumettreForm(false)}
                    >
                      Annuler
                    </button>
                    <button type="submit" className="btn-primary" disabled={statusUpdating}>
                      {statusUpdating ? 'Envoi...' : 'Soumettre pour validation'}
                    </button>
                  </div>
                </form>
              )}
            </>
          )}

          {/* Étape 2 — Chef de Projet */}
          {canValiderOuRejeter && (
            <div className="status-actions">
              <button
                type="button"
                className="btn-primary"
                disabled={statusUpdating}
                onClick={handleValider}
              >
                ✔ Valider
              </button>
              <button
                type="button"
                className="btn-danger"
                disabled={statusUpdating}
                onClick={handleRejeter}
              >
                ✘ Rejeter
              </button>
            </div>
          )}

          {!canOverrideAdmin && !canSoumettre && !canValiderOuRejeter && (
            <p className="empty-inline">
              {isPending
                ? 'En attente de la décision du Chef de Projet.'
                : 'Vous n\'avez pas de statut à faire évoluer sur cette tâche.'}
            </p>
          )}
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