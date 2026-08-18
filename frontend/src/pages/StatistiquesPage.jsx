import useStatistiques from '../hooks/useStatistiques';

const STATUS_COLORS = {
  PLANIFIE: '#1d4ed8',
  EN_COURS: '#15803d',
  TERMINE: '#8b8580',
  SUSPENDU: '#dc2626',
};
const DEFAULT_BAR_COLOR = '#c94d25';

const BarChart = ({ data }) => {
  const entries = Object.entries(data || {});
  if (entries.length === 0) {
    return <div className="chart-empty">Aucune donnée disponible</div>;
  }
  const max = Math.max(...entries.map(([, v]) => v), 1);

  return (
    <div className="stat-bar-list">
      {entries.map(([label, value]) => (
        <div className="stat-bar-row" key={label}>
          <span className="stat-bar-row__label">{label}</span>
          <div className="stat-bar-row__track">
            <div
              className="stat-bar-row__fill"
              style={{
                width: `${(value / max) * 100}%`,
                background: STATUS_COLORS[label] || DEFAULT_BAR_COLOR,
              }}
            />
          </div>
          <span className="stat-bar-row__value">{value}</span>
        </div>
      ))}
    </div>
  );
};

const StatCard = ({ icon, label, value }) => (
  <div className="stat-card">
    <div className="stat-card__icon">{icon}</div>
    <div>
      <div className="stat-card__value">{value ?? 0}</div>
      <div className="stat-card__label">{label}</div>
    </div>
  </div>
);

const StatistiquesPage = () => {
  const {
    dashboardStats,
    sitesStats,
    ouvriersStats,
    tachesStats,
    clientsStats,
    usersStats,
    loading,
    error,
  } = useStatistiques();

  if (loading && !dashboardStats) {
    return <div className="loading">Chargement des statistiques...</div>;
  }

  return (
    <div className="dashboard-stats-page">
      <div className="page-header">
        <h1>Statistiques</h1>
      </div>

      {error && <div className="error-banner">{error}</div>}

      {dashboardStats && (
        <>
          <h2 className="section-title">Vue d'ensemble</h2>
          <div className="kpi-grid">
            <StatCard icon="🏗️" label="Chantiers" value={dashboardStats.totalSites} />
            <StatCard icon="👷" label="Ouvriers" value={dashboardStats.totalOuvriers} />
            <StatCard icon="🟢" label="Ouvriers actifs" value={dashboardStats.ouvriersActifs} />
            <StatCard icon="✅" label="Tâches" value={dashboardStats.totalTaches} />
            <StatCard icon="🏢" label="Clients" value={dashboardStats.totalClients} />
            <StatCard icon="🕒" label="Pointages" value={dashboardStats.totalPointages} />
            <StatCard icon="👤" label="Utilisateurs" value={dashboardStats.totalUsers} />
          </div>

          <div className="chart-grid">
            <div className="chart-card">
              <h3>Chantiers par statut</h3>
              <BarChart data={dashboardStats.sitesByStatus} />
            </div>
            <div className="chart-card">
              <h3>Tâches par statut</h3>
              <BarChart data={dashboardStats.tachesByStatus} />
            </div>
            <div className="chart-card">
              <h3>Pointages par statut</h3>
              <BarChart data={dashboardStats.pointagesByStatus} />
            </div>
          </div>
        </>
      )}

      {ouvriersStats && (
        <>
          <h2 className="section-title">Ouvriers par spécialité</h2>
          <div className="chart-grid">
            <div className="chart-card">
              <h3>Répartition par spécialité</h3>
              <BarChart data={ouvriersStats.ouvriersBySpecialite} />
            </div>
          </div>
        </>
      )}

      {sitesStats && (
        <>
          <h2 className="section-title">Chantiers par client</h2>
          <div className="chart-grid">
            <div className="chart-card">
              <h3>Répartition par client</h3>
              <BarChart data={sitesStats.sitesByClient} />
            </div>
          </div>
        </>
      )}

      {tachesStats && (
        <>
          <h2 className="section-title">Tâches par priorité</h2>
          <div className="chart-grid">
            <div className="chart-card">
              <h3>Répartition par priorité</h3>
              <BarChart data={tachesStats.tachesByPriority} />
            </div>
          </div>
        </>
      )}

      {clientsStats && (
        <>
          <h2 className="section-title">Clients</h2>
          <div className="kpi-grid">
            <StatCard icon="🏢" label="Total clients" value={clientsStats.totalClients} />
          </div>
        </>
      )}

      {usersStats && (
        <>
          <h2 className="section-title">Utilisateurs (Admin)</h2>
          <div className="kpi-grid">
            <StatCard icon="👤" label="Total utilisateurs" value={usersStats.totalUsers} />
            <StatCard icon="🟢" label="Utilisateurs actifs" value={usersStats.usersActifs} />
          </div>
        </>
      )}
    </div>
  );
};

export default StatistiquesPage;