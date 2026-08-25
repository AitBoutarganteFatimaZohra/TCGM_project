import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getAffectationById, deleteAffectation, validerAffectation, rejeterAffectation } from '../api/affectationApi';
// ⚠️ Adaptez ce chemin si votre hook d'authentification s'appelle
// différemment (comme dans TacheDetailPage / PointageDetailPage). Il doit
// exposer l'utilisateur connecté avec sa propriété `role`.
import useAuth from '../hooks/useAuth';

const STATUT_LABELS = {
  PLANIFIEE: 'Planifiée',
  EN_COURS: 'En cours',
  TERMINEE: 'Terminée',
  ANNULEE: 'Annulée',
  EN_ATTENTE_VALIDATION: 'En attente de validation',
  REJETEE: 'Rejetée',
};

const STATUT_BADGE_CLASS = {
  PLANIFIEE: 'badge badge--neutral',
  EN_COURS: 'badge badge--success',
  TERMINEE: 'badge badge--neutral',
  ANNULEE: 'badge badge--danger',
  EN_ATTENTE_VALIDATION: 'badge badge--warning',
  REJETEE: 'badge badge--danger',
};

const AffectationDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const role = user?.role;

  const [affectation, setAffectation] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [actionLoading, setActionLoading] = useState(false);

  const load = () => {
    getAffectationById(id)
      .then(setAffectation)
      .catch(() => setError('Impossible de charger cette affectation.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const handleDelete = async () => {
    if (window.confirm('Supprimer cette affectation ?')) {
      await deleteAffectation(id);
      navigate('/affectations');
    }
  };

  const handleValider = async () => {
    if (!window.confirm('Valider cette affectation ?')) return;
    setActionLoading(true);
    try {
      await validerAffectation(id);
      load();
    } catch (err) {
      alert(err?.response?.data?.message || 'Erreur lors de la validation');
    } finally {
      setActionLoading(false);
    }
  };

  const handleRejeter = async () => {
    const motif = window.prompt('Motif du rejet (optionnel) :', '');
    if (motif === null) return;
    setActionLoading(true);
    try {
      await rejeterAffectation(id, motif);
      load();
    } catch (err) {
      alert(err?.response?.data?.message || 'Erreur lors du rejet');
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) return <div className="loading">Chargement...</div>;
  if (error) return <div className="error-banner">{error}</div>;
  if (!affectation) return null;

  const isPending = affectation.statut === 'EN_ATTENTE_VALIDATION';
  const canValiderOuRejeter = (role === 'CHEF_PROJET' || role === 'ADMIN') && isPending;

  return (
    <div className="affectation-detail-page">
      <div className="page-header">
        <h1>Affectation #{affectation.id}</h1>
        <span className={STATUT_BADGE_CLASS[affectation.statut] || 'badge badge--neutral'}>
          {STATUT_LABELS[affectation.statut] || affectation.statut}
        </span>
      </div>

      {isPending && (
        <div className="info-banner">
          ⏳ Cette affectation attend la validation du Chef de Projet.
        </div>
      )}

      {affectation.statut === 'REJETEE' && affectation.rejectionReason && (
        <div className="error-banner">
          ⚠️ Affectation rejetée. Motif : {affectation.rejectionReason}
        </div>
      )}

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

        {canValiderOuRejeter && (
          <div className="status-actions" style={{ padding: '0 16px 16px' }}>
            <button type="button" className="btn-primary" disabled={actionLoading} onClick={handleValider}>
              ✔ Valider
            </button>
            <button type="button" className="btn-danger" disabled={actionLoading} onClick={handleRejeter}>
              ✘ Rejeter
            </button>
          </div>
        )}

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