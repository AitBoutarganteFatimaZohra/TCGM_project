import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import useTravaux from '../hooks/useTravaux';
import useChantiers from '../hooks/useChantiers';

const initialForm = {
  code: '',
  intitule: '',
  description: '',
  dateDebutPrevue: '',
  dateFinPrevue: '',
  priorite: '',
  statut: 'PLANIFIE',
  budgetEstime: '',
  chantierId: '',
};

const toLocalDateTime = (dateStr) => (dateStr ? `${dateStr}T00:00:00` : null);
const toNullableLong = (value) => (value ? Number(value) : null);
const toNullableDecimal = (value) => (value ? Number(value) : null);

const STATUT_LABELS = {
  PLANIFIE: 'Planifié',
  EN_COURS: 'En cours',
  TERMINE: 'Terminé',
  SUSPENDU: 'Suspendu',
};

const TravauxCreatePage = () => {
  const { addTravaux, loading, error } = useTravaux();
  const { chantiers } = useChantiers();
  const navigate = useNavigate();
  const [form, setForm] = useState(initialForm);

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
      const created = await addTravaux(payload);
      navigate(`/travaux/${created.id}`);
    } catch (err) {
      // erreur déjà exposée via `error`
    }
  };

  const selectedChantier = chantiers.find((c) => String(c.id) === String(form.chantierId));

  return (
    <div className="form-page">
      <div className="form-page__header">
        <button type="button" className="form-page__back" onClick={() => navigate('/travaux')} aria-label="Retour">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <polyline points="15 18 9 12 15 6" />
          </svg>
        </button>
        <div>
          <h1>Nouveaux travaux</h1>
          <p className="form-page__subtitle">Renseignez les informations pour créer une nouvelle fiche de travaux</p>
        </div>
      </div>

      {error && <div className="error-banner">{error}</div>}

      <form onSubmit={handleSubmit} className="form-page__body">
        <div className="form-page__main">

          {/* ---- Informations générales ---- */}
          <section className="form-section">
            <div className="form-section__header">
              <div className="form-section__icon">📋</div>
              <div>
                <h3>Informations générales</h3>
                <p>Identification et description des travaux</p>
              </div>
            </div>
            <div className="form-section__body">
              <div className="form-row">
                <div className="form-group">
                  <label>Code *</label>
                  <input type="text" name="code" value={form.code} onChange={handleChange} placeholder="TRX-2026-001" required />
                </div>
                <div className="form-group">
                  <label>Intitulé *</label>
                  <input type="text" name="intitule" value={form.intitule} onChange={handleChange} placeholder="Ex. Gros œuvre bâtiment A" required />
                </div>
              </div>

              <div className="form-group">
                <label>Description</label>
                <textarea
                  name="description"
                  value={form.description}
                  onChange={handleChange}
                  rows={4}
                  placeholder="Détails des travaux..."
                />
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
            </div>
          </section>

          {/* ---- Planification ---- */}
          <section className="form-section">
            <div className="form-section__header">
              <div className="form-section__icon">📅</div>
              <div>
                <h3>Planification</h3>
                <p>Dates prévisionnelles de réalisation</p>
              </div>
            </div>
            <div className="form-section__body">
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
            </div>
          </section>

          {/* ---- Priorité, budget & statut ---- */}
          <section className="form-section">
            <div className="form-section__header">
              <div className="form-section__icon">💰</div>
              <div>
                <h3>Priorité & budget</h3>
                <p>Niveau d'urgence et enveloppe estimée</p>
              </div>
            </div>
            <div className="form-section__body">
              <div className="form-row">
                <div className="form-group">
                  <label>Priorité (1 à 5)</label>
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
            </div>
          </section>

          <div className="form-page__actions">
            <button type="button" className="btn-ghost" onClick={() => navigate('/travaux')}>
              Annuler
            </button>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Création...' : 'Créer les travaux'}
            </button>
          </div>
        </div>

        {/* ---- Aperçu latéral ---- */}
        <aside className="form-page__sidebar">
          <div className="summary-card">
            <h3 className="summary-card__title">Aperçu</h3>

            <div className="summary-card__row">
              <span className="summary-card__label">Code</span>
              <span className="summary-card__value">{form.code || '—'}</span>
            </div>
            <div className="summary-card__row">
              <span className="summary-card__label">Intitulé</span>
              <span className="summary-card__value">{form.intitule || '—'}</span>
            </div>
            <div className="summary-card__row">
              <span className="summary-card__label">Chantier</span>
              <span className="summary-card__value">{selectedChantier?.name || '—'}</span>
            </div>
            <div className="summary-card__row">
              <span className="summary-card__label">Début prévu</span>
              <span className="summary-card__value">{form.dateDebutPrevue || '—'}</span>
            </div>
            <div className="summary-card__row">
              <span className="summary-card__label">Fin prévue</span>
              <span className="summary-card__value">{form.dateFinPrevue || '—'}</span>
            </div>
            <div className="summary-card__row">
              <span className="summary-card__label">Budget</span>
              <span className="summary-card__value">
                {form.budgetEstime ? `${form.budgetEstime} DH` : '—'}
              </span>
            </div>
            <div className="summary-card__row">
              <span className="summary-card__label">Statut</span>
              <span className="badge badge--success">{STATUT_LABELS[form.statut]}</span>
            </div>
          </div>
        </aside>
      </form>
    </div>
  );
};

export default TravauxCreatePage;