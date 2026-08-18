import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getDossierPointageById, updateDossierPointage } from '../api/pointageApi';

const PointageEditPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({ siteId: '', date: '', notes: '' });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [formError, setFormError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    getDossierPointageById(id)
      .then((d) => {
        setFormData({
          siteId: d.site?.id || '',
          date: d.date || '',
          notes: d.notes || '',
        });
      })
      .catch(() => setError('Impossible de charger ce dossier de pointage.'))
      .finally(() => setLoading(false));
  }, [id]);

  const handleChange = (field) => (e) => {
    setFormData((prev) => ({ ...prev, [field]: e.target.value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError(null);
    setSubmitting(true);
    try {
      await updateDossierPointage(id, {
        siteId: Number(formData.siteId),
        date: formData.date,
        notes: formData.notes || null,
      });
      navigate(`/pointage/${id}`);
    } catch (err) {
      setFormError(err.response?.data?.message || 'Une erreur est survenue.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <div className="loading">Chargement...</div>;
  if (error) return <div className="error-banner">{error}</div>;

  return (
    <div className="pointage-edit-page">
      <div className="page-header">
        <h1>Modifier le pointage</h1>
      </div>

      <form className="chantier-form" onSubmit={handleSubmit}>
        {formError && <div className="error-banner">{formError}</div>}

        <div className="form-group">
          <label>Date *</label>
          <input type="date" value={formData.date} onChange={handleChange('date')} required />
        </div>

        <div className="form-group">
          <label>Notes</label>
          <textarea value={formData.notes} onChange={handleChange('notes')} />
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
    </div>
  );
};

export default PointageEditPage;