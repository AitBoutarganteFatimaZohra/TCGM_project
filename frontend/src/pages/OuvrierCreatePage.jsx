import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import useOuvriers from '../hooks/useOuvriers';

const initialForm = {
  firstName: '',
  lastName: '',
  cin: '',
  specialite: '',
  phone: '',
  hireDate: '',
  active: true,
};

const OuvrierCreatePage = () => {
  const { addOuvrier, loading, error } = useOuvriers();
  const navigate = useNavigate();
  const [form, setForm] = useState(initialForm);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm((prev) => ({ ...prev, [name]: type === 'checkbox' ? checked : value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const created = await addOuvrier({
        ...form,
        hireDate: form.hireDate || null,
      });
      navigate(`/ouvriers/${created.id}`);
    } catch (err) {
      // erreur déjà exposée via `error`
    }
  };

  return (
    <div className="chantiers-page">
      <div className="page-header">
        <h1>👷 Nouvel ouvrier</h1>
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
            <input type="text" name="specialite" value={form.specialite} onChange={handleChange} placeholder="Ex: Maçon, Ferrailleur..." />
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
            {loading ? 'Création...' : "Créer l'ouvrier"}
          </button>
          <button type="button" className="btn-view" onClick={() => navigate('/ouvriers')}>
            Annuler
          </button>
        </div>
      </form>
    </div>
  );
};

export default OuvrierCreatePage;