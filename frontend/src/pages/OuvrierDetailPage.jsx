import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import useOuvriers from '../hooks/useOuvriers';

const formatDate = (dateStr) =>
  dateStr ? new Date(dateStr).toLocaleDateString('fr-FR') : 'N/A';

const OuvrierDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { fetchOuvrierById, removeOuvrier, loading } = useOuvriers();
  const [ouvrier, setOuvrier] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchOuvrierById(id)
      .then(setOuvrier)
      .catch(() => setError("Impossible de charger cet ouvrier."));
  }, [id]);

  const handleDelete = async () => {
    if (window.confirm('Supprimer cet ouvrier ?')) {
      await removeOuvrier(id);
      navigate('/ouvriers');
    }
  };

  if (loading && !ouvrier) {
    return <div className="loading">Chargement de l'ouvrier...</div>;
  }

  if (error) {
    return <div className="error-banner">❌ {error}</div>;
  }

  if (!ouvrier) {
    return null;
  }

  return (
    <div className="chantiers-page">
      <div className="page-header">
        <div>
          <h1>👷 {ouvrier.firstName} {ouvrier.lastName}</h1>
          <span className={`badge ${ouvrier.active ? 'badge--success' : 'badge--danger'}`}>
            {ouvrier.active ? 'Actif' : 'Inactif'}
          </span>
        </div>
        <div className="chantier-footer" style={{ borderTop: 'none', paddingTop: 0 }}>
          <Link to={`/ouvriers/${id}/modifier`} className="btn-edit">Modifier</Link>
          <button onClick={handleDelete} className="btn-delete">Supprimer</button>
          <Link to="/ouvriers" className="btn-view">Retour</Link>
        </div>
      </div>

      <div className="chantier-card" style={{ maxWidth: 520, marginBottom: 20 }}>
        <p><strong>CIN:</strong> {ouvrier.cin}</p>
        <p><strong>Spécialité:</strong> {ouvrier.specialite || 'N/A'}</p>
        <p><strong>Téléphone:</strong> {ouvrier.phone || 'N/A'}</p>
        <p><strong>Date d'embauche:</strong> {formatDate(ouvrier.hireDate)}</p>
      </div>

      {ouvrier.affectations?.length > 0 && (
        <>
          <h3 className="section-title">Historique des affectations</h3>
          <div className="table-card">
            <table>
              <thead>
                <tr>
                  <th>Chantier</th>
                  <th>Date de début</th>
                  <th>Date de fin</th>
                  <th>Statut</th>
                </tr>
              </thead>
              <tbody>
                {ouvrier.affectations.map((a) => (
                  <tr key={a.id}>
                    <td>
                      <Link to={`/chantiers/${a.siteId}`}>{a.siteName}</Link>
                    </td>
                    <td>{formatDate(a.startDate)}</td>
                    <td>{a.endDate ? formatDate(a.endDate) : '—'}</td>
                    <td>
                      <span className={`badge ${a.active ? 'badge--success' : 'badge--neutral'}`}>
                        {a.active ? 'En cours' : 'Terminée'}
                      </span>
                    </td>
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

export default OuvrierDetailPage;