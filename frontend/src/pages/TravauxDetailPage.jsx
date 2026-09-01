import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import useTaches from '../hooks/useTaches';
import {
  updateTacheStatus,
  affecterOuvrier,
  retirerOuvrier,
  proposerModificationTache,
  validerModificationTache,
  rejeterModificationTache,
} from '../api/tacheApi';
import { getOuvriers } from '../api/ouvrierApi';

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

const ROLES_VALIDATION = ['ADMIN', 'CHEF_PROJET'];
const ROLES_PROPOSITION = ['ADMIN', 'CHEF_CHANTIER'];
// Rôles autorisés à affecter/retirer un ouvrier d'une tâche (aligné sur le @PreAuthorize backend)
const ROLES_AFFECTATION = ['ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER'];

const TacheDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { fetchTacheById, removeTache, loading, error } = useTaches();

  const canValidate = ROLES_VALIDATION.includes(user?.role);
  const canPropose = ROLES_PROPOSITION.includes(user?.role);
  const canAffecter = ROLES_AFFECTATION.includes(user?.role);

  const [tache, setTache] = useState(null);
  const [notFound, setNotFound] = useState(false);
  const [statusUpdating, setStatusUpdating] = useState(false);

  const [proposedStatus, setProposedStatus] = useState('');
  const [proposedDate, setProposedDate] = useState('');
  const [proposing, setProposing] = useState(false);

  const [showValidation, setShowValidation] = useState(null); // 'valider' | 'rejeter' | null
  const [motif, setMotif] = useState('');
  const [validating, setValidating] = useState(false);

  // ⚠️ NOUVEAU — affectation d'ouvriers à la tâche
  const [availableOuvriers, setAvailableOuvriers] = useState([]);
  const [selectedOuvrierId, setSelectedOuvrierId] = useState('');
  const [affecting, setAffecting] = useState(false);
  const [ouvriersLoading, setOuvriersLoading] = useState(false);

  const loadTache = async () => {
    try {
      const data = await fetchTacheById(id);
      setTache(data);
    } catch (err) {
      setNotFound(true);
    }
  };

  // ⚠️ NOUVEAU — charge la liste des ouvriers disponibles pour le select
  const loadOuvriers = async () => {
    setOuvriersLoading(true);
    try {
      const data = await getOuvriers({ size: 500 });
      setAvailableOuvriers(data.content || data || []);
    } catch (err) {
      console.error('Erreur lors du chargement des ouvriers', err);
    } finally {
      setOuvriersLoading(false);
    }
  };

  useEffect(() => {
    loadTache();
    loadOuvriers();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const isPendingValidation = !!(tache?.proposedStatus || tache?.proposedPlannedDate);

  // Ouvriers pas encore affectés à cette tâche → seuls ceux-là dans le select
  const affectedIds = new Set((tache?.ouvriers || []).map((o) => o.id));
  const selectableOuvriers = availableOuvriers.filter((o) => !affectedIds.has(o.id));

  // Admin/Chef de Projet gardent le changement direct (pas de circuit).
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

  const handleProposer = async (e) => {
    e.preventDefault();
    if (!proposedStatus && !proposedDate) return;

    setProposing(true);
    try {
      await proposerModificationTache(id, {
        status: proposedStatus || undefined,
        plannedDate: proposedDate ? `${proposedDate}T00:00:00` : undefined,
      });
      setProposedStatus('');
      setProposedDate('');
      await loadTache();
    } catch (err) {
      alert(err.response?.data?.message || 'Erreur lors de la proposition.');
    } finally {
      setProposing(false);
    }
  };

  const handleValidation = async () => {
    setValidating(true);
    try {
      if (showValidation === 'valider') {
        await validerModificationTache(id);
      } else {
        await rejeterModificationTache(id, motif || null);
      }
      setShowValidation(null);
      setMotif('');
      await loadTache();
    } catch (err) {
      alert(err.response?.data?.message || 'Erreur lors de la validation.');
    } finally {
      setValidating(false);
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

  // ⚠️ NOUVEAU — affecter un ouvrier sélectionné à la tâche
  const handleAffecterOuvrier = async (e) => {
    e.preventDefault();
    if (!selectedOuvrierId) return;

    setAffecting(true);
    try {
      await affecterOuvrier(id, selectedOuvrierId);
      setSelectedOuvrierId('');
      await loadTache();
    } catch (err) {
      alert(err.response?.data?.message || "Erreur lors de l'affectation de l'ouvrier");
    } finally {
      setAffecting(false);
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

      {/* Modification en attente */}
      {isPendingValidation && (
        <div className="error-banner" style={{ background: '#e0f2fe', color: '#0369a1', borderColor: '#bae6fd', marginBottom: 20 }}>
          Modification en attente de validation par le Chef de Projet :
          {tache.proposedStatus && (
            <> Statut → <strong>{STATUTS.find((s) => s.value === tache.proposedStatus)?.label || tache.proposedStatus}</strong> (déjà appliqué, sera annulé en cas de rejet). </>
          )}
          {tache.proposedPlannedDate && <> Nouvelle date prévue : <strong>{formatDateTime(tache.proposedPlannedDate)}</strong> (pas encore appliquée). </>}
        </div>
      )}

      {!isPendingValidation && tache.rejectionReason && (
        <div className="error-banner" style={{ marginBottom: 20 }}>
          ❌ La dernière proposition de modification a été rejetée. Motif : {tache.rejectionReason}
        </div>
      )}

      {/* Panneau Chef de Projet : valider ou rejeter */}
      {canValidate && isPendingValidation && (
        <div className="detail-card" style={{ marginBottom: 20 }}>
          {showValidation ? (
            <div className="status-actions" style={{ flexDirection: 'column', alignItems: 'stretch' }}>
              <label style={{ marginBottom: 6 }}>{showValidation === 'valider' ? 'Confirmation' : 'Motif du rejet'}</label>
              <textarea
                value={motif}
                onChange={(e) => setMotif(e.target.value)}
                placeholder={showValidation === 'valider' ? 'Commentaire éventuel...' : 'Expliquez le motif du rejet...'}
                rows={3}
              />
              <div style={{ display: 'flex', gap: 8, marginTop: 8 }}>
                <button className="btn-primary" onClick={handleValidation} disabled={validating}>
                  {validating ? 'Traitement...' : `Confirmer ${showValidation === 'valider' ? 'la validation' : 'le rejet'}`}
                </button>
                <button className="btn-secondary" onClick={() => { setShowValidation(null); setMotif(''); }}>
                  Annuler
                </button>
              </div>
            </div>
          ) : (
            <div style={{ display: 'flex', gap: 8 }}>
              <button className="btn-primary" onClick={() => setShowValidation('valider')}>
                ✓ Valider la modification
              </button>
              <button className="btn-danger" onClick={() => setShowValidation('rejeter')}>
                ✕ Rejeter la modification
              </button>
            </div>
          )}
        </div>
      )}

      {/* Panneau Chef de Chantier : proposer une modification */}
      {canPropose && !isPendingValidation && (
        <form className="detail-card" onSubmit={handleProposer} style={{ marginBottom: 20 }}>
          <h2>Proposer une modification</h2>
          <div className="status-actions" style={{ flexWrap: 'wrap', gap: 12 }}>
            <div>
              <label style={{ display: 'block', marginBottom: 4 }}>Nouveau statut</label>
              <select value={proposedStatus} onChange={(e) => setProposedStatus(e.target.value)} className="form-select">
                <option value="">— Aucun changement —</option>
                {STATUTS.filter((s) => s.value !== tache.status).map((s) => (
                  <option key={s.value} value={s.value}>{s.label}</option>
                ))}
              </select>
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: 4 }}>Nouvelle date prévue</label>
              <input type="date" value={proposedDate} onChange={(e) => setProposedDate(e.target.value)} className="form-input" />
            </div>
          </div>
          <div className="form-actions" style={{ marginTop: 12 }}>
            <button type="submit" className="btn-primary" disabled={proposing || (!proposedStatus && !proposedDate)}>
              {proposing ? 'Envoi...' : 'Soumettre pour validation'}
            </button>
          </div>
        </form>
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

        {/* Changement direct réservé Admin/Chef de Projet (hors circuit) */}
        {canValidate && !isPendingValidation && (
          <div className="detail-card">
            <h2>Statut (changement direct)</h2>
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
        )}

        <div className="detail-card detail-card--full">
          <h2>
            Ouvriers affectés
            <span className="counter-badge">{tache.totalOuvriers ?? 0}</span>
          </h2>

          {/* ⚠️ NOUVEAU — formulaire d'affectation d'un ouvrier à la tâche */}
          {canAffecter && (
            <form
              onSubmit={handleAffecterOuvrier}
              style={{ display: 'flex', gap: 8, alignItems: 'center', marginBottom: 16, flexWrap: 'wrap' }}
            >
              <select
                value={selectedOuvrierId}
                onChange={(e) => setSelectedOuvrierId(e.target.value)}
                className="form-select"
                disabled={ouvriersLoading || selectableOuvriers.length === 0}
              >
                <option value="">
                  {ouvriersLoading
                    ? 'Chargement des ouvriers...'
                    : selectableOuvriers.length === 0
                    ? 'Aucun ouvrier disponible'
                    : '— Choisir un ouvrier —'}
                </option>
                {selectableOuvriers.map((o) => (
                  <option key={o.id} value={o.id}>
                    {o.firstName} {o.lastName} {o.cin ? `(${o.cin})` : ''}
                  </option>
                ))}
              </select>
              <button
                type="submit"
                className="btn-primary"
                disabled={affecting || !selectedOuvrierId}
              >
                {affecting ? 'Affectation...' : '＋ Affecter'}
              </button>
            </form>
          )}

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
                      {canAffecter && (
                        <button
                          type="button"
                          className="icon-btn icon-btn--danger"
                          title="Retirer"
                          onClick={() => handleRetirerOuvrier(o.id, `${o.firstName} ${o.lastName}`)}
                        >
                          🗑
                        </button>
                      )}
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