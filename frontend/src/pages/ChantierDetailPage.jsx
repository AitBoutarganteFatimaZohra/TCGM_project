import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import useChantiers from '../hooks/useChantiers';
import ChantierMap from '../components/ChantierMap';

const STATUS_LABELS = {
  PLANIFIE: 'Planifié',
  EN_COURS: 'En cours',
  TERMINE: 'Terminé',
  SUSPENDU: 'Suspendu',
};

const formatDate = (isoString) =>
  isoString ? new Date(isoString).toLocaleDateString('fr-FR') : 'N/A';

const formatUser = (user) =>
  user ? `${user.firstName || ''} ${user.lastName || ''}`.trim() || 'N/A' : 'N/A';

const ChantierDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { fetchChantierById, removeChantier, loading } = useChantiers();
  const [chantier, setChantier] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchChantierById(id)
      .then(setChantier)
      .catch(() => setError("Impossible de charger ce chantier."));
  }, [id]);

  const handleDelete = async () => {
    if (window.confirm('Supprimer ce chantier ?')) {
      await removeChantier(id);
      navigate('/chantiers');
    }
  };

  if (loading && !chantier) {
    return <div className="loading">Chargement du chantier...</div>;
  }

  if (error) {
    return <div className="error-banner">❌ {error}</div>;
  }

  if (!chantier) {
    return null;
  }

  return (
    <div className="chantiers-page">
      <div className="page-header">
        <div>
          <h1>🏗️ {chantier.name}</h1>
          <span className={`status-badge status-${(chantier.status || '').toLowerCase()}`}>
            {STATUS_LABELS[chantier.status] || chantier.status}
          </span>
        </div>
        <div className="chantier-footer" style={{ borderTop: 'none', paddingTop: 0 }}>
          <Link to={`/chantiers/edit/${id}`} className="btn-edit">Modifier</Link>
          <button onClick={handleDelete} className="btn-delete">Supprimer</button>
          <Link to="/chantiers" className="btn-view">Retour</Link>
        </div>
      </div>

      <div className="kpi-row">
        <div className="kpi">
          <div className="num">{chantier.totalTaches ?? 0}</div>
          <div className="lbl">Tâches</div>
        </div>
        <div className="kpi">
          <div className="num">{chantier.totalOuvriers ?? 0}</div>
          <div className="lbl">Ouvriers</div>
        </div>
        <div className="kpi">
          <div className="num">{chantier.totalPointages ?? 0}</div>
          <div className="lbl">Pointages</div>
        </div>
      </div>

      <div className="chantier-card" style={{ maxWidth: 620, marginBottom: 20 }}>
        <p><strong>Référence:</strong> {chantier.reference || 'N/A'}</p>
        <p><strong>Adresse:</strong> {chantier.address || 'N/A'}</p>
        <p><strong>Date de début:</strong> {formatDate(chantier.startDate)}</p>
        <p><strong>Date de fin prévisionnelle:</strong> {formatDate(chantier.endDate)}</p>

        <hr style={{ border: 'none', borderTop: '1px solid #f3f4f6', margin: '8px 0' }} />

        <p><strong>Client:</strong> {chantier.client?.name || 'N/A'}
          {chantier.client?.phone && ` — ${chantier.client.phone}`}
        </p>
        <p><strong>Chef de projet:</strong> {formatUser(chantier.chefProjet)}</p>
        <p><strong>Chef de chantier:</strong> {formatUser(chantier.chefChantier)}</p>
        <p><strong>Magasinier:</strong> {formatUser(chantier.magasinier)}</p>
        <p><strong>Agent de saisie:</strong> {formatUser(chantier.agentSaisie)}</p>

        {chantier.description && (
          <>
            <hr style={{ border: 'none', borderTop: '1px solid #f3f4f6', margin: '8px 0' }} />
            <p style={{ marginBottom: 4 }}><strong>Description:</strong></p>
            <p style={{ whiteSpace: 'pre-wrap', color: '#4b5563' }}>{chantier.description}</p>
          </>
        )}
      </div>

      <ChantierMap latitude={chantier.latitude} longitude={chantier.longitude} name={chantier.name} />

      {chantier.taches?.length > 0 && (
        <>
          <h3 className="page-title" style={{ fontSize: 16, marginBottom: 10 }}>Tâches</h3>
          <div className="table-card" style={{ marginBottom: 20 }}>
            <table>
              <thead>
                <tr><th>Titre</th><th>Statut</th><th>Priorité</th></tr>
              </thead>
              <tbody>
                {chantier.taches.map((t) => (
                  <tr key={t.id}>
                    <td>{t.title}</td>
                    <td>{t.status}</td>
                    <td>{t.priority ?? '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}

      {chantier.ouvriers?.length > 0 && (
        <>
          <h3 className="page-title" style={{ fontSize: 16, marginBottom: 10 }}>Ouvriers affectés</h3>
          <div className="table-card">
            <table>
              <thead>
                <tr><th>Nom</th><th>CIN</th><th>Spécialité</th></tr>
              </thead>
              <tbody>
                {chantier.ouvriers.map((o) => (
                  <tr key={o.id}>
                    <td>{o.firstName} {o.lastName}</td>
                    <td className="cell-mono">{o.cin || '—'}</td>
                    <td>{o.specialite || '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  );
};

export default ChantierDetailPage;