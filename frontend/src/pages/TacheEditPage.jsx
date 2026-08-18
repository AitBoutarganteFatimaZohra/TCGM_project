import { useState, useEffect } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import useTaches from '../hooks/useTaches';
import useTravaux from '../hooks/useTravaux';

const TacheEditPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { fetchTacheById, editTache, loading, error } = useTaches();
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
  const [initialLoading, setInitialLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);

  useEffect(() => {
    const load = async () => {
      try {
        const tache = await fetchTacheById(id);
        setFormData({
          title: tache.title || '',
          description: tache.description || '',
          plannedDate: tache.plannedDate ? tache.plannedDate.slice(0, 16) : '',
          status: tache.status || 'PLANIFIEE',
          priority: tache.priority ?? 1,
          travauxId: tache.site?.id || '',
        });
      } catch (err) {
        setNotFound(true);
      } finally {
        setInitialLoading(false);
      }
    };
    load();
  }, [id]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    setValidationErrors((prev) => ({ ...prev, [name]: null }));
  };

  const validate = () => {
    const errors = {};
    if (!formData.title.trim()) errors.title = 'Le titre de la tâche est obligatoire';
    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    try {
      const payload = {
        title: formData.title,
        description: formData.description,
        plannedDate: formData.plannedDate || null,
        status: formData.status,
        priority: Number(formData.priority),
        travauxId: formData.travauxId ? Number(formData.travauxId) : undefined,
      };
      await editTache(id, payload);
      navigate(`/taches/${id}`);
    } catch (err) {
      // erreur déjà gérée par le hook
    }
  };

  if (initialLoading) {
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

  return (
    <div className="tache-edit-page">
      <div className="page-header">
        <h1>✎ Modifier la tâche</h1>
        <Link to={`/taches/${id}`} className="btn-secondary">
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
          />
        </div>

        <div className="form-row">
          <div className="form-group">
            <label className="form-label">Travaux / Chantier</label>
            <select
              name="travauxId"
              className="form-select"
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
          <Link to={`/taches/${id}`} className="btn-secondary">
            Annuler
          </Link>
          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? 'Enregistrement...' : 'Enregistrer'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default TacheEditPage;