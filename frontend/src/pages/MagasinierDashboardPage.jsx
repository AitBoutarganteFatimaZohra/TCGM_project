import { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import useChantiers from '../hooks/useChantiers';
import useRessources from '../hooks/useRessources';
import useMySites from '../hooks/useMySites';
import { getJournalBySite } from '../api/journalApi';

const STATUT_LABELS = {
  PLANIFIE: 'Planifié',
  EN_COURS: 'En cours',
  TERMINE: 'Terminé',
  SUSPENDU: 'Suspendu',
};

const ACTION_LABELS = {
  CREATION: 'Création',
  MODIFICATION: 'Modification',
  SUPPRESSION: 'Suppression',
};

const formatDate = (isoString) =>
  isoString ? new Date(isoString).toLocaleDateString('fr-FR') : 'N/A';

const formatDateTime = (isoString) =>
  isoString ? new Date(isoString).toLocaleString('fr-FR') : 'N/A';

const formatUser = (user) =>
  user ? `${user.firstName || ''} ${user.lastName || ''}`.trim() || 'N/A' : 'N/A';

const getInitials = (firstName, lastName) =>
  `${(firstName?.[0] || '').toUpperCase()}${(lastName?.[0] || '').toUpperCase()}`;

const MagasinierDashboardPage = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { sites, loading: loadingSites } = useMySites();
  const { fetchChantierById } = useChantiers();
  const { ressources, fetchRessources } = useRessources();

  const [selectedSiteId, setSelectedSiteId] = useState(null);
  const [chantier, setChantier] = useState(null);
  const [loadingChantier, setLoadingChantier] = useState(false);
  const [recentActivity, setRecentActivity] = useState([]);

  useEffect(() => {
    if (sites.length > 0 && !selectedSiteId) setSelectedSiteId(sites[0].id);
  }, [sites]);

  useEffect(() => {
    if (!selectedSiteId) return;
    setLoadingChantier(true);
    fetchChantierById(selectedSiteId)
      .then(setChantier)
      .finally(() => setLoadingChantier(false));
    fetchRessources(selectedSiteId);
    getJournalBySite(selectedSiteId, { size: 5, sort: 'createdAt,desc' })
      .then((data) => setRecentActivity(data.content || []))
      .catch(() => setRecentActivity([]));
  }, [selectedSiteId]);

  const counts = ressources.reduce((acc, r) => {
    acc[r.statut] = (acc[r.statut] || 0) + 1;
    return acc;
  }, {});

  const stockCritique = ressources.filter(
    (r) => r.seuilAlerte != null && r.quantite != null && r.quantite <= r.seuilAlerte
  );

  if (loadingSites) {
    return <div className="loading">Chargement...</div>;
  }

  if (sites.length === 0) {
    return (
      <div className="empty-state">
        <p>Vous n'êtes affecté à aucun chantier pour le moment.</p>
      </div>
    );
  }

  return (
    <div className="dashboard-stats-page">
      <div className="page-header">
        <h1>
          👋 Bonjour {user?.firstName}
          {chantier && <small> — {chantier.name}</small>}
        </h1>

        {sites.length > 1 && (
          <select
            className="filter-select"
            value={selectedSiteId || ''}
            onChange={(e) => setSelectedSiteId(Number(e.target.value))}
          >
            {sites.map((s) => (
              <option key={s.id} value={s.id}>{s.name}</option>
            ))}
          </select>
        )}
      </div>

      {loadingChantier || !chantier ? (
        <div className="loading">Chargement du chantier...</div>
      ) : (
        <>
          {/* Alerte stock critique */}
          {stockCritique.length > 0 && (
            <div className="notification notification--warning" style={{ position: 'static', marginBottom: 20 }}>
              ⚠️ <strong>{stockCritique.length}</strong> ressource(s) en stock critique :{' '}
              {stockCritique.map((r) => r.nom).join(', ')} —{' '}
              <Link to="/ressources" style={{ textDecoration: 'underline' }}>voir les ressources</Link>
            </div>
          )}

          {/* Indicateurs Matériel & Ressources */}
          <h3 className="section-title">Matériel & Ressources</h3>
          <div className="kpi-grid">
            <div className="stat-card">
              <div className="stat-card__icon">📦</div>
              <div>
                <div className="stat-card__value">{ressources.length}</div>
                <div className="stat-card__label">Ressources au total</div>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-card__icon stat-card__icon--success">✅</div>
              <div>
                <div className="stat-card__value">{counts.DISPONIBLE || 0}</div>
                <div className="stat-card__label">Disponibles</div>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-card__icon stat-card__icon--info">🔧</div>
              <div>
                <div className="stat-card__value">{counts.EN_UTILISATION || 0}</div>
                <div className="stat-card__label">En utilisation</div>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-card__icon stat-card__icon--danger">⚠️</div>
              <div>
                <div className="stat-card__value">
                  {(counts.HORS_SERVICE || 0) + (counts.EN_MAINTENANCE || 0)}
                </div>
                <div className="stat-card__label">Hors service / maintenance</div>
              </div>
            </div>
          </div>

          <div className="chart-grid">
            {/* Informations du chantier */}
            <div className="chart-card">
              <h3>Informations du chantier</h3>
              <p><strong>Nom:</strong> {chantier.name}</p>
              <p><strong>Adresse:</strong> {chantier.address || 'N/A'}</p>
              <p>
                <strong>Statut:</strong>{' '}
                <span className={`status-badge status-${(chantier.status || '').toLowerCase()}`}>
                  {STATUT_LABELS[chantier.status] || chantier.status}
                </span>
              </p>
              <p><strong>Date de début:</strong> {formatDate(chantier.startDate)}</p>
              <p><strong>Date de fin prévisionnelle:</strong> {formatDate(chantier.endDate)}</p>
              <hr style={{ border: 'none', borderTop: '1px solid #f3f4f6', margin: '8px 0' }} />
              <p><strong>Client:</strong> {chantier.client?.name || 'N/A'}
                {chantier.client?.phone && ` — ${chantier.client.phone}`}
              </p>
              <p><strong>Chef de projet:</strong> {formatUser(chantier.chefProjet)}</p>
              <p><strong>Chef de chantier:</strong> {formatUser(chantier.chefChantier)}</p>
            </div>

            {/* Équipe sur site */}
            <div className="chart-card chart-card--scroll">
              <h3>Équipe sur site ({chantier.ouvriers?.length || 0})</h3>
              {(!chantier.ouvriers || chantier.ouvriers.length === 0) ? (
                <p className="chart-empty">Aucun ouvrier affecté</p>
              ) : (
                chantier.ouvriers.map((o) => (
                  <div className="worker-row" key={o.id}>
                    <div className="worker-avatar">{getInitials(o.firstName, o.lastName)}</div>
                    <div className="worker-info">
                      <p className="worker-name">{o.firstName} {o.lastName}</p>
                      {o.cin && <span className="worker-cin">{o.cin}</span>}
                    </div>
                    {o.specialite && (
                      <span className="badge-specialite">{o.specialite}</span>
                    )}
                  </div>
                ))
              )}
            </div>

            {/* Mini-journal d'activités récentes */}
            <div className="chart-card">
              <h3>Activités récentes</h3>
              {recentActivity.length === 0 ? (
                <p className="chart-empty">Aucune activité récente</p>
              ) : (
                recentActivity.map((entry) => (
                  <div className="activity-row" key={entry.id}>
                    <div className="activity-dot" />
                    <div>
                      <p className="activity-text">
                        {ACTION_LABELS[entry.actionType] || entry.actionType} — {entry.details}
                      </p>
                      <span className="activity-meta">{formatDateTime(entry.createdAt)}</span>
                    </div>
                  </div>
                ))
              )}
              <div style={{ marginTop: 12 }}>
                <Link to="/mon-journal" style={{ fontSize: 13 }}>Voir tout le journal →</Link>
              </div>
            </div>
          </div>

          <div className="form-actions" style={{ marginTop: 20 }}>
            <button className="btn-primary" onClick={() => navigate('/ressources')}>
              Gérer les ressources →
            </button>
          </div>
        </>
      )}
    </div>
  );
};

export default MagasinierDashboardPage;