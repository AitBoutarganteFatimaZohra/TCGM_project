import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { register } from '../api/authApi';
import useAuth from '../hooks/useAuth';

const ROLES = [
  { value: 'ADMIN', label: 'Administrateur', icon: '🛡️' },
  { value: 'CHEF_PROJET', label: 'Chef de Projet', icon: '📁' },
  { value: 'CHEF_CHANTIER', label: 'Chef de Chantier', icon: '🏗️' },
  { value: 'MAGASINIER', label: 'Magasinier', icon: '📦' },
  { value: 'AGENT_SAISIE', label: 'Agent de Saisie', icon: '📝' },
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

  const selectedRole = ROLES.find((r) => r.value === form.role);
  const initials = `${form.firstName?.[0] || ''}${form.lastName?.[0] || ''}`.toUpperCase() || '？';
  const fullName = form.firstName || form.lastName
    ? `${form.firstName} ${form.lastName}`.trim()
    : '—';

  return (
    <div className="form-page">
      <div className="form-page__header">
        <button
          type="button"
          className="form-page__back"
          onClick={() => navigate('/dashboard')}
          aria-label="Retour"
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <polyline points="15 18 9 12 15 6" />
          </svg>
        </button>
        <div>
          <h1>👤 Nouvel utilisateur</h1>
          <p className="form-page__subtitle">Créez un compte et attribuez un rôle d'accès à l'application</p>
        </div>
      </div>

      {success && (
        <div className="error-banner" style={{ background: '#dcfce7', color: '#15803d', borderColor: '#bbf7d0', marginBottom: 20 }}>
          ✅ Compte créé avec succès.
        </div>
      )}
      {error && <div className="error-banner" style={{ marginBottom: 20 }}>❌ {error}</div>}

      <form onSubmit={handleSubmit} className="form-page__body">
        <div className="form-page__main">
          {/* ---- Identité ---- */}
          <section className="form-section">
            <div className="form-section__header">
              <div className="form-section__icon">🙋</div>
              <div>
                <h3>Identité</h3>
                <p>Nom et prénom de l'utilisateur</p>
              </div>
            </div>
            <div className="form-section__body">
              <div className="form-row">
                <div className="form-group">
                  <label>Prénom *</label>
                  <input
                    type="text"
                    name="firstName"
                    value={form.firstName}
                    onChange={handleChange}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Nom *</label>
                  <input
                    type="text"
                    name="lastName"
                    value={form.lastName}
                    onChange={handleChange}
                    required
                  />
                </div>
              </div>
            </div>
          </section>

          {/* ---- Coordonnées ---- */}
          <section className="form-section">
            <div className="form-section__header">
              <div className="form-section__icon">✉️</div>
              <div>
                <h3>Coordonnées</h3>
                <p>Comment contacter cet utilisateur</p>
              </div>
            </div>
            <div className="form-section__body">
              <div className="form-group">
                <label>E-mail professionnel *</label>
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
            </div>
          </section>

          {/* ---- Rôle & accès ---- */}
          <section className="form-section">
            <div className="form-section__header">
              <div className="form-section__icon">🎭</div>
              <div>
                <h3>Rôle & accès</h3>
                <p>Détermine les pages et actions autorisées</p>
              </div>
            </div>
            <div className="form-section__body">
              <div className="form-group">
                <label>Rôle *</label>
                <select name="role" value={form.role} onChange={handleChange}>
                  {ROLES.map((r) => (
                    <option key={r.value} value={r.value}>
                      {r.icon} {r.label}
                    </option>
                  ))}
                </select>
              </div>
            </div>
          </section>

          {/* ---- Sécurité ---- */}
          <section className="form-section">
            <div className="form-section__header">
              <div className="form-section__icon">🔐</div>
              <div>
                <h3>Sécurité</h3>
                <p>Mot de passe temporaire — à faire changer à la première connexion</p>
              </div>
            </div>
            <div className="form-section__body">
              <div className="form-row">
                <div className="form-group">
                  <label>Mot de passe temporaire *</label>
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
                  <label>Confirmer *</label>
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
            </div>
          </section>

          <div className="form-page__actions">
            <button type="button" className="btn-ghost" onClick={() => navigate('/dashboard')}>
              Annuler
            </button>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Création...' : 'Créer le compte'}
            </button>
          </div>
        </div>

        {/* ---- Aperçu latéral ---- */}
        <aside className="form-page__sidebar">
          <div className="summary-card">
            <h3 className="summary-card__title">Aperçu</h3>

            <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 4 }}>
              <div
                style={{
                  width: 44,
                  height: 44,
                  borderRadius: '50%',
                  background: 'var(--tcgm-terracotta-bg)',
                  border: '1px solid var(--tcgm-terracotta-border)',
                  color: 'var(--tcgm-terracotta)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontWeight: 700,
                  fontSize: 15,
                  flexShrink: 0,
                }}
              >
                {initials}
              </div>
              <div style={{ minWidth: 0 }}>
                <div style={{ fontSize: 13.5, fontWeight: 600, color: 'var(--tcgm-black)' }}>
                  {fullName}
                </div>
                <div style={{ fontSize: 11, color: 'var(--tcgm-gray)' }}>
                  {selectedRole?.icon} {selectedRole?.label}
                </div>
              </div>
            </div>

            <div className="summary-card__row">
              <span className="summary-card__label">E-mail</span>
              <span className="summary-card__value">{form.email || '—'}</span>
            </div>
            <div className="summary-card__row">
              <span className="summary-card__label">Téléphone</span>
              <span className="summary-card__value">{form.phone || '—'}</span>
            </div>
            <div className="summary-card__row">
              <span className="summary-card__label">Rôle</span>
              <span className="summary-card__value">{selectedRole?.label}</span>
            </div>
          </div>
        </aside>
      </form>
    </div>
  );
};

export default RegisterPage;