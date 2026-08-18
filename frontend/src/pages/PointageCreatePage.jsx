import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { getChantiers } from '../api/chantierApi';
import { getOuvriers } from '../api/ouvrierApi';
import { getTaches } from '../api/tacheApi';
import { createDossierPointage, addLignePointage } from '../api/pointageApi';

const todayStr = () => new Date().toISOString().slice(0, 10);

let rowIdCounter = 0;
const newRow = () => ({
  rowId: `row-${rowIdCounter++}`,
  ouvrierId: '',
  tacheId: '',
  halfDay: false,
  startTime: '',
  endTime: '',
  notes: '',
  status: null, // null | 'success' | 'error'
  errorMsg: null,
});

/**
 * Formulaire de création d'un pointage en une seule page :
 * chantier + date + notes, PLUS un tableau dynamique d'ouvriers.
 *
 * Le backend n'ayant pas d'endpoint "bulk", la soumission :
 *  1) crée le dossier (POST /pointage/dossiers)
 *  2) ajoute chaque ligne remplie séquentiellement (POST /pointage/dossiers/{id}/lignes)
 * Le dossier reste "verrouillé" (chantier/date non modifiables) une fois créé,
 * et on peut relancer uniquement les lignes en échec sans dupliquer les lignes réussies.
 */
const PointageCreatePage = () => {
  const navigate = useNavigate();

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

  useEffect(() => {
    getChantiers().then((d) => setChantiers(d.content || d)).catch(() => {});
    getOuvriers().then((d) => setOuvriers(d.content || d)).catch(() => {});
    getTaches().then((d) => setTaches(d.content || d)).catch(() => {});
  }, []);

  const updateRow = (rowId, field, value) => {
    setRows((prev) =>
      prev.map((r) => (r.rowId === rowId ? { ...r, [field]: value, status: null, errorMsg: null } : r))
    );
  };

  const addRow = () => setRows((prev) => [...prev, newRow()]);
  const removeRow = (rowId) => setRows((prev) => prev.filter((r) => r.rowId !== rowId));

  const rowPayload = (r) => ({
    ouvrierId: Number(r.ouvrierId),
    tacheId: Number(r.tacheId),
    halfDay: r.halfDay,
    startTime: r.halfDay ? null : r.startTime,
    endTime: r.halfDay ? null : r.endTime,
    notes: r.notes || null,
  });

  const rowIsFilled = (r) => r.ouvrierId && r.tacheId && (r.halfDay || (r.startTime && r.endTime));
  const rowIsTouched = (r) => r.ouvrierId || r.tacheId;

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

  return (
    <div className="pointage-create-page">
      <div className="page-header">
        <h1>+ Nouveau pointage</h1>
      </div>

      <form className="chantier-form" onSubmit={handleSubmit} style={{ maxWidth: 900 }}>
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
            <select value={siteId} onChange={(e) => setSiteId(e.target.value)} disabled={!!dossierId} required>
              <option value="">Sélectionner un chantier</option>
              {chantiers.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name} {c.reference ? `— ${c.reference}` : ''}
                </option>
              ))}
            </select>
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
          <button type="button" className="btn-ghost" onClick={addRow}>
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
                <th>Début</th>
                <th>Fin</th>
                <th>Notes</th>
                <th className="col-actions">—</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((r) => (
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
                      disabled={r.status === 'success'}
                    >
                      <option value="">—</option>
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
                    <input
                      type="datetime-local"
                      value={r.startTime}
                      onChange={(e) => updateRow(r.rowId, 'startTime', e.target.value)}
                      disabled={r.halfDay || r.status === 'success'}
                    />
                  </td>
                  <td>
                    <input
                      type="datetime-local"
                      value={r.endTime}
                      onChange={(e) => updateRow(r.rowId, 'endTime', e.target.value)}
                      disabled={r.halfDay || r.status === 'success'}
                    />
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
              ))}
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