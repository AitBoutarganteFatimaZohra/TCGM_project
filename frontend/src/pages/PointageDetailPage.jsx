import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  getDossierPointageById,
  deleteDossierPointage,
  addLignePointage,
  removeLignePointage,
  validerDossierPointage,
  rejeterDossierPointage,
} from '../api/pointageApi';
import { getOuvriers } from '../api/ouvrierApi';
import { getTaches } from '../api/tacheApi';

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

const emptyLigne = {
  ouvrierId: '',
  tacheId: '',
  halfDay: false,
  startTime: '',
  endTime: '',
  notes: '',
};

const PointageDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();

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
      getTaches().then((data) => setTaches(data.content || data)).catch(() => {}),
    ]).finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const isEditable = dossier?.status === 'EN_ATTENTE';

  const handleDeleteDossier = async () => {
    if (window.confirm('Supprimer ce dossier de pointage ?')) {
      await deleteDossierPointage(id);
      navigate('/pointage');
    }
  };

  const handleLigneChange = (field) => (e) => {
    const value = field === 'halfDay' ? e.target.checked : e.target.value;
    setLigneForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleAddLigne = async (e) => {
    e.preventDefault();
    setLigneError(null);

    if (!ligneForm.ouvrierId || !ligneForm.tacheId) {
      setLigneError("Veuillez sélectionner l'ouvrier et la tâche.");
      return;
    }
    if (!ligneForm.halfDay && (!ligneForm.startTime || !ligneForm.endTime)) {
      setLigneError("Veuillez renseigner l'heure de début et de fin, ou cocher demi-journée.");
      return;
    }

    setLigneSubmitting(true);
    try {
      await addLignePointage(id, {
        ouvrierId: Number(ligneForm.ouvrierId),
        tacheId: Number(ligneForm.tacheId),
        halfDay: ligneForm.halfDay,
        startTime: ligneForm.halfDay ? null : ligneForm.startTime,
        endTime: ligneForm.halfDay ? null : ligneForm.endTime,
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
  if (error) return <div className="error-banner">{error}</div>;
  if (!dossier) return null;

  return (
    <div className="pointage-detail-page">
      <div className="page-header">
        <h1>
          Pointage du {dossier.date}
          <small>{dossier.site?.name}</small>
        </h1>
        <span className={STATUT_BADGE_CLASS[dossier.status] || 'badge badge--neutral'}>
          {STATUT_LABELS[dossier.status] || dossier.status}
        </span>
      </div>

      {/* Infos générales */}
      <div className="chantier-card" style={{ marginBottom: 20 }}>
        <div className="chantier-body">
          <p><strong>Chantier :</strong> {dossier.site?.name} {dossier.site?.reference ? `(${dossier.site.reference})` : ''}</p>
          <p><strong>Créé par :</strong> {dossier.createdBy ? `${dossier.createdBy.firstName} ${dossier.createdBy.lastName}` : '—'}</p>
          {dossier.status !== 'EN_ATTENTE' && (
            <p><strong>Traité par :</strong> {dossier.validatedBy ? `${dossier.validatedBy.firstName} ${dossier.validatedBy.lastName}` : '—'} {dossier.validatedAt ? `le ${dossier.validatedAt}` : ''}</p>
          )}
          {dossier.notes && <p><strong>Notes :</strong> {dossier.notes}</p>}
          <p><strong>Total :</strong> {dossier.totalOuvriers ?? 0} ouvrier(s) — {dossier.totalHeures ?? 0} h</p>
        </div>

        <div className="chantier-footer">
          {isEditable && (
            <Link to={`/pointage/${id}/modifier`} className="btn-edit">Modifier</Link>
          )}
          {isEditable && (
            <button className="btn-delete" onClick={handleDeleteDossier}>Supprimer</button>
          )}
          <Link to="/pointage" className="btn-view">Retour</Link>
        </div>
      </div>

      {/* Actions de validation */}
      {isEditable && (
        <div className="chantier-card" style={{ marginBottom: 20 }}>
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
      <div className="page-header">
        <h1 style={{ fontSize: 18 }}>Ouvriers pointés</h1>
        {isEditable && (
          <button className="btn-primary" onClick={() => setShowLigneForm((v) => !v)}>
            {showLigneForm ? 'Fermer' : '+ Ajouter un ouvrier'}
          </button>
        )}
      </div>

      {isEditable && showLigneForm && (
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
              <select value={ligneForm.tacheId} onChange={handleLigneChange('tacheId')} required>
                <option value="">Sélectionner une tâche</option>
                {taches.map((t) => (
                  <option key={t.id} value={t.id}>
                    {t.title}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="form-group">
            <label>
              <input type="checkbox" checked={ligneForm.halfDay} onChange={handleLigneChange('halfDay')} style={{ marginRight: 8 }} />
              Demi-journée (sans horaires précis)
            </label>
          </div>

          {!ligneForm.halfDay && (
            <div className="form-row">
              <div className="form-group">
                <label>Heure de début *</label>
                <input type="datetime-local" value={ligneForm.startTime} onChange={handleLigneChange('startTime')} />
              </div>
              <div className="form-group">
                <label>Heure de fin *</label>
                <input type="datetime-local" value={ligneForm.endTime} onChange={handleLigneChange('endTime')} />
              </div>
            </div>
          )}

          <div className="form-group">
            <label>Notes</label>
            <textarea value={ligneForm.notes} onChange={handleLigneChange('notes')} />
          </div>

          <div className="form-actions">
            <button type="submit" className="btn-primary" disabled={ligneSubmitting}>
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
                {isEditable && <th className="col-actions">Actions</th>}
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
                  <td>
                    {l.halfDay ? 'Demi-journée' : (l.startTime && l.endTime ? `${l.startTime} → ${l.endTime}` : '—')}
                  </td>
                  <td>{l.notes || '—'}</td>
                  {isEditable && (
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