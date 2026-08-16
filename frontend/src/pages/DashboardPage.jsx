import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  PieChart,
  Pie,
  Cell,
  Tooltip,
  Legend,
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
} from 'recharts';
import {
  Building2,
  HardHat,
  ListChecks,
  Users,
  UserCog,
  Clock,
  Activity,
} from 'lucide-react';

import { useAuth } from '../hooks/useAuth';
import { getDashboardStats } from '../api/statistiqueApi';

const STATUS_LABELS = {
  PLANIFIE: 'Planifié',
  EN_COURS: 'En cours',
  TERMINE: 'Terminé',
  SUSPENDU: 'Suspendu',
};

const STATUS_COLORS = {
  PLANIFIE: '#2563eb',
  EN_COURS: '#16a34a',
  TERMINE: '#6b7280',
  SUSPENDU: '#dc2626',
};

const BAR_COLOR = '#1a1a2e';

// Transforme une Map<String,Long> renvoyée par le backend en tableau
// exploitable par recharts, avec label + couleur pour les statuts connus.
const toChartData = (map, labels = {}, colors = {}) =>
  Object.entries(map || {}).map(([key, value]) => ({
    name: labels[key] || key,
    value,
    color: colors[key] || BAR_COLOR,
  }));

const DashboardPage = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const data = await getDashboardStats();
        setStats(data);
      } catch (err) {
        console.error('Erreur dashboard :', err);
        setError('Erreur lors du chargement des statistiques');
      } finally {
        setLoading(false);
      }
    };

    fetchStats();
  }, []);

  if (loading) {
    return <div className="loading">Chargement des statistiques...</div>;
  }

  if (error) {
    return (
      <div className="error-banner">
        ❌ {error} — vérifie que le backend est démarré et que
        l'API des statistiques fonctionne.
      </div>
    );
  }

  const sitesData = toChartData(stats?.sitesByStatus, STATUS_LABELS, STATUS_COLORS);
  const tachesData = toChartData(stats?.tachesByStatus);
  const specialiteData = toChartData(stats?.ouvriersBySpecialite);
  const clientData = toChartData(stats?.sitesByClient);

  return (
    <div className="dashboard-stats-page">
      <div className="page-header">
        <div>
          <h1 className="page-header__title">📊 Dashboard</h1>
          <p className="page-header__subtitle">
            Bonjour, {user?.firstName || ''} {user?.lastName || ''} —{' '}
            {new Date().toLocaleDateString('fr-FR', {
              weekday: 'long',
              year: 'numeric',
              month: 'long',
              day: 'numeric',
            })}
          </p>
        </div>
      </div>

      {/* ======================== KPI ======================== */}
      <div className="kpi-grid">
        <StatCard Icon={Building2} value={stats?.totalSites || 0} label="Chantiers" color="#2563eb" />
        <StatCard Icon={HardHat} value={stats?.totalOuvriers || 0} label="Ouvriers" color="#d97706" />
        <StatCard Icon={Activity} value={stats?.ouvriersActifs || 0} label="Ouvriers actifs" color="#16a34a" />
        <StatCard Icon={ListChecks} value={stats?.totalTaches || 0} label="Tâches" color="#7c3aed" />
        <StatCard Icon={Users} value={stats?.totalClients || 0} label="Clients" color="#0891b2" />
        <StatCard Icon={Clock} value={stats?.totalPointages || 0} label="Pointages" color="#dc2626" />
        <StatCard Icon={UserCog} value={stats?.totalUsers || 0} label="Utilisateurs" color="#1a1a2e" />
      </div>

      {/* ======================== GRAPHIQUES ======================== */}
      <div className="chart-grid">
        <div className="chart-card">
          <h3>Chantiers par statut</h3>
          {sitesData.length === 0 ? (
            <p className="chart-empty">Aucune donnée</p>
          ) : (
            <ResponsiveContainer width="100%" height={240}>
              <PieChart>
                <Pie
                  data={sitesData}
                  dataKey="value"
                  nameKey="name"
                  innerRadius={55}
                  outerRadius={85}
                  paddingAngle={2}
                >
                  {sitesData.map((entry) => (
                    <Cell key={entry.name} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          )}
        </div>

        <div className="chart-card">
          <h3>Tâches par statut</h3>
          {tachesData.length === 0 ? (
            <p className="chart-empty">Aucune donnée</p>
          ) : (
            <ResponsiveContainer width="100%" height={240}>
              <BarChart data={tachesData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="name" tick={{ fontSize: 12 }} />
                <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
                <Tooltip />
                <Bar dataKey="value" fill={BAR_COLOR} radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>

        <div className="chart-card">
          <h3>Ouvriers par spécialité</h3>
          {specialiteData.length === 0 ? (
            <p className="chart-empty">Aucune donnée</p>
          ) : (
            <ResponsiveContainer width="100%" height={240}>
              <BarChart data={specialiteData} layout="vertical" margin={{ left: 20 }}>
                <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                <XAxis type="number" allowDecimals={false} tick={{ fontSize: 12 }} />
                <YAxis type="category" dataKey="name" width={100} tick={{ fontSize: 12 }} />
                <Tooltip />
                <Bar dataKey="value" fill="#d97706" radius={[0, 4, 4, 0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>

        <div className="chart-card">
          <h3>Chantiers par client</h3>
          {clientData.length === 0 ? (
            <p className="chart-empty">Aucune donnée</p>
          ) : (
            <ResponsiveContainer width="100%" height={240}>
              <BarChart data={clientData} layout="vertical" margin={{ left: 20 }}>
                <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                <XAxis type="number" allowDecimals={false} tick={{ fontSize: 12 }} />
                <YAxis type="category" dataKey="name" width={100} tick={{ fontSize: 12 }} />
                <Tooltip />
                <Bar dataKey="value" fill="#0891b2" radius={[0, 4, 4, 0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>

      {/* ======================== CHANTIERS RÉCENTS ======================== */}
      {stats?.sitesRecents?.length > 0 && (
        <>
          <h3 className="section-title">Chantiers récents</h3>
          <div className="table-card" style={{ marginBottom: 24 }}>
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
                {stats.sitesRecents.map((site) => (
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
          </div>
        </>
      )}

      {/* ======================== ACTIVITÉ RÉCENTE ======================== */}
      {stats?.recentActivity?.length > 0 && (
        <>
          <h3 className="section-title">Activité récente</h3>
          <div className="chantier-card">
            {stats.recentActivity.map((entry, i) => (
              <div key={entry.id || i} className="activity-row">
                <span className="activity-dot" />
                <div>
                  <p className="activity-text">
                    {entry.description || entry.action || entry.type || 'Action effectuée'}
                  </p>
                  <span className="activity-meta">
                    {entry.author || entry.user || ''}
                    {entry.createdAt &&
                      ` — ${new Date(entry.createdAt).toLocaleString('fr-FR')}`}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </>
      )}
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

export default DashboardPage;