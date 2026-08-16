import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Mail, Lock } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import tcgmLogo from '../assets/images/tcgm-logo-dark.svg';
import menaraHoldingLogo from '../assets/images/menara-holding-dark.svg';

// TODO : remplace par tes vrais comptes de démonstration une fois créés
// via /utilisateurs/nouveau. Les valeurs ci-dessous sont des placeholders.
const DEMO_ACCOUNTS = [
  { role: 'ADMIN', label: 'Administrateur', email: 'admin@tcgm.com', password: 'admin123' },
  { role: 'CHEF_PROJET', label: 'Chef de Projet', email: 'chef.projet@tcgm.com', password: 'ChangeMe123' },
  { role: 'CHEF_CHANTIER', label: 'Chef de Chantier', email: 'chef.chantier@tcgm.com', password: 'ChangeMe123' },
  { role: 'MAGASINIER', label: 'Magasinier', email: 'magasinier@tcgm.com', password: 'ChangeMe123' },
  { role: 'AGENT_SAISIE', label: 'Agent de Saisie', email: 'agent.saisie@tcgm.com', password: 'ChangeMe123' },
];

const LoginPage = () => {
  const [selectedRole, setSelectedRole] = useState('ADMIN');
  const [email, setEmail] = useState(DEMO_ACCOUNTS[0].email);
  const [password, setPassword] = useState(DEMO_ACCOUNTS[0].password);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSelectRole = (account) => {
    setSelectedRole(account.role);
    setEmail(account.email);
    setPassword(account.password);
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const result = await login(email, password);

      if (result.success) {
        navigate('/dashboard', { replace: true });
      } else {
        setError(result.message || 'Erreur de connexion');
      }
    } catch (err) {
      console.error(err);
      setError('Une erreur est survenue');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-split">
      {/* PANNEAU GAUCHE */}
      <div className="login-side">
        <div className="login-side__brand">
          <img src={tcgmLogo} alt="TCGM" />
        </div>

        <div className="login-side__content">
          <h1 className="login-side__title">
            Gestion des chantiers, centralisée.
          </h1>
          <p className="login-side__text">
            Clients, sites, ouvriers, tâches, pointage et traçabilité —
            un seul outil pour toute l'équipe.
          </p>
        </div>

        <div className="login-side__footer">
          <img src={menaraHoldingLogo} alt="Menara Holding" />
          <span>Menara Holding — TCGM</span>
        </div>
      </div>

      {/* FORMULAIRE */}
      <div className="login-form-side">
        <div className="login-form-card">
          <h2 className="login-form-card__title">Bon retour !</h2>
          <p className="login-form-card__subtitle">Connecte-toi à ton compte</p>

          {/* SÉLECTEUR DE RÔLE (démo) */}
          <div className="role-tabs">
            {DEMO_ACCOUNTS.map((account) => (
              <button
                key={account.role}
                type="button"
                className={`role-tab${selectedRole === account.role ? ' role-tab--active' : ''}`}
                onClick={() => handleSelectRole(account)}
              >
                {account.label}
              </button>
            ))}
          </div>

          {error && <div className="error-banner">❌ {error}</div>}

          <form onSubmit={handleSubmit} className="auth-form">
            <div className="input-icon-group">
              <Mail size={18} className="input-icon-group__icon" />
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="Adresse e-mail"
                required
                disabled={loading}
              />
            </div>

            <div className="input-icon-group">
              <Lock size={18} className="input-icon-group__icon" />
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Mot de passe"
                required
                disabled={loading}
              />
            </div>

            <button type="submit" className="btn-primary btn-block" disabled={loading}>
              {loading ? 'Connexion en cours...' : 'Se connecter'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;