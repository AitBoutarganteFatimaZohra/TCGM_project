import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import useTravaux from '../hooks/useTravaux';
import { updateTravauxStatut } from '../api/travauxApi';

const STATUT_LABELS = {
  PLANIFIE: 'Planifié',
  EN_COURS: 'En cours',
  TERMINE: 'Terminé',
  SUSPENDU: 'Suspendu',
};

const formatDate = (isoString) =>
  isoString ? new Date(isoString).toLocaleDateString('fr-FR') : 'N/A';

const TravauxDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { fetchTravauxById, removeTravaux, loading } = useTravaux();
  const [travaux, setTravaux] = useState(null);
  const [error, setError] = useState(null);
  const [statutUpdating, setStatutUpdating] = useState(false);

  const loadTravaux = () => {
    fetchTravauxById(id)
      .then(setTravaux)
      .catch(() => setError('Impossible de charger ces travaux.'));
  };

  useEffect(() => {
    loadTravaux();
  }, [id]);

  const handleDelete = async () => {
    if (window.confirm('Supprimer ces travaux ?')) {
      await removeTravaux(id);
      navigate('/travaux');
    }
  };

  const handleStatutChange = async (newStatut) => {
    setStatutUpdating(true);
    try {
      await updateTravauxStatut(id, newStatut);
      loadTravaux();
    } catch (err) {
      alert('Erreur lors du changement de statut');
    } finally {
      setStatutUpdating(false);
    }
  };

  if (loading && !travaux) {
    return <div className="loading">Chargement des travaux...</div>;
  }

  if (error) {
    return <div className="error-banner">❌ {error}</div>;
  }

  if (!travaux) {
    return null;
  }

  return (
    <div className="travaux-page">
      <div className="page-header">
        <div>
          <h1>🔧 {travaux.intitule}</h1>
          <span className={`status-badge status-${(travaux.statut || '').toLowerCase()}`}>
            {STATUT_LABELS[travaux.statut] || travaux.statut}
          </span>
        </div>
        <div className="chantier-footer" style={{ borderTop: 'none', paddingTop: 0 }}>
          <Link to={`/travaux/edit/${id}`} className="btn-edit">Modifier</Link>
          <button onClick={handleDelete} className="btn-delete">Supprimer</button>
          <Link to="/travaux" className="btn-view">Retour</Link>
        </div>
      </div>

      <div className="kpi-row">
        <div className="kpi">
          <div className="num">{travaux.totalTaches ?? 0}</div>
          <div className="lbl">Tâches</div>
        </div>
        <div className="kpi">
          <div className="num">{travaux.totalTachesTerminees ?? 0}</div>
          <div className="lbl">Tâches terminées</div>
        </div>
        <div className="kpi">
          <div className="num">{travaux.priorite ?? '—'}</div>
          <div className="lbl">Priorité</div>
        </div>
      </div>

      <div className="chantier-card" style={{ maxWidth: 620, marginBottom: 20 }}>
        <p><strong>Code:</strong> <span className="cell-mono">{travaux.code}</span></p>
        <p><strong>Chantier:</strong> {travaux.chantier?.name || 'N/A'}
          {travaux.chantier?.reference && ` (${travaux.chantier.reference})`}
        </p>
        <p><strong>Date de début prévue:</strong> {formatDate(travaux.dateDebutPrevue)}</p>
        <p><strong>Date de fin prévue:</strong> {formatDate(travaux.dateFinPrevue)}</p>
        <p><strong>Date de début réelle:</strong> {formatDate(travaux.dateDebutReelle)}</p>
        <p><strong>Date de fin réelle:</strong> {formatDate(travaux.dateFinReelle)}</p>
        <p><strong>Budget estimé:</strong> {travaux.budgetEstime ? `${travaux.budgetEstime} DH` : 'N/A'}</p>

        {travaux.description && (
          <>
            <hr style={{ border: 'none', borderTop: '1px solid #f3f4f6', margin: '8px 0' }} />
            <p style={{ marginBottom: 4 }}><strong>Description:</strong></p>
            <p style={{ whiteSpace: 'pre-wrap', color: '#4b5563' }}>{travaux.description}</p>
          </>
        )}
      </div>

      <div className="chantier-card" style={{ maxWidth: 620, marginBottom: 20 }}>
        <p style={{ marginBottom: 8 }}><strong>Changer le statut :</strong></p>
        <div className="status-actions">
          {Object.entries(STATUT_LABELS).map(([value, label]) => (
            <button
              key={value}
              type="button"
              className={`btn-status ${travaux.statut === value ? 'btn-status--active' : ''}`}
              disabled={statutUpdating || travaux.statut === value}
              onClick={() => handleStatutChange(value)}
            >
              {label}
            </button>
          ))}
        </div>
      </div>

      {travaux.taches?.length > 0 && (
        <>
          <h3 className="page-title" style={{ fontSize: 16, marginBottom: 10 }}>Tâches</h3>
          <div className="table-card">
            <table>
              <thead>
                <tr><th>Titre</th><th>Statut</th><th>Priorité</th></tr>
              </thead>
              <tbody>
                {travaux.taches.map((t) => (
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
    </div>
  );
};

export default TravauxDetailPage;