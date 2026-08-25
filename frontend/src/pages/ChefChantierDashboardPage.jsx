import { useEffect, useState } from 'react';
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
  ListChecks,
  Clock,
  Activity,
} from 'lucide-react';

import { useAuth } from '../hooks/useAuth';
import { getDashboardStats } from '../api/statistiqueApi';

const TACHE_STATUS_LABELS = {
  PLANIFIEE: 'Planifiée',
  EN_COURS: 'En cours',
  TERMINEE: 'Terminée',
};

// ⚠️ À vérifier/ajuster selon les valeurs exactes de ton enum
// de statut de dossier de pointage (StatutDossierPointage ou équivalent).
const POINTAGE_STATUS_LABELS = {
  EN_ATTENTE: 'En attente',
  VALIDE: 'Validé',
  REJETE: 'Rejeté',
};

const POINTAGE_STATUS_COLORS = {
  EN_ATTENTE: '#d97706',
  VALIDE: '#16a34a',
  REJETE: '#dc2626',
};

const BAR_COLOR = '#1a1a2e';

const toChartData = (map, labels = {}, colors = {}) =>
  Object.entries(map || {}).map(([key, value]) => ({
    name: labels[key] || key,
    value,
    color: colors[key] || BAR_COLOR,
  }));

const ChefChantierDashboardPage = () => {
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

  const tachesData = toChartData(stats?.tachesByStatus, TACHE_STATUS_LABELS);
  const pointagesData = toChartData(
    stats?.pointagesByStatus,
    POINTAGE_STATUS_LABELS,
    POINTAGE_STATUS_COLORS
  );

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
        <StatCard
          Icon={Building2}
          value={stats?.totalChantiers || 0}
          label="Mes chantiers"
          color="#2563eb"
        />
        <StatCard
          Icon={Activity}
          value={stats?.ouvriersActifs || 0}
          label="Ouvriers actifs"
          color="#16a34a"
        />
        <StatCard
          Icon={ListChecks}
          value={stats?.totalTaches || 0}
          label="Tâches"
          color="#7c3aed"
        />
        <StatCard
          Icon={Clock}
          value={stats?.pointagesEnAttente || 0}
          label="Pointages en attente"
          color="#dc2626"
        />
      </div>

      {/* ======================== GRAPHIQUES ======================== */}
      <div className="chart-grid">
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
          <h3>Pointages par statut</h3>
          {pointagesData.length === 0 ? (
            <p className="chart-empty">Aucune donnée</p>
          ) : (
            <ResponsiveContainer width="100%" height={240}>
              <PieChart>
                <Pie
                  data={pointagesData}
                  dataKey="value"
                  nameKey="name"
                  innerRadius={55}
                  outerRadius={85}
                  paddingAngle={2}
                >
                  {pointagesData.map((entry) => (
                    <Cell key={entry.name} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          )}
        </div>
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

export default ChefChantierDashboardPage;