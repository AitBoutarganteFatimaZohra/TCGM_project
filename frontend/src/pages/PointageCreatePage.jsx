import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import { getChantiers, getMySites } from '../api/chantierApi';
import { getOuvriers } from '../api/ouvrierApi';
import { getTachesBySite } from '../api/tacheApi';
import { createDossierPointage, addLignePointage } from '../api/pointageApi';
import { buildHalfDayTimes } from '../utils/pointageFormat';

const todayStr = () => new Date().toISOString().slice(0, 10);

let rowIdCounter = 0;
const newRow = () => ({
  rowId: `row-${rowIdCounter++}`,
  ouvrierId: '',
  tacheId: '',
  halfDay: false,
  halfDaySlot: 'MATIN',
  startTime: '',
  endTime: '',
  notes: '',
  status: null,
  errorMsg: null,
});

const isTimeRangeValid = (start, end) => {
  if (!start || !end) return true;
  return new Date(start) < new Date(end);
};

const PointageCreatePage = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const isAgentSaisie = user?.role === 'AGENT_SAISIE';

  const [chantiers, setChantiers] = useState([]);
  const [ouvriers, setOuvriers] = useState([]);
  const [taches, setTaches] = useState([]);

  const [siteId, setSiteId] = useState('');
  const [date, setDate] = useState(todayStr());
  const [notes, setNotes] = useState('');
  const [rows, setRows] = useState([newRow()]);

  const [formError, setFormError] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [dossierId, setDossierId] = useState(null);
  const [chantiersLoading, setChantiersLoading] = useState(true);

  useEffect(() => {
    setChantiersLoading(true);
    if (isAgentSaisie) {
      getMySites()
        .then((data) => {
          const sites = data.content || data || [];
          setChantiers(sites);
          if (sites.length > 0) {
            setSiteId(String(sites[0].id));
          }
        })
        .catch(() => {})
        .finally(() => setChantiersLoading(false));
    } else {
      getChantiers()
        .then((d) => setChantiers(d.content || d))
        .catch(() => {})
        .finally(() => setChantiersLoading(false));
    }
  }, [isAgentSaisie]);

  useEffect(() => {
    getOuvriers().then((d) => setOuvriers(d.content || d)).catch(() => {});
  }, []);

  useEffect(() => {
    if (!siteId) {
      setTaches([]);
      return;
    }
    getTachesBySite(siteId).then(setTaches).catch(() => setTaches([]));
  }, [siteId]);

  const handleSiteChange = (e) => {
    setSiteId(e.target.value);
    setRows([newRow()]);
  };

  const updateRow = (rowId, field, value) => {
    setRows((prev) =>
      prev.map((r) => {
        if (r.rowId !== rowId) return r;
        const updated = { ...r, [field]: value, status: null, errorMsg: null };
        if (field === 'halfDay' || field === 'halfDaySlot') {
          const halfDayOn = field === 'halfDay' ? value : r.halfDay;
          const slot = field === 'halfDaySlot' ? value : r.halfDaySlot;
          if (halfDayOn && date) {
            const times = buildHalfDayTimes(date, slot);
            updated.startTime = times.startTime;
            updated.endTime = times.endTime;
          } else if (field === 'halfDay' && !value) {
            updated.startTime = '';
            updated.endTime = '';
          }
        }
        return updated;
      })
    );
  };

  const addRow = () => setRows((prev) => [...prev, newRow()]);
  const removeRow = (rowId) => setRows((prev) => prev.filter((r) => r.rowId !== rowId));

  const rowPayload = (r) => ({
    ouvrierId: Number(r.ouvrierId),
    tacheId: Number(r.tacheId),
    halfDay: r.halfDay,
    startTime: r.startTime,
    endTime: r.endTime,
    notes: r.notes || null,
  });

  const rowIsFilled = (r) => r.ouvrierId && r.tacheId && r.startTime && r.endTime;
  const rowIsTouched = (r) => r.ouvrierId || r.tacheId;
  const rowTimeInvalid = (r) => r.startTime && r.endTime && !isTimeRangeValid(r.startTime, r.endTime);

  const submitLines = async (targetDossierId, targetRows) => {
    for (const r of targetRows) {
      try {
        await addLignePointage(targetDossierId, rowPayload(r));
        setRows((prev) =>
          prev.map((row) => (row.rowId === r.rowId ? { ...row, status: 'success', errorMsg: null } : row))
        );
      } catch (err) {
        setRows((prev) =>
          prev.map((row) =>
            row.rowId === r.rowId
              ? { ...row, status: 'error', errorMsg: err.response?.data?.message || "Erreur lors de l'ajout" }
              : row
          )
        );
      }
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError(null);

    if (!siteId || !date) {
      setFormError('Veuillez renseigner le chantier et la date.');
      return;
    }

    const touchedIncomplete = rows.filter((r) => rowIsTouched(r) && !rowIsFilled(r));
    if (touchedIncomplete.length > 0) {
      setFormError(
        'Certaines lignes ouvrier sont incomplètes (ouvrier, tâche, et horaires ou demi-journée requis).'
      );
      return;
    }

    const invalidTimeRows = rows.filter((r) => rowIsFilled(r) && rowTimeInvalid(r));
    if (invalidTimeRows.length > 0) {
      setFormError("L'heure de début doit être antérieure à l'heure de fin pour chaque ouvrier.");
      return;
    }

    const validRows = rows.filter(rowIsFilled);

    setSubmitting(true);
    try {
      let id = dossierId;
      if (!id) {
        const created = await createDossierPointage({
          siteId: Number(siteId),
          date,
          notes: notes || null,
        });
        id = created.id;
        setDossierId(id);
      }

      const rowsToSubmit = validRows.filter((r) => r.status !== 'success');
      await submitLines(id, rowsToSubmit);
    } catch (err) {
      setFormError(err.response?.data?.message || 'Une erreur est survenue lors de la création du dossier.');
    } finally {
      setSubmitting(false);
    }
  };

  const filledRows = rows.filter(rowIsFilled);
  const hasErrors = rows.some((r) => r.status === 'error');
  const allDone = dossierId && filledRows.length > 0 && filledRows.every((r) => r.status === 'success');

  const siteSelectDisabled = !!dossierId || isAgentSaisie;

  return (
    <div className="pointage-create-page">
      <div className="page-header">
        <h1>+ Nouveau pointage</h1>
      </div>

      <form className="chantier-form" onSubmit={handleSubmit} style={{ maxWidth: 950 }}>
        {formError && <div className="error-banner">{formError}</div>}

        {dossierId && !hasErrors && !allDone && (
          <div className="error-banner" style={{ background: '#e0f2fe', color: '#0369a1', borderColor: '#bae6fd' }}>
            Dossier créé — ajoutez des ouvriers ci-dessous puis validez, ou accédez directement au dossier.
          </div>
        )}
        {allDone && (
          <div className="error-banner" style={{ background: '#dcfce7', color: '#15803d', borderColor: '#bbf7d0' }}>
            Pointage créé avec {filledRows.length} ouvrier(s) ajouté(s) avec succès.
          </div>
        )}
        {hasErrors && (
          <div className="error-banner">
            Certains ouvriers n'ont pas pu être ajoutés (marqués ✕ dans le tableau). Corrigez la ligne puis
            cliquez à nouveau sur le bouton pour réessayer uniquement ces lignes.
          </div>
        )}

        <div className="form-row">
          <div className="form-group">
            <label>Chantier *</label>
            <select value={siteId} onChange={handleSiteChange} disabled={siteSelectDisabled} required>
              {chantiersLoading ? (
                <option value="">Chargement...</option>
              ) : chantiers.length === 0 ? (
                <option value="">Aucun chantier assigné</option>
              ) : (
                <>
                  {!isAgentSaisie && <option value="">Sélectionner un chantier</option>}
                  {chantiers.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.name} {c.reference ? `— ${c.reference}` : ''}
                    </option>
                  ))}
                </>
              )}
            </select>
            {isAgentSaisie && chantiers.length > 0 && (
              <small style={{ color: 'var(--tcgm-gray)' }}>Chantier assigné automatiquement</small>
            )}
          </div>
          <div className="form-group">
            <label>Date *</label>
            <input type="date" value={date} onChange={(e) => setDate(e.target.value)} disabled={!!dossierId} required />
          </div>
        </div>

        <div className="form-group">
          <label>Notes</label>
          <textarea
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            placeholder="Remarques éventuelles..."
            disabled={!!dossierId}
          />
        </div>

        <div className="page-header" style={{ marginBottom: 8, marginTop: 8 }}>
          <h1 style={{ fontSize: 16 }}>Ouvriers pointés</h1>
          <button type="button" className="btn-ghost" onClick={addRow} disabled={!siteId}>
            + Ajouter un ouvrier
          </button>
        </div>

        <div className="table-container">
          <table className="ouvriers-table">
            <thead>
              <tr>
                <th>Ouvrier</th>
                <th>Tâche</th>
                <th>Demi-j.</th>
                <th>Créneau</th>
                <th>Début</th>
                <th>Fin</th>
                <th>Notes</th>
                <th className="col-actions">—</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((r) => {
                const timeInvalid = rowTimeInvalid(r);
                return (
                  <tr key={r.rowId}>
                    <td>
                      <select
                        value={r.ouvrierId}
                        onChange={(e) => updateRow(r.rowId, 'ouvrierId', e.target.value)}
                        disabled={r.status === 'success'}
                      >
                        <option value="">—</option>
                        {ouvriers.map((o) => (
                          <option key={o.id} value={o.id}>
                            {o.firstName} {o.lastName}
                          </option>
                        ))}
                      </select>
                    </td>
                    <td>
                      <select
                        value={r.tacheId}
                        onChange={(e) => updateRow(r.rowId, 'tacheId', e.target.value)}
                        disabled={r.status === 'success' || taches.length === 0}
                      >
                        <option value="">{taches.length === 0 ? 'Aucune tâche' : '—'}</option>
                        {taches.map((t) => (
                          <option key={t.id} value={t.id}>
                            {t.title}
                          </option>
                        ))}
                      </select>
                    </td>
                    <td style={{ textAlign: 'center' }}>
                      <input
                        type="checkbox"
                        checked={r.halfDay}
                        onChange={(e) => updateRow(r.rowId, 'halfDay', e.target.checked)}
                        disabled={r.status === 'success'}
                      />
                    </td>
                    <td>
                      <select
                        value={r.halfDaySlot}
                        onChange={(e) => updateRow(r.rowId, 'halfDaySlot', e.target.value)}
                        disabled={!r.halfDay || r.status === 'success'}
                      >
                        <option value="MATIN">Matin</option>
                        <option value="APRES_MIDI">Après-midi</option>
                      </select>
                    </td>
                    <td>
                      <input
                        type="time"
                        value={r.startTime ? r.startTime.slice(11, 16) : ''}
                        onChange={(e) => updateRow(r.rowId, 'startTime', date ? `${date}T${e.target.value}` : '')}
                        disabled={r.halfDay || r.status === 'success'}
                        style={timeInvalid ? { borderColor: '#dc2626' } : undefined}
                      />
                    </td>
                    <td>
                      <input
                        type="time"
                        value={r.endTime ? r.endTime.slice(11, 16) : ''}
                        onChange={(e) => updateRow(r.rowId, 'endTime', date ? `${date}T${e.target.value}` : '')}
                        disabled={r.halfDay || r.status === 'success'}
                        style={timeInvalid ? { borderColor: '#dc2626' } : undefined}
                      />
                      {timeInvalid && (
                        <div style={{ color: '#dc2626', fontSize: 12 }}>Fin doit être après le début</div>
                      )}
                    </td>
                    <td>
                      <input
                        type="text"
                        value={r.notes}
                        onChange={(e) => updateRow(r.rowId, 'notes', e.target.value)}
                        disabled={r.status === 'success'}
                        style={{
                          width: '100%',
                          padding: '6px 8px',
                          border: '1px solid var(--tcgm-gray-light)',
                          borderRadius: 6,
                        }}
                      />
                    </td>
                    <td className="col-actions">
                      {r.status === 'success' && <span className="badge badge--success">✓</span>}
                      {r.status === 'error' && (
                        <span className="badge badge--danger" title={r.errorMsg}>
                          ✕
                        </span>
                      )}
                      {r.status !== 'success' && rows.length > 1 && (
                        <button type="button" className="icon-btn icon-btn--danger" onClick={() => removeRow(r.rowId)}>
                          🗑
                        </button>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>

        <div className="form-actions">
          <button type="submit" className="btn-primary" disabled={submitting}>
            {submitting
              ? 'Enregistrement...'
              : dossierId
              ? 'Ajouter / réessayer les ouvriers'
              : 'Créer le pointage'}
          </button>
          {dossierId ? (
            <Link to={`/pointage/${dossierId}`} className="btn-ghost">
              Voir le dossier
            </Link>
          ) : (
            <button type="button" className="btn-ghost" onClick={() => navigate('/pointage')}>
              Annuler
            </button>
          )}
        </div>
      </form>
    </div>
  );
};

export default PointageCreatePage;