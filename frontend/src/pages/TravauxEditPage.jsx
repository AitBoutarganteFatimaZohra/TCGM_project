import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import useTravaux from '../hooks/useTravaux';
import useChantiers from '../hooks/useChantiers';

const toLocalDateTime = (dateStr) => (dateStr ? `${dateStr}T00:00:00` : null);
const toDateInput = (isoString) => (isoString ? isoString.slice(0, 10) : '');
const toNullableLong = (value) => (value ? Number(value) : null);
const toNullableDecimal = (value) => (value ? Number(value) : null);

const TravauxEditPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { fetchTravauxById, editTravaux, loading, error } = useTravaux();
  const { chantiers } = useChantiers();

  const [form, setForm] = useState(null);
  const [notFound, setNotFound] = useState(false);

  useEffect(() => {
    const load = async () => {
      try {
        const t = await fetchTravauxById(id);
        setForm({
          code: t.code || '',
          intitule: t.intitule || '',
          description: t.description || '',
          dateDebutPrevue: toDateInput(t.dateDebutPrevue),
          dateFinPrevue: toDateInput(t.dateFinPrevue),
          priorite: t.priorite ?? '',
          statut: t.statut || 'PLANIFIE',
          budgetEstime: t.budgetEstime ?? '',
          chantierId: t.chantier?.id || '',
        });
      } catch (err) {
        setNotFound(true);
      }
    };
    load();
  }, [id]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const payload = {
      code: form.code,
      intitule: form.intitule,
      description: form.description || null,
      dateDebutPrevue: toLocalDateTime(form.dateDebutPrevue),
      dateFinPrevue: toLocalDateTime(form.dateFinPrevue),
      priorite: toNullableLong(form.priorite),
      statut: form.statut,
      budgetEstime: toNullableDecimal(form.budgetEstime),
      chantierId: toNullableLong(form.chantierId),
    };

    try {
      await editTravaux(id, payload);
      navigate(`/travaux/${id}`);
    } catch (err) {
      // erreur déjà exposée via `error`
    }
  };

  if (notFound) {
    return (
      <div className="empty-state">
        <p>Travaux introuvables</p>
      </div>
    );
  }

  if (!form) {
    return <div className="loading">Chargement des travaux...</div>;
  }

  return (
    <div className="travaux-page">
      <div className="page-header">
        <h1>✎ Modifier les travaux</h1>
      </div>

      {error && <div className="error-banner">❌ {error}</div>}

      <form onSubmit={handleSubmit} className="chantier-form">
        <div className="form-row">
          <div className="form-group">
            <label>Code *</label>
            <input type="text" name="code" value={form.code} onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label>Intitulé *</label>
            <input type="text" name="intitule" value={form.intitule} onChange={handleChange} required />
          </div>
        </div>

        <div className="form-group">
          <label>Description</label>
          <textarea name="description" value={form.description} onChange={handleChange} rows={4} />
        </div>

        <div className="form-group">
          <label>Chantier *</label>
          <select name="chantierId" value={form.chantierId} onChange={handleChange} required>
            <option value="">— Sélectionner —</option>
            {chantiers.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name} {c.reference ? `(${c.reference})` : ''}
              </option>
            ))}
          </select>
        </div>

        <div className="form-row">
          <div className="form-group">
            <label>Date de début prévue</label>
            <input type="date" name="dateDebutPrevue" value={form.dateDebutPrevue} onChange={handleChange} />
          </div>
          <div className="form-group">
            <label>Date de fin prévue</label>
            <input type="date" name="dateFinPrevue" value={form.dateFinPrevue} onChange={handleChange} />
          </div>
        </div>

        <div className="form-row">
          <div className="form-group">
            <label>Priorité</label>
            <input type="number" name="priorite" min="1" max="5" value={form.priorite} onChange={handleChange} />
          </div>
          <div className="form-group">
            <label>Budget estimé (DH)</label>
            <input type="number" name="budgetEstime" step="0.01" value={form.budgetEstime} onChange={handleChange} />
          </div>
        </div>

        <div className="form-group">
          <label>Statut</label>
          <select name="statut" value={form.statut} onChange={handleChange}>
            <option value="PLANIFIE">Planifié</option>
            <option value="EN_COURS">En cours</option>
            <option value="TERMINE">Terminé</option>
            <option value="SUSPENDU">Suspendu</option>
          </select>
        </div>

        <div className="form-actions">
          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? 'Enregistrement...' : 'Enregistrer'}
          </button>
          <button type="button" className="btn-view" onClick={() => navigate(`/travaux/${id}`)}>
            Annuler
          </button>
        </div>
      </form>
    </div>
  );
};

export default TravauxEditPage;