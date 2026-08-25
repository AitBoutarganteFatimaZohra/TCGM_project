import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { AlertTriangle, Building2, Clock, ShieldAlert, CheckCircle2 } from 'lucide-react';

import { useAuth } from '../hooks/useAuth';
import { getMySites } from '../api/chantierApi'; // ajuste le chemin si besoin
import { getMyAlertes, resolveAlerte } from '../api/alerteApi';

const STATUS_LABELS = {
  PLANIFIE: 'Planifié',
  EN_COURS: 'En cours',
  TERMINE: 'Terminé',
  SUSPENDU: 'Suspendu',
};

// Icône + couleur par type d'alerte (⚠️ ajoute une entrée ici pour
// chaque nouveau TypeAlerte créé côté backend)
const ALERTE_CONFIG = {
  RETARD: { icon: Clock, color: '#dc2626' },
  SANS_CHEF_CHANTIER: { icon: ShieldAlert, color: '#d97706' },
};

const ChefProjetDashboardPage = () => {
  const { user } = useAuth();
  const [chantiers, setChantiers] = useState([]);
  const [alertes, setAlertes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [resolvingId, setResolvingId] = useState(null);

  const chargerAlertes = () => {
    return getMyAlertes().then((data) => setAlertes(Array.isArray(data) ? data : []));
  };

  useEffect(() => {
    setLoading(true);
    Promise.all([
      getMySites().then((data) => setChantiers(Array.isArray(data) ? data : data?.content || [])),
      chargerAlertes(),
    ])
      .catch((err) => setError(err?.response?.data?.message || 'Erreur de chargement'))
      .finally(() => setLoading(false));
  }, []);

  const handleResolve = async (alerteId) => {
    setResolvingId(alerteId);
    try {
      await resolveAlerte(alerteId);
      // Retire l'alerte résolue de la liste affichée sans tout recharger
      setAlertes((prev) => prev.filter((a) => a.id !== alerteId));
    } catch (err) {
      setError(err?.response?.data?.message || "Impossible de résoudre l'alerte");
    } finally {
      setResolvingId(null);
    }
  };

  const now = new Date();
  const enCours = chantiers.filter((c) => c.status === 'EN_COURS').length;
  const planifies = chantiers.filter((c) => c.status === 'PLANIFIE').length;

  if (loading) {
    return <div className="loading">Chargement...</div>;
  }

  if (error) {
    return <div className="error-banner">❌ {error}</div>;
  }

  return (
    <div className="dashboard-stats-page">
      <div className="page-header">
        <div>
          <h1 className="page-header__title">📊 Mes chantiers</h1>
          <p className="page-header__subtitle">
            Bonjour, {user?.firstName || ''} {user?.lastName || ''} —{' '}
            {now.toLocaleDateString('fr-FR', {
              weekday: 'long',
              year: 'numeric',
              month: 'long',
              day: 'numeric',
            })}
          </p>
        </div>
      </div>

      <div className="kpi-grid">
        <StatCard Icon={Building2} value={chantiers.length} label="Mes chantiers" color="#2563eb" />
        <StatCard Icon={Clock} value={enCours} label="En cours" color="#16a34a" />
        <StatCard Icon={Building2} value={planifies} label="Planifiés" color="#6b7280" />
        <StatCard Icon={AlertTriangle} value={alertes.length} label="Alertes" color="#dc2626" />
      </div>

      <h3 className="section-title">Alertes</h3>
      <div className="chantier-card" style={{ marginBottom: 24 }}>
        {alertes.length === 0 ? (
          <p style={{ color: '#8b8580' }}>Aucune alerte — tout est en ordre.</p>
        ) : (
          alertes.map((alerte) => {
            const config = ALERTE_CONFIG[alerte.type] || { icon: AlertTriangle, color: '#dc2626' };
            const Icon = config.icon;
            return (
              <div key={alerte.id} className="activity-row" style={{ alignItems: 'center' }}>
                <Icon size={18} color={config.color} style={{ flexShrink: 0 }} />
                <div style={{ flex: 1 }}>
                  <Link to={`/chantiers/${alerte.siteId}`} className="activity-text" style={{ color: '#1a1a2e' }}>
                    {alerte.message} — {alerte.siteName}
                  </Link>
                </div>
                <button
                  type="button"
                  className="btn-view"
                  disabled={resolvingId === alerte.id}
                  onClick={() => handleResolve(alerte.id)}
                  style={{ display: 'flex', alignItems: 'center', gap: 6, flexShrink: 0 }}
                >
                  <CheckCircle2 size={16} />
                  {resolvingId === alerte.id ? 'Résolution...' : 'Résoudre'}
                </button>
              </div>
            );
          })
        )}
      </div>

      <h3 className="section-title">Mes chantiers</h3>
      <div className="table-card">
        {chantiers.length === 0 ? (
          <div className="empty-state">
            <p>Aucun chantier ne t'est encore assigné en tant que Chef de Projet.</p>
          </div>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Référence</th>
                <th>Nom</th>
                <th>Client</th>
                <th>Statut</th>
              </tr>
            </thead>
            <tbody>
              {chantiers.map((site) => (
                <tr key={site.id}>
                  <td className="cell-mono">{site.reference || '—'}</td>
                  <td>
                    <Link to={`/chantiers/${site.id}`}>{site.name}</Link>
                  </td>
                  <td>{site.client?.name || '—'}</td>
                  <td>
                    <span className={`status-badge status-${(site.status || '').toLowerCase()}`}>
                      {STATUS_LABELS[site.status] || site.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

const StatCard = ({ Icon, value, label, color }) => (
  <div className="stat-card">
    <div className="stat-card__icon" style={{ backgroundColor: `${color}1a`, color }}>
      <Icon size={22} strokeWidth={2} />
    </div>
    <div>
      <div className="stat-card__value">{value}</div>
      <div className="stat-card__label">{label}</div>
    </div>
  </div>
);

export default ChefProjetDashboardPage;