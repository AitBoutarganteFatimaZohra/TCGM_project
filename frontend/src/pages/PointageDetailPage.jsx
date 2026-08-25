import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import {
  getDossierPointageById,
  deleteDossierPointage,
  addLignePointage,
  removeLignePointage,
  soumettreDossierPointage,
  validerDossierPointage,
  rejeterDossierPointage,
} from '../api/pointageApi';
import { getOuvriers } from '../api/ouvrierApi';
import { getTachesBySite } from '../api/tacheApi';
import {
  STATUT_LABELS,
  STATUT_BADGE_CLASS,
  formatDateFr,
  formatHoraire,
  formatTotalHeures,
  getStatusMessage,
  buildHalfDayTimes,
} from '../utils/pointageFormat';

// Rôles autorisés côté backend à valider/rejeter un dossier
const ROLES_VALIDATION = ['ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER'];
// Rôles autorisés à modifier / soumettre un dossier "En attente"
const ROLES_EDITION = ['ADMIN', 'CHEF_PROJET', 'AGENT_SAISIE'];

const emptyLigne = {
  ouvrierId: '',
  tacheId: '',
  halfDay: false,
  halfDaySlot: 'MATIN',
  startTime: '',
  endTime: '',
  notes: '',
};

const isTimeRangeValid = (start, end) => {
  if (!start || !end) return true;
  return new Date(start) < new Date(end);
};

const PointageDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const canValidate = ROLES_VALIDATION.includes(user?.role);
  const canEdit = ROLES_EDITION.includes(user?.role);

  const [dossier, setDossier] = useState(null);
  const [ouvriers, setOuvriers] = useState([]);
  const [taches, setTaches] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [showLigneForm, setShowLigneForm] = useState(false);
  const [ligneForm, setLigneForm] = useState(emptyLigne);
  const [ligneError, setLigneError] = useState(null);
  const [ligneSubmitting, setLigneSubmitting] = useState(false);

  const [showValidation, setShowValidation] = useState(null); // 'valider' | 'rejeter' | null
  const [validationNote, setValidationNote] = useState('');
  const [validationSubmitting, setValidationSubmitting] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const loadDossier = () => {
    return getDossierPointageById(id)
      .then(setDossier)
      .catch(() => setError('Impossible de charger ce dossier de pointage.'));
  };

  useEffect(() => {
    setLoading(true);
    Promise.all([
      loadDossier(),
      getOuvriers().then((data) => setOuvriers(data.content || data)).catch(() => {}),
    ]).finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  useEffect(() => {
    if (!dossier?.site?.id) return;
    getTachesBySite(dossier.site.id).then(setTaches).catch(() => setTaches([]));
  }, [dossier?.site?.id]);

  const isEditable = dossier?.status === 'EN_ATTENTE';
  const isPendingValidation = dossier?.status === 'EN_ATTENTE_VALIDATION';
  const statusMessage = dossier ? getStatusMessage(dossier) : null;

  const handleDeleteDossier = async () => {
    if (window.confirm('Supprimer ce dossier de pointage ?')) {
      await deleteDossierPointage(id);
      navigate('/pointage');
    }
  };

  const handleSubmitDossier = async () => {
    if (!dossier.lignes || dossier.lignes.length === 0) {
      setError('Impossible de soumettre un pointage sans aucun ouvrier.');
      return;
    }
    if (!window.confirm('Soumettre ce pointage pour validation ? Il ne sera plus modifiable ensuite.')) return;
    setSubmitting(true);
    try {
      await soumettreDossierPointage(id);
      await loadDossier();
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la soumission.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleLigneChange = (field) => (e) => {
    const value = field === 'halfDay' ? e.target.checked : e.target.value;
    setLigneForm((prev) => {
      const next = { ...prev, [field]: value };
      if (field === 'halfDay' || field === 'halfDaySlot') {
        if (next.halfDay) {
          const times = buildHalfDayTimes(dossier?.date, next.halfDaySlot);
          next.startTime = times.startTime;
          next.endTime = times.endTime;
        } else {
          next.startTime = '';
          next.endTime = '';
        }
      }
      return next;
    });
  };

  const handleAddLigne = async (e) => {
    e.preventDefault();
    setLigneError(null);

    if (!ligneForm.ouvrierId || !ligneForm.tacheId) {
      setLigneError("Veuillez sélectionner l'ouvrier et la tâche.");
      return;
    }
    if (!ligneForm.startTime || !ligneForm.endTime) {
      setLigneError("Veuillez renseigner l'heure de début et de fin, ou cocher demi-journée.");
      return;
    }
    if (!isTimeRangeValid(ligneForm.startTime, ligneForm.endTime)) {
      setLigneError("L'heure de début doit être antérieure à l'heure de fin.");
      return;
    }

    setLigneSubmitting(true);
    try {
      await addLignePointage(id, {
        ouvrierId: Number(ligneForm.ouvrierId),
        tacheId: Number(ligneForm.tacheId),
        halfDay: ligneForm.halfDay,
        startTime: ligneForm.startTime,
        endTime: ligneForm.endTime,
        notes: ligneForm.notes || null,
      });
      setLigneForm(emptyLigne);
      setShowLigneForm(false);
      await loadDossier();
    } catch (err) {
      setLigneError(err.response?.data?.message || "Erreur lors de l'ajout de la ligne.");
    } finally {
      setLigneSubmitting(false);
    }
  };

  const handleRemoveLigne = async (ligneId) => {
    if (window.confirm('Retirer cet ouvrier du pointage ?')) {
      await removeLignePointage(ligneId);
      await loadDossier();
    }
  };

  const handleValidation = async () => {
    setValidationSubmitting(true);
    try {
      if (showValidation === 'valider') {
        await validerDossierPointage(id, { notes: validationNote || null });
      } else {
        await rejeterDossierPointage(id, { motifRejet: validationNote || null });
      }
      setShowValidation(null);
      setValidationNote('');
      await loadDossier();
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la validation.');
    } finally {
      setValidationSubmitting(false);
    }
  };

  if (loading) return <div className="loading">Chargement...</div>;
  if (error && !dossier) return <div className="error-banner">{error}</div>;
  if (!dossier) return null;

  const ligneTimeInvalid =
    ligneForm.startTime && ligneForm.endTime && !isTimeRangeValid(ligneForm.startTime, ligneForm.endTime);

  const BANNER_CLASS = {
    info: { background: '#e0f2fe', color: '#0369a1', borderColor: '#bae6fd' },
    success: { background: '#dcfce7', color: '#15803d', borderColor: '#bbf7d0' },
    danger: undefined, // classe .error-banner par défaut suffit
  };

  return (
    <div className="pointage-detail-page">
      <div className="page-header">
        <h1>
          Pointage du {formatDateFr(dossier.date)}
          <small>{dossier.site?.name}</small>
        </h1>
        <span className={STATUT_BADGE_CLASS[dossier.status] || 'badge badge--neutral'}>
          {STATUT_LABELS[dossier.status] || dossier.status}
        </span>
      </div>

      {error && <div className="error-banner">{error}</div>}

      {/* Infos générales */}
      <div className="chantier-card" style={{ marginBottom: 20 }}>
        <div className="chantier-body">
          <p><strong>Chantier :</strong> {dossier.site?.name} {dossier.site?.reference ? `(${dossier.site.reference})` : ''}</p>
          <p><strong>Créé par :</strong> {dossier.createdBy ? `${dossier.createdBy.firstName} ${dossier.createdBy.lastName}` : '—'}</p>
          {dossier.status === 'VALIDE' && (
            <p><strong>Validé par :</strong> {dossier.validatedBy ? `${dossier.validatedBy.firstName} ${dossier.validatedBy.lastName}` : '—'} {dossier.validatedAt ? `le ${formatDateFr(dossier.validatedAt)}` : ''}</p>
          )}
          {dossier.notes && <p><strong>Notes :</strong> {dossier.notes}</p>}
          <p><strong>Total :</strong> {dossier.totalOuvriers ?? 0} ouvrier(s) — {formatTotalHeures(dossier.totalHeures)}</p>
        </div>

        <div className="chantier-footer">
          {isEditable && canEdit && (
            <Link to={`/pointage/${id}/modifier`} className="btn-edit">Modifier</Link>
          )}
          {isEditable && canEdit && (
            <button className="btn-delete" onClick={handleDeleteDossier}>Supprimer</button>
          )}
          {isEditable && canEdit && (
            <button className="btn-primary" onClick={handleSubmitDossier} disabled={submitting}>
              {submitting ? 'Envoi...' : 'Soumettre'}
            </button>
          )}
          <Link to="/pointage" className="btn-view">Retour</Link>
        </div>
      </div>

      {/* Message de statut dynamique (§4) */}
      {statusMessage && (
        <div
          className={statusMessage.type === 'danger' ? 'error-banner' : 'error-banner'}
          style={BANNER_CLASS[statusMessage.type]}
        >
          {statusMessage.text}
        </div>
      )}

      {/* Actions de validation — réservées à Admin / Chef de Projet / Chef de Chantier,
          uniquement quand le dossier a été soumis */}
      {isPendingValidation && canValidate && (
        <div className="chantier-card" style={{ marginTop: 20, marginBottom: 20 }}>
          {showValidation ? (
            <div className="form-group">
              <label>{showValidation === 'valider' ? 'Note de validation (optionnel)' : 'Motif du rejet'}</label>
              <textarea
                value={validationNote}
                onChange={(e) => setValidationNote(e.target.value)}
                placeholder={showValidation === 'valider' ? 'Commentaire éventuel...' : 'Expliquez le motif du rejet...'}
              />
              <div className="form-actions">
                <button className="btn-primary" onClick={handleValidation} disabled={validationSubmitting}>
                  {validationSubmitting ? 'Traitement...' : `Confirmer ${showValidation === 'valider' ? 'la validation' : 'le rejet'}`}
                </button>
                <button className="btn-ghost" onClick={() => { setShowValidation(null); setValidationNote(''); }}>
                  Annuler
                </button>
              </div>
            </div>
          ) : (
            <div className="chantier-footer" style={{ borderTop: 'none', paddingTop: 0 }}>
              <button className="btn-primary" onClick={() => setShowValidation('valider')}>
                ✓ Valider le pointage
              </button>
              <button className="btn-delete" onClick={() => setShowValidation('rejeter')}>
                ✕ Rejeter le pointage
              </button>
            </div>
          )}
        </div>
      )}

      {/* Lignes de pointage */}
      <div className="page-header" style={{ marginTop: 20 }}>
        <h1 style={{ fontSize: 18 }}>Ouvriers pointés</h1>
        {isEditable && canEdit && (
          <button className="btn-primary" onClick={() => setShowLigneForm((v) => !v)}>
            {showLigneForm ? 'Fermer' : '+ Ajouter un ouvrier'}
          </button>
        )}
      </div>

      {isEditable && canEdit && showLigneForm && (
        <form className="chantier-form" onSubmit={handleAddLigne} style={{ marginBottom: 20 }}>
          {ligneError && <div className="error-banner">{ligneError}</div>}

          <div className="form-row">
            <div className="form-group">
              <label>Ouvrier *</label>
              <select value={ligneForm.ouvrierId} onChange={handleLigneChange('ouvrierId')} required>
                <option value="">Sélectionner un ouvrier</option>
                {ouvriers.map((o) => (
                  <option key={o.id} value={o.id}>
                    {o.firstName} {o.lastName}
                  </option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label>Tâche *</label>
              <select value={ligneForm.tacheId} onChange={handleLigneChange('tacheId')} required disabled={taches.length === 0}>
                <option value="">{taches.length === 0 ? 'Aucune tâche pour ce chantier' : 'Sélectionner une tâche'}</option>
                {taches.map((t) => (
                  <option key={t.id} value={t.id}>
                    {t.title}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>
                <input type="checkbox" checked={ligneForm.halfDay} onChange={handleLigneChange('halfDay')} style={{ marginRight: 8 }} />
                Demi-journée
              </label>
            </div>
            {ligneForm.halfDay && (
              <div className="form-group">
                <label>Créneau</label>
                <select value={ligneForm.halfDaySlot} onChange={handleLigneChange('halfDaySlot')}>
                  <option value="MATIN">Matin (08:00 → 12:00)</option>
                  <option value="APRES_MIDI">Après-midi (13:00 → 17:00)</option>
                </select>
              </div>
            )}
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>Heure de début *</label>
              <input
                type="time"
                value={ligneForm.startTime ? ligneForm.startTime.slice(11, 16) : ''}
                onChange={(e) => setLigneForm((p) => ({ ...p, startTime: `${dossier.date}T${e.target.value}` }))}
                disabled={ligneForm.halfDay}
                style={ligneTimeInvalid ? { borderColor: '#dc2626' } : undefined}
              />
            </div>
            <div className="form-group">
              <label>Heure de fin *</label>
              <input
                type="time"
                value={ligneForm.endTime ? ligneForm.endTime.slice(11, 16) : ''}
                onChange={(e) => setLigneForm((p) => ({ ...p, endTime: `${dossier.date}T${e.target.value}` }))}
                disabled={ligneForm.halfDay}
                style={ligneTimeInvalid ? { borderColor: '#dc2626' } : undefined}
              />
              {ligneTimeInvalid && (
                <small style={{ color: '#dc2626' }}>Fin doit être après le début</small>
              )}
            </div>
          </div>

          <div className="form-group">
            <label>Notes</label>
            <textarea value={ligneForm.notes} onChange={handleLigneChange('notes')} />
          </div>

          <div className="form-actions">
            <button type="submit" className="btn-primary" disabled={ligneSubmitting || ligneTimeInvalid}>
              {ligneSubmitting ? 'Ajout...' : 'Ajouter'}
            </button>
          </div>
        </form>
      )}

      {(!dossier.lignes || dossier.lignes.length === 0) ? (
        <div className="empty-state">
          <p>Aucun ouvrier pointé pour ce dossier</p>
        </div>
      ) : (
        <div className="table-container">
          <table className="ouvriers-table">
            <thead>
              <tr>
                <th>Ouvrier</th>
                <th>Tâche</th>
                <th>Horaires</th>
                <th>Notes</th>
                {isEditable && canEdit && <th className="col-actions">Actions</th>}
              </tr>
            </thead>
            <tbody>
              {dossier.lignes.map((l) => (
                <tr key={l.id}>
                  <td>
                    {l.ouvrierName}
                    {l.ouvrierCin && <div className="activity-meta">{l.ouvrierCin}</div>}
                  </td>
                  <td>{l.tacheTitle}</td>
                  <td>{formatHoraire(l.startTime, l.endTime, l.halfDay)}</td>
                  <td>{l.notes || '—'}</td>
                  {isEditable && canEdit && (
                    <td className="col-actions">
                      <div className="row-actions">
                        <button className="icon-btn icon-btn--danger" title="Retirer" onClick={() => handleRemoveLigne(l.id)}>
                          🗑
                        </button>
                      </div>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default PointageDetailPage;