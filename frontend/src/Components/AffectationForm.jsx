import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getChantiers } from '../api/chantierApi';
import { getOuvriersDisponibles } from '../api/ouvrierApi';

const STATUTS = ['PLANIFIEE', 'EN_COURS', 'TERMINEE', 'ANNULEE'];

const STATUT_LABELS = {
  PLANIFIEE: 'Planifiée',
  EN_COURS: 'En cours',
  TERMINEE: 'Terminée',
  ANNULEE: 'Annulée',
};

/**
 * Formulaire partagé Création / Édition d'une affectation.
 * - initialData : affectation existante (mode édition)
 * - onSubmit(data) : fonction async appelée avec le payload prêt pour l'API
 * - isEdit : affiche le champ statut si true
 */
const AffectationForm = ({ initialData, onSubmit, submitLabel = 'Enregistrer', isEdit = false }) => {
  const navigate = useNavigate();
  const [chantiers, setChantiers] = useState([]);
  const [ouvriers, setOuvriers] = useState([]);
  const [formData, setFormData] = useState({
    ouvrierId: initialData?.ouvrier?.id || '',
    chantierId: initialData?.chantier?.id || '',
    dateDebut: initialData?.dateDebut || '',
    dateFin: initialData?.dateFin || '',
    statut: initialData?.statut || 'EN_COURS',
  });
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState(null);

  useEffect(() => {
    getChantiers({ size: 100 }).then((data) => setChantiers(data.content || data)).catch(() => {});

    // ✅ CORRIGÉ : on utilise /ouvriers/disponibles (ouvriers sans
    // affectation EN_COURS) au lieu de /ouvriers, qui pour un Chef de
    // Chantier ne renvoyait que son équipe déjà en poste — un ouvrier
    // fraîchement libéré (affectation TERMINEE/ANNULEE) n'apparaissait
    // donc jamais dans ce select.
    getOuvriersDisponibles({ size: 100 })
      .then((data) => {
        let list = data.content || data;
        // En mode édition, l'ouvrier actuellement assigné a justement une
        // affectation EN_COURS (celle qu'on modifie) donc il n'apparaît
        // pas dans "disponibles" — on le réinjecte manuellement pour que
        // le select l'affiche correctement.
        if (initialData?.ouvrier && !list.some((o) => o.id === initialData.ouvrier.id)) {
          list = [initialData.ouvrier, ...list];
        }
        setOuvriers(list);
      })
      .catch(() => {});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [initialData?.ouvrier?.id]);

  // Si initialData arrive après le premier rendu (fetch async côté page parente)
  useEffect(() => {
    if (initialData) {
      setFormData({
        ouvrierId: initialData.ouvrier?.id || '',
        chantierId: initialData.chantier?.id || '',
        dateDebut: initialData.dateDebut || '',
        dateFin: initialData.dateFin || '',
        statut: initialData.statut || 'EN_COURS',
      });
    }
  }, [initialData]);

  const handleChange = (field) => (e) => {
    setFormData((prev) => ({ ...prev, [field]: e.target.value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError(null);

    if (!formData.ouvrierId || !formData.chantierId || !formData.dateDebut) {
      setFormError("Veuillez renseigner l'ouvrier, le chantier et la date de début.");
      return;
    }

    setSubmitting(true);
    try {
      await onSubmit({
        ouvrierId: Number(formData.ouvrierId),
        chantierId: Number(formData.chantierId),
        dateDebut: formData.dateDebut,
        dateFin: formData.dateFin || null,
        statut: formData.statut,
      });
      navigate('/affectations');
    } catch (err) {
      setFormError(err.response?.data?.message || 'Une erreur est survenue.');
    } finally {
      setSubmitting(false);
    }
  };

  const selectedOuvrier = ouvriers.find((o) => String(o.id) === String(formData.ouvrierId));
  const selectedChantier = chantiers.find((c) => String(c.id) === String(formData.chantierId));

  return (
    <form className="form-page__body" onSubmit={handleSubmit}>
      <div className="form-page__main">
        {formError && <div className="error-banner">{formError}</div>}

        {/* ---- Affectation ---- */}
        <section className="form-section">
          <div className="form-section__header">
            <div className="form-section__icon">👷</div>
            <div>
              <h3>Affectation</h3>
              <p>Ouvrier et chantier concernés</p>
            </div>
          </div>
          <div className="form-section__body">
            <div className="form-group">
              <label>Ouvrier *</label>
              <select value={formData.ouvrierId} onChange={handleChange('ouvrierId')} required>
                <option value="">Sélectionner un ouvrier</option>
                {ouvriers.map((o) => (
                  <option key={o.id} value={o.id}>
                    {o.firstName} {o.lastName} {o.cin ? `— ${o.cin}` : ''}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label>Chantier *</label>
              <select value={formData.chantierId} onChange={handleChange('chantierId')} required>
                <option value="">Sélectionner un chantier</option>
                {chantiers.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name} {c.reference ? `— ${c.reference}` : ''}
                  </option>
                ))}
              </select>
            </div>
          </div>
        </section>

        {/* ---- Période ---- */}
        <section className="form-section">
          <div className="form-section__header">
            <div className="form-section__icon">📅</div>
            <div>
              <h3>Période</h3>
              <p>Dates de début et de fin de l'affectation</p>
            </div>
          </div>
          <div className="form-section__body">
            <div className="form-row">
              <div className="form-group">
                <label>Date de début *</label>
                <input type="date" value={formData.dateDebut} onChange={handleChange('dateDebut')} required />
              </div>
              <div className="form-group">
                <label>Date de fin</label>
                <input type="date" value={formData.dateFin} onChange={handleChange('dateFin')} />
              </div>
            </div>
          </div>
        </section>

        {/* ---- Statut (édition uniquement) ---- */}
        {isEdit && (
          <section className="form-section">
            <div className="form-section__header">
              <div className="form-section__icon">🔖</div>
              <div>
                <h3>Statut</h3>
                <p>Avancement de l'affectation</p>
              </div>
            </div>
            <div className="form-section__body">
              <div className="form-group">
                <label>Statut</label>
                <select value={formData.statut} onChange={handleChange('statut')}>
                  {STATUTS.map((s) => (
                    <option key={s} value={s}>
                      {STATUT_LABELS[s]}
                    </option>
                  ))}
                </select>
              </div>
            </div>
          </section>
        )}

        <div className="form-page__actions">
          <button type="button" className="btn-ghost" onClick={() => navigate('/affectations')}>
            Annuler
          </button>
          <button type="submit" className="btn-primary" disabled={submitting}>
            {submitting ? 'Enregistrement...' : submitLabel}
          </button>
        </div>
      </div>

      {/* ---- Aperçu latéral ---- */}
      <aside className="form-page__sidebar">
        <div className="summary-card">
          <h3 className="summary-card__title">Aperçu</h3>

          <div className="summary-card__row">
            <span className="summary-card__label">Ouvrier</span>
            <span className="summary-card__value">
              {selectedOuvrier ? `${selectedOuvrier.firstName} ${selectedOuvrier.lastName}` : '—'}
            </span>
          </div>
          <div className="summary-card__row">
            <span className="summary-card__label">Chantier</span>
            <span className="summary-card__value">{selectedChantier?.name || '—'}</span>
          </div>
          <div className="summary-card__row">
            <span className="summary-card__label">Date de début</span>
            <span className="summary-card__value">{formData.dateDebut || '—'}</span>
          </div>
          <div className="summary-card__row">
            <span className="summary-card__label">Date de fin</span>
            <span className="summary-card__value">{formData.dateFin || '—'}</span>
          </div>
          {isEdit && (
            <div className="summary-card__row">
              <span className="summary-card__label">Statut</span>
              <span className="badge badge--success">{STATUT_LABELS[formData.statut]}</span>
            </div>
          )}
        </div>
      </aside>
    </form>
  );
};

export default AffectationForm;