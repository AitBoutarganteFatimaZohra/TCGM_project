import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import useRessources from '../hooks/useRessources';
import { validerRessource, rejeterRessource } from '../api/ressourceApi';
// ⚠️ Adaptez ce chemin si besoin (même hook que TacheDetailPage/AffectationDetailPage).
import useAuth from '../hooks/useAuth';

const TYPE_LABELS = {
  MATERIEL: 'Matériel',
  EQUIPEMENT: 'Équipement',
  OUTIL: 'Outil',
  CONSOMMABLE: 'Consommable',
};

const STATUT_LABELS = {
  DISPONIBLE: 'Disponible',
  EN_UTILISATION: 'En utilisation',
  HORS_SERVICE: 'Hors service',
  EN_MAINTENANCE: 'En maintenance',
};

const VALIDATION_LABELS = {
  VALIDEE: null, // rien à afficher, état stable
  EN_ATTENTE_CHEF_CHANTIER: 'En attente de validation (Chef de Chantier)',
  EN_ATTENTE_CHEF_PROJET: 'En attente de validation (Chef de Projet — recours)',
  REJETEE: 'Rejetée définitivement',
};

const ACTION_LABELS = {
  CREATION: 'création',
  MODIFICATION: 'modification',
  CHANGEMENT_STATUT: 'changement de statut',
  SUPPRESSION: 'suppression',
};

const formatDate = (isoString) =>
  isoString ? new Date(isoString).toLocaleDateString('fr-FR') : 'N/A';

const RessourceDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { fetchRessourceById, removeRessource, loading } = useRessources();
  const { user } = useAuth();
  const role = user?.role;

  const [ressource, setRessource] = useState(null);
  const [error, setError] = useState(null);
  const [actionLoading, setActionLoading] = useState(false);

  const load = () => {
    fetchRessourceById(id)
      .then(setRessource)
      .catch(() => setError("Impossible de charger cette ressource."));
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const handleDelete = async () => {
    if (window.confirm('Soumettre la suppression de cette ressource pour validation ?')) {
      await removeRessource(id);
      load();
    }
  };

  const handleValider = async () => {
    if (!window.confirm('Valider cette action ?')) return;
    setActionLoading(true);
    try {
      const result = await validerRessource(id);
      if (!result) {
        // La ressource a été supprimée (validation d'une SUPPRESSION)
        navigate('/ressources');
        return;
      }
      setRessource(result);
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
      const result = await rejeterRessource(id, motif);
      setRessource(result);
    } catch (err) {
      alert(err?.response?.data?.message || 'Erreur lors du rejet');
    } finally {
      setActionLoading(false);
    }
  };

  if (loading && !ressource) {
    return <div className="loading">Chargement...</div>;
  }

  if (error) {
    return <div className="error-banner">❌ {error}</div>;
  }

  if (!ressource) return null;

  const validationStatus = ressource.validationStatus;
  const isPendingNiveau1 = validationStatus === 'EN_ATTENTE_CHEF_CHANTIER';
  const isPendingNiveau2 = validationStatus === 'EN_ATTENTE_CHEF_PROJET';
  const isPending = isPendingNiveau1 || isPendingNiveau2;

  const canValiderOuRejeter =
    (isPendingNiveau1 && (role === 'CHEF_CHANTIER' || role === 'ADMIN')) ||
    (isPendingNiveau2 && (role === 'CHEF_PROJET' || role === 'ADMIN'));

  return (
    <div className="chantiers-page">
      <div className="page-header">
        <div>
          <h1>📦 {ressource.nom}</h1>
          <span className="badge-specialite">{TYPE_LABELS[ressource.type] || ressource.type}</span>
        </div>
        <div className="chantier-footer" style={{ borderTop: 'none', paddingTop: 0 }}>
          <Link to={`/ressources/${id}/modifier`} className="btn-edit">Modifier</Link>
          <button onClick={handleDelete} className="btn-delete">Supprimer</button>
          <Link to="/ressources" className="btn-view">Retour</Link>
        </div>
      </div>

      {isPending && (
        <div className="info-banner">
          ⏳ {VALIDATION_LABELS[validationStatus]} — action en attente :{' '}
          <strong>{ACTION_LABELS[ressource.pendingAction] || ressource.pendingAction}</strong>
        </div>
      )}

      {validationStatus === 'REJETEE' && ressource.rejectionReason && (
        <div className="error-banner">
          ⚠️ Demande rejetée définitivement. Motif : {ressource.rejectionReason}
        </div>
      )}

      {canValiderOuRejeter && (
        <div className="status-actions" style={{ marginBottom: 16 }}>
          <button type="button" className="btn-primary" disabled={actionLoading} onClick={handleValider}>
            ✔ Valider
          </button>
          <button type="button" className="btn-danger" disabled={actionLoading} onClick={handleRejeter}>
            ✘ Rejeter
          </button>
        </div>
      )}

      <div className="chantier-card" style={{ maxWidth: 560 }}>
        <p><strong>Statut:</strong> {STATUT_LABELS[ressource.statut] || ressource.statut}</p>
        <p><strong>Quantité:</strong> {ressource.quantite ?? '—'} {ressource.unite || ''}</p>
        <p><strong>Site:</strong> {ressource.siteName || 'N/A'}</p>
        <p><strong>Ajoutée le:</strong> {formatDate(ressource.dateAjout)}</p>

        {ressource.description && (
          <>
            <hr style={{ border: 'none', borderTop: '1px solid #f3f4f6', margin: '8px 0' }} />
            <p style={{ marginBottom: 4 }}><strong>Description:</strong></p>
            <p style={{ whiteSpace: 'pre-wrap', color: '#4b5563' }}>{ressource.description}</p>
          </>
        )}
      </div>
    </div>
  );
};

export default RessourceDetailPage;