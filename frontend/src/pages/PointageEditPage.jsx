import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import {
  getDossierPointageById,
  updateDossierPointage,
  addLignePointage,
  removeLignePointage,
} from '../api/pointageApi';
import { getOuvriers } from '../api/ouvrierApi';
import { getTachesBySite } from '../api/tacheApi';
import { formatDateFr, formatHoraire, buildHalfDayTimes } from '../utils/pointageFormat';

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

// ✅ Rôles autorisés à modifier un pointage
const ROLES_EDITION = ['ADMIN', 'CHEF_PROJET', 'AGENT_SAISIE'];

const PointageEditPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const canEdit = ROLES_EDITION.includes(user?.role);

  const [dossier, setDossier] = useState(null);
  const [ouvriers, setOuvriers] = useState([]);
  const [taches, setTaches] = useState([]);
  const [notes, setNotes] = useState('');

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [blocked, setBlocked] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const [ligneForm, setLigneForm] = useState(emptyLigne);
  const [editingLigneId, setEditingLigneId] = useState(null);
  const [ligneError, setLigneError] = useState(null);
  const [ligneSubmitting, setLigneSubmitting] = useState(false);

  const loadDossier = async () => {
    const d = await getDossierPointageById(id);
    setDossier(d);
    setNotes(d.notes || '');
    if (d.status !== 'EN_ATTENTE') {
      setBlocked(true);
    }
    return d;
  };

  useEffect(() => {
    setLoading(true);
    Promise.all([
      loadDossier(),
      getOuvriers().then((data) => setOuvriers(data.content || data)).catch(() => {}),
    ]).catch(() => setError('Impossible de charger ce dossier de pointage.'))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  useEffect(() => {
    if (!dossier?.site?.id) return;
    getTachesBySite(dossier.site.id).then(setTaches).catch(() => setTaches([]));
  }, [dossier?.site?.id]);

  // ✅ Vérification des droits avant d'afficher la page
  if (!canEdit && !loading) {
    return (
      <div className="pointage-edit-page">
        <div className="page-header">
          <h1>Modifier le pointage</h1>
        </div>
        <div className="error-banner">
          ⛔ Vous n'avez pas le droit de modifier ce pointage.
        </div>
        <Link to={`/pointage/${id}`} className="btn-ghost">Retour au dossier</Link>
      </div>
    );
  }

  const handleSaveNotes = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      await updateDossierPointage(id, { siteId: dossier.site.id, date: dossier.date, notes: notes || null });
      navigate(`/pointage/${id}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Une erreur est survenue.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleLigneFieldChange = (field) => (e) => {
    const value = field === 'halfDay' ? e.target.checked : e.target.value;
    setLigneForm((prev) => {
      const next = { ...prev, [field]: value };
      if (field === 'halfDay' || field === 'halfDaySlot') {
        const halfDayOn = field === 'halfDay' ? value : prev.halfDay;
        const slot = field === 'halfDaySlot' ? value : prev.halfDaySlot;
        if (halfDayOn) {
          const times = buildHalfDayTimes(dossier.date, slot);
          next.startTime = times.startTime;
          next.endTime = times.endTime;
        } else if (field === 'halfDay' && !value) {
          next.startTime = '';
          next.endTime = '';
        }
      }
      return next;
    });
  };

  const startEditLigne = (l) => {
    setEditingLigneId(l.id);
    setLigneForm({
      ouvrierId: String(l.ouvrierId),
      tacheId: String(l.tacheId),
      halfDay: !!l.halfDay,
      halfDaySlot: 'MATIN',
      startTime: l.startTime || '',
      endTime: l.endTime || '',
      notes: l.notes || '',
    });
  };

  const cancelLigneForm = () => {
    setEditingLigneId(null);
    setLigneForm(emptyLigne);
    setLigneError(null);
  };

  const handleSubmitLigne = async (e) => {
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
      if (editingLigneId) {
        await removeLignePointage(editingLigneId);
      }
      await addLignePointage(id, {
        ouvrierId: Number(ligneForm.ouvrierId),
        tacheId: Number(ligneForm.tacheId),
        halfDay: ligneForm.halfDay,
        startTime: ligneForm.startTime,
        endTime: ligneForm.endTime,
        notes: ligneForm.notes || null,
      });
      cancelLigneForm();
      await loadDossier();
    } catch (err) {
      setLigneError(err.response?.data?.message || "Erreur lors de l'enregistrement de la ligne.");
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

  if (loading) return <div className="loading">Chargement...</div>;

  if (blocked) {
    return (
      <div className="pointage-edit-page">
        <div className="page-header">
          <h1>Modifier le pointage</h1>
        </div>
        <div className="error-banner">
          Ce dossier n'est plus modifiable (statut actuel : {dossier?.status}).
        </div>
        <Link to={`/pointage/${id}`} className="btn-ghost">Retour au dossier</Link>
      </div>
    );
  }

  if (error && !dossier) return <div className="error-banner">{error}</div>;
  if (!dossier) return null;

  const ligneTimeInvalid =
    ligneForm.startTime && ligneForm.endTime && !isTimeRangeValid(ligneForm.startTime, ligneForm.endTime);

  return (
    <div className="pointage-edit-page">
      <div className="page-header">
        <h1>Modifier le pointage</h1>
      </div>

      <form className="chantier-form" onSubmit={handleSaveNotes}>
        {error && <div className="error-banner">{error}</div>}

        <div className="form-row">
          <div className="form-group">
            <label>Chantier</label>
            <input type="text" value={`${dossier.site?.name || ''} ${dossier.site?.reference ? `(${dossier.site.reference})` : ''}`} disabled />
          </div>
          <div className="form-group">
            <label>Date</label>
            <input type="text" value={formatDateFr(dossier.date)} disabled />
          </div>
        </div>

        <div className="form-group">
          <label>Notes</label>
          <textarea value={notes} onChange={(e) => setNotes(e.target.value)} />
        </div>

        <div className="form-actions">
          <button type="submit" className="btn-primary" disabled={submitting}>
            {submitting ? 'Enregistrement...' : 'Enregistrer les modifications'}
          </button>
          <button type="button" className="btn-ghost" onClick={() => navigate(`/pointage/${id}`)}>
            Annuler
          </button>
        </div>
      </form>

      <div className="page-header" style={{ marginTop: 24 }}>
        <h1 style={{ fontSize: 18 }}>Ouvriers pointés</h1>
        {!editingLigneId && (
          <button className="btn-primary" onClick={() => setLigneForm(emptyLigne) || setEditingLigneId('new')}>
            + Ajouter un ouvrier
          </button>
        )}
      </div>

      {(editingLigneId) && (
        <form className="chantier-form" onSubmit={handleSubmitLigne} style={{ marginBottom: 20 }}>
          {ligneError && <div className="error-banner">{ligneError}</div>}

          <div className="form-row">
            <div className="form-group">
              <label>Ouvrier *</label>
              <select value={ligneForm.ouvrierId} onChange={handleLigneFieldChange('ouvrierId')} required>
                <option value="">Sélectionner un ouvrier</option>
                {ouvriers.map((o) => (
                  <option key={o.id} value={o.id}>{o.firstName} {o.lastName}</option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label>Tâche *</label>
              <select value={ligneForm.tacheId} onChange={handleLigneFieldChange('tacheId')} required disabled={taches.length === 0}>
                <option value="">{taches.length === 0 ? 'Aucune tâche pour ce chantier' : 'Sélectionner une tâche'}</option>
                {taches.map((t) => (
                  <option key={t.id} value={t.id}>{t.title}</option>
                ))}
              </select>
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>
                <input type="checkbox" checked={ligneForm.halfDay} onChange={handleLigneFieldChange('halfDay')} style={{ marginRight: 8 }} />
                Demi-journée
              </label>
            </div>
            {ligneForm.halfDay && (
              <div className="form-group">
                <label>Créneau</label>
                <select value={ligneForm.halfDaySlot} onChange={handleLigneFieldChange('halfDaySlot')}>
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
              {ligneTimeInvalid && <small style={{ color: '#dc2626' }}>Fin doit être après le début</small>}
            </div>
          </div>

          <div className="form-group">
            <label>Notes</label>
            <textarea value={ligneForm.notes} onChange={handleLigneFieldChange('notes')} />
          </div>

          <div className="form-actions">
            <button type="submit" className="btn-primary" disabled={ligneSubmitting || ligneTimeInvalid}>
              {ligneSubmitting ? 'Enregistrement...' : editingLigneId === 'new' ? 'Ajouter' : 'Enregistrer la ligne'}
            </button>
            <button type="button" className="btn-ghost" onClick={cancelLigneForm}>
              Annuler
            </button>
          </div>
        </form>
      )}

      {(!dossier.lignes || dossier.lignes.length === 0) ? (
        <div className="empty-state"><p>Aucun ouvrier pointé pour ce dossier</p></div>
      ) : (
        <div className="table-container">
          <table className="ouvriers-table">
            <thead>
              <tr>
                <th>Ouvrier</th>
                <th>Tâche</th>
                <th>Horaires</th>
                <th>Notes</th>
                <th className="col-actions">Actions</th>
              </tr>
            </thead>
            <tbody>
              {dossier.lignes.map((l) => (
                <tr key={l.id}>
                  <td>{l.ouvrierName}{l.ouvrierCin && <div className="activity-meta">{l.ouvrierCin}</div>}</td>
                  <td>{l.tacheTitle}</td>
                  <td>{formatHoraire(l.startTime, l.endTime, l.halfDay)}</td>
                  <td>{l.notes || '—'}</td>
                  <td className="col-actions">
                    <div className="row-actions">
                      <button className="icon-btn icon-btn--edit" title="Modifier" onClick={() => startEditLigne(l)}>✎</button>
                      <button className="icon-btn icon-btn--danger" title="Retirer" onClick={() => handleRemoveLigne(l.id)}>🗑</button>
                    </div>
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

export default PointageEditPage;