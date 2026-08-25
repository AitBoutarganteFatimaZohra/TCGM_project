import { useState } from 'react';
import useAuth from '../hooks/useAuth';
import { changeMyPassword } from '../api/userApi';

const ROLE_LABELS = {
  ADMIN: 'Administrateur',
  CHEF_PROJET: 'Chef de projet',
  CHEF_CHANTIER: 'Chef de chantier',
  MAGASINIER: 'Magasinier',
  AGENT_SAISIE: 'Agent de saisie',
};

const getInitials = (firstName, lastName) =>
  `${(firstName?.[0] || '').toUpperCase()}${(lastName?.[0] || '').toUpperCase()}`;

const MonProfilPage = () => {
  const { user } = useAuth();
  const [form, setForm] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
    setSuccess(false);
    setError(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (form.newPassword !== form.confirmPassword) {
      setError('Les deux mots de passe ne correspondent pas.');
      return;
    }
    if (form.newPassword.length < 8) {
      setError('Le nouveau mot de passe doit contenir au moins 8 caractères.');
      return;
    }

    setLoading(true);
    setError(null);
    try {
      await changeMyPassword({
        currentPassword: form.currentPassword,
        newPassword: form.newPassword,
      });
      setSuccess(true);
      setForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
    } catch (err) {
      setError(err?.response?.data?.message || 'Erreur lors du changement de mot de passe');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="form-page">
      <div className="form-page__header">
        <div className="profile-avatar-lg">{getInitials(user?.firstName, user?.lastName)}</div>
        <div>
          <h1>{user?.firstName} {user?.lastName}</h1>
          <p className="form-page__subtitle">{ROLE_LABELS[user?.role] || user?.role}</p>
        </div>
      </div>

      <div className="form-page__body">
        <div className="form-page__main">
          {success && (
            <div className="notification notification--success" style={{ position: 'static' }}>
              Mot de passe changé avec succès.
            </div>
          )}
          {error && <div className="error-banner">❌ {error}</div>}

          <div className="form-section">
            <div className="form-section__header">
              <div className="form-section__icon">🔒</div>
              <div>
                <h3>Changer mon mot de passe</h3>
                <p>Choisissez un mot de passe d'au moins 8 caractères</p>
              </div>
            </div>
            <form onSubmit={handleSubmit} className="form-section__body">
              <div className="form-group">
                <label>Mot de passe actuel *</label>
                <input
                  type="password"
                  name="currentPassword"
                  value={form.currentPassword}
                  onChange={handleChange}
                  required
                />
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Nouveau mot de passe *</label>
                  <input
                    type="password"
                    name="newPassword"
                    value={form.newPassword}
                    onChange={handleChange}
                    minLength={8}
                    required
                  />
                </div>

                <div className="form-group">
                  <label>Confirmer le nouveau mot de passe *</label>
                  <input
                    type="password"
                    name="confirmPassword"
                    value={form.confirmPassword}
                    onChange={handleChange}
                    minLength={8}
                    required
                  />
                </div>
              </div>

              <div className="form-page__actions">
                <button type="submit" className="btn-primary" disabled={loading}>
                  {loading ? 'Enregistrement...' : 'Changer le mot de passe'}
                </button>
              </div>
            </form>
          </div>
        </div>

        <div className="form-page__sidebar">
          <div className="summary-card">
            <p className="summary-card__title">Mes informations</p>
            <div className="summary-card__row">
              <span className="summary-card__label">Nom</span>
              <span className="summary-card__value">{user?.firstName} {user?.lastName}</span>
            </div>
            <div className="summary-card__row">
              <span className="summary-card__label">Email</span>
              <span className="summary-card__value">{user?.email}</span>
            </div>
            <div className="summary-card__row">
              <span className="summary-card__label">Rôle</span>
              <span className="summary-card__value">{ROLE_LABELS[user?.role] || user?.role}</span>
            </div>
            {user?.phone && (
              <div className="summary-card__row">
                <span className="summary-card__label">Téléphone</span>
                <span className="summary-card__value">{user.phone}</span>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default MonProfilPage;