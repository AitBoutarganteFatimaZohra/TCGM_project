import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import useOuvriers from '../hooks/useOuvriers';

const OuvrierEditPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { fetchOuvrierById, editOuvrier, loading, error } = useOuvriers();
  const [form, setForm] = useState(null);

  useEffect(() => {
    fetchOuvrierById(id).then((data) =>
      setForm({
        firstName: data.firstName || '',
        lastName: data.lastName || '',
        cin: data.cin || '',
        specialite: data.specialite || '',
        phone: data.phone || '',
        hireDate: data.hireDate || '',
        active: data.active,
      })
    );
  }, [id]);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm((prev) => ({ ...prev, [name]: type === 'checkbox' ? checked : value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await editOuvrier(id, form);
      navigate(`/ouvriers/${id}`);
    } catch (err) {
      // erreur déjà exposée via `error`
    }
  };

  if (!form) {
    return <div className="loading">Chargement de l'ouvrier...</div>;
  }

  return (
    <div className="chantiers-page">
      <div className="page-header">
        <h1>👷 Modifier l'ouvrier</h1>
      </div>

      {error && <div className="error-banner">❌ {error}</div>}

      <form onSubmit={handleSubmit} className="chantier-form">
        <div className="form-row">
          <div className="form-group">
            <label>Prénom *</label>
            <input type="text" name="firstName" value={form.firstName} onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label>Nom *</label>
            <input type="text" name="lastName" value={form.lastName} onChange={handleChange} required />
          </div>
        </div>

        <div className="form-group">
          <label>CIN *</label>
          <input type="text" name="cin" value={form.cin} onChange={handleChange} required />
        </div>

        <div className="form-row">
          <div className="form-group">
            <label>Spécialité</label>
            <input type="text" name="specialite" value={form.specialite} onChange={handleChange} />
          </div>
          <div className="form-group">
            <label>Téléphone</label>
            <input type="tel" name="phone" value={form.phone} onChange={handleChange} />
          </div>
        </div>

        <div className="form-group">
          <label>Date d'embauche</label>
          <input type="date" name="hireDate" value={form.hireDate} onChange={handleChange} />
        </div>

        <div className="form-group" style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
          <input
            type="checkbox"
            id="active"
            name="active"
            checked={form.active}
            onChange={handleChange}
            style={{ width: 'auto' }}
          />
          <label htmlFor="active" style={{ margin: 0 }}>Ouvrier actif</label>
        </div>

        <div className="form-actions">
          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? 'Enregistrement...' : 'Enregistrer les modifications'}
          </button>
          <button type="button" className="btn-view" onClick={() => navigate(`/ouvriers/${id}`)}>
            Annuler
          </button>
        </div>
      </form>
    </div>
  );
};

export default OuvrierEditPage;