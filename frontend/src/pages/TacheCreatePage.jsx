import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import useTaches from '../hooks/useTaches';
import useTravaux from '../hooks/useTravaux';

const TacheCreatePage = () => {
  const navigate = useNavigate();
  const { addTache, loading, error } = useTaches();
  const { travaux, loading: travauxLoading } = useTravaux();

  const [formData, setFormData] = useState({
    title: '',
    description: '',
    plannedDate: '',
    status: 'PLANIFIEE',
    priority: 1,
    travauxId: '',
  });
  const [validationErrors, setValidationErrors] = useState({});

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    setValidationErrors((prev) => ({ ...prev, [name]: null }));
  };

  const validate = () => {
    const errors = {};
    if (!formData.title.trim()) errors.title = 'Le titre de la tâche est obligatoire';
    if (!formData.travauxId) errors.travauxId = 'Les travaux sont obligatoires';
    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    try {
      const payload = {
        ...formData,
        priority: Number(formData.priority),
        travauxId: Number(formData.travauxId),
        plannedDate: formData.plannedDate || null,
      };
      const created = await addTache(payload);
      navigate(`/taches/${created.id}`);
    } catch (err) {
      // erreur déjà gérée par le hook (state error)
    }
  };

  return (
    <div className="tache-create-page">
      <div className="page-header">
        <h1>✅ Nouvelle tâche</h1>
        <Link to="/taches" className="btn-secondary">
          ← Retour
        </Link>
      </div>

      {error && <div className="error-banner">❌ {error}</div>}

      <form className="form-card" onSubmit={handleSubmit}>
        <div className="form-group">
          <label className="form-label">Titre *</label>
          <input
            type="text"
            name="title"
            className={`form-input ${validationErrors.title ? 'form-input--error' : ''}`}
            value={formData.title}
            onChange={handleChange}
            placeholder="Ex : Coulage de la dalle"
          />
          {validationErrors.title && <span className="form-error">{validationErrors.title}</span>}
        </div>

        <div className="form-group">
          <label className="form-label">Description</label>
          <textarea
            name="description"
            className="form-textarea"
            rows={4}
            value={formData.description}
            onChange={handleChange}
            placeholder="Détails de la tâche..."
          />
        </div>

        <div className="form-row">
          <div className="form-group">
            <label className="form-label">Travaux / Chantier *</label>
            <select
              name="travauxId"
              className={`form-select ${validationErrors.travauxId ? 'form-input--error' : ''}`}
              value={formData.travauxId}
              onChange={handleChange}
              disabled={travauxLoading}
            >
              <option value="">— Sélectionner —</option>
              {travaux.map((t) => (
                <option key={t.id} value={t.id}>
                  {t.title || t.reference || `Travaux #${t.id}`}
                  {t.chantier?.name ? ` — ${t.chantier.name}` : ''}
                </option>
              ))}
            </select>
            {validationErrors.travauxId && (
              <span className="form-error">{validationErrors.travauxId}</span>
            )}
          </div>

          <div className="form-group">
            <label className="form-label">Priorité (1-5)</label>
            <select
              name="priority"
              className="form-select"
              value={formData.priority}
              onChange={handleChange}
            >
              {[1, 2, 3, 4, 5].map((p) => (
                <option key={p} value={p}>
                  P{p}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div className="form-row">
          <div className="form-group">
            <label className="form-label">Date prévue</label>
            <input
              type="datetime-local"
              name="plannedDate"
              className="form-input"
              value={formData.plannedDate}
              onChange={handleChange}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Statut</label>
            <select
              name="status"
              className="form-select"
              value={formData.status}
              onChange={handleChange}
            >
              <option value="PLANIFIEE">Planifiée</option>
              <option value="EN_COURS">En cours</option>
              <option value="TERMINEE">Terminée</option>
            </select>
          </div>
        </div>

        <div className="form-actions">
          <Link to="/taches" className="btn-secondary">
            Annuler
          </Link>
          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? 'Création...' : 'Créer la tâche'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default TacheCreatePage;