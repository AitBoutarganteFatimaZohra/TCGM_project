import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { register } from '../api/authApi';
import useAuth from '../hooks/useAuth';

const ROLES = [
  { value: 'ADMIN', label: 'Administrateur' },
  { value: 'CHEF_PROJET', label: 'Chef de Projet' },
  { value: 'CHEF_CHANTIER', label: 'Chef de Chantier' },
  { value: 'MAGASINIER', label: 'Magasinier' },
  { value: 'AGENT_SAISIE', label: 'Agent de Saisie' },
];

const initialForm = {
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  confirmPassword: '',
  phone: '',
  role: 'AGENT_SAISIE',
};

const RegisterPage = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState(initialForm);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(false);

  // Garde-fou : seul un Admin connecté peut créer des comptes.
  if (user?.role !== 'ADMIN') {
    return <Navigate to="/dashboard" replace />;
  }

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess(false);

    if (form.password !== form.confirmPassword) {
      setError('Les mots de passe ne correspondent pas.');
      return;
    }

    setLoading(true);
    try {
      await register({
        firstName: form.firstName,
        lastName: form.lastName,
        email: form.email,
        password: form.password,
        phone: form.phone || null,
        role: form.role,
      });

      setSuccess(true);
      setForm(initialForm);
    } catch (err) {
      setError(
        err?.response?.data?.message || 'Erreur lors de la création du compte.'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="chantiers-page">
      <div className="page-header">
        <h1>👤 Nouvel utilisateur</h1>
      </div>

      {success && (
        <div className="success-banner">
          ✅ Compte créé avec succès.
        </div>
      )}
      {error && <div className="error-banner">❌ {error}</div>}

      <form onSubmit={handleSubmit} className="chantier-form">
        <div className="form-row">
          <div className="form-group">
            <label>Prénom</label>
            <input
              type="text"
              name="firstName"
              value={form.firstName}
              onChange={handleChange}
              required
            />
          </div>
          <div className="form-group">
            <label>Nom</label>
            <input
              type="text"
              name="lastName"
              value={form.lastName}
              onChange={handleChange}
              required
            />
          </div>
        </div>

        <div className="form-group">
          <label>E-mail professionnel</label>
          <input
            type="email"
            name="email"
            value={form.email}
            onChange={handleChange}
            placeholder="prenom.nom@tcgm.ma"
            required
          />
        </div>

        <div className="form-group">
          <label>Téléphone</label>
          <input
            type="tel"
            name="phone"
            value={form.phone}
            onChange={handleChange}
            placeholder="06 12 34 56 78"
          />
        </div>

        <div className="form-group">
          <label>Rôle</label>
          <select name="role" value={form.role} onChange={handleChange}>
            {ROLES.map((r) => (
              <option key={r.value} value={r.value}>
                {r.label}
              </option>
            ))}
          </select>
        </div>

        <div className="form-row">
          <div className="form-group">
            <label>Mot de passe temporaire</label>
            <input
              type="password"
              name="password"
              value={form.password}
              onChange={handleChange}
              required
              minLength={8}
            />
          </div>
          <div className="form-group">
            <label>Confirmer</label>
            <input
              type="password"
              name="confirmPassword"
              value={form.confirmPassword}
              onChange={handleChange}
              required
              minLength={8}
            />
          </div>
        </div>

        <div className="form-actions">
          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? 'Création...' : 'Créer le compte'}
          </button>
          <button type="button" className="btn-view" onClick={() => navigate('/dashboard')}>
            Annuler
          </button>
        </div>
      </form>
    </div>
  );
};

export default RegisterPage;