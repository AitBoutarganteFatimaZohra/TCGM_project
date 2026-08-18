import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getAffectationById, deleteAffectation } from '../api/affectationApi';

const STATUT_LABELS = {
  PLANIFIEE: 'Planifiée',
  EN_COURS: 'En cours',
  TERMINEE: 'Terminée',
  ANNULEE: 'Annulée',
};

const STATUT_BADGE_CLASS = {
  PLANIFIEE: 'badge badge--neutral',
  EN_COURS: 'badge badge--success',
  TERMINEE: 'badge badge--neutral',
  ANNULEE: 'badge badge--danger',
};

const AffectationDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [affectation, setAffectation] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    getAffectationById(id)
      .then(setAffectation)
      .catch(() => setError('Impossible de charger cette affectation.'))
      .finally(() => setLoading(false));
  }, [id]);

  const handleDelete = async () => {
    if (window.confirm('Supprimer cette affectation ?')) {
      await deleteAffectation(id);
      navigate('/affectations');
    }
  };

  if (loading) return <div className="loading">Chargement...</div>;
  if (error) return <div className="error-banner">{error}</div>;
  if (!affectation) return null;

  return (
    <div className="affectation-detail-page">
      <div className="page-header">
        <h1>Affectation #{affectation.id}</h1>
        <span className={STATUT_BADGE_CLASS[affectation.statut] || 'badge badge--neutral'}>
          {STATUT_LABELS[affectation.statut] || affectation.statut}
        </span>
      </div>

      <div className="chantier-card" style={{ maxWidth: 560 }}>
        <div className="chantier-body">
          <p>
            <strong>Ouvrier :</strong> {affectation.ouvrier?.firstName} {affectation.ouvrier?.lastName}
          </p>
          {affectation.ouvrier?.cin && (
            <p>
              <strong>CIN :</strong> {affectation.ouvrier.cin}
            </p>
          )}
          {affectation.ouvrier?.specialite && (
            <p>
              <strong>Spécialité :</strong> {affectation.ouvrier.specialite}
            </p>
          )}
          <p>
            <strong>Chantier :</strong> {affectation.chantier?.name}
          </p>
          {affectation.chantier?.reference && (
            <p>
              <strong>Référence :</strong> {affectation.chantier.reference}
            </p>
          )}
          <p>
            <strong>Date début :</strong> {affectation.dateDebut}
          </p>
          <p>
            <strong>Date fin :</strong> {affectation.dateFin || '—'}
          </p>
        </div>

        <div className="chantier-footer">
          <Link to={`/affectations/${affectation.id}/modifier`} className="btn-edit">
            Modifier
          </Link>
          <button className="btn-delete" onClick={handleDelete}>
            Supprimer
          </button>
          <Link to="/affectations" className="btn-view">
            Retour
          </Link>
        </div>
      </div>
    </div>
  );
};

export default AffectationDetailPage;