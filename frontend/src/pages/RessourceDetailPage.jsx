import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import useRessources from '../hooks/useRessources';
import {
  proposerStatutRessource,
  validerStatutRessource,
  rejeterStatutRessource,
} from '../api/ressourceApi';

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

const ROLES_VALIDATION = ['ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER'];
const ROLES_PROPOSITION = ['ADMIN', 'MAGASINIER'];

const formatDate = (isoString) =>
  isoString ? new Date(isoString).toLocaleDateString('fr-FR') : 'N/A';

const RessourceDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { fetchRessourceById, removeRessource, loading } = useRessources();

  const [ressource, setRessource] = useState(null);
  const [error, setError] = useState(null);

  const [proposedStatut, setProposedStatut] = useState('');
  const [proposing, setProposing] = useState(false);

  const [showValidation, setShowValidation] = useState(null); // 'valider' | 'rejeter' | null
  const [motif, setMotif] = useState('');
  const [validating, setValidating] = useState(false);

  const canPropose = ROLES_PROPOSITION.includes(user?.role);
  const canValidate = ROLES_VALIDATION.includes(user?.role);

  const load = () => fetchRessourceById(id).then(setRessource);

  useEffect(() => {
    load().catch(() => setError("Impossible de charger cette ressource."));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const handleDelete = async () => {
    if (window.confirm('Supprimer cette ressource ?')) {
      await removeRessource(id);
      navigate('/ressources');
    }
  };

  const handleProposer = async (e) => {
    e.preventDefault();
    if (!proposedStatut) return;
    setProposing(true);
    try {
      await proposerStatutRessource(id, proposedStatut);
      setProposedStatut('');
      await load();
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la proposition.');
    } finally {
      setProposing(false);
    }
  };

  const handleValidation = async () => {
    setValidating(true);
    try {
      if (showValidation === 'valider') {
        await validerStatutRessource(id);
      } else {
        await rejeterStatutRessource(id, motif || null);
      }
      setShowValidation(null);
      setMotif('');
      await load();
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la validation.');
    } finally {
      setValidating(false);
    }
  };

  if (loading && !ressource) {
    return <div className="loading">Chargement...</div>;
  }

  if (error && !ressource) {
    return <div className="error-banner">❌ {error}</div>;
  }

  if (!ressource) return null;

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

      {error && <div className="error-banner">❌ {error}</div>}

      <div className="chantier-card" style={{ maxWidth: 560, marginBottom: 20 }}>
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

      {/* Statut proposé en attente */}
      {ressource.pendingStatut && (
        <div className="error-banner" style={{ background: '#e0f2fe', color: '#0369a1', borderColor: '#bae6fd', marginBottom: 20 }}>
          Changement de statut proposé : <strong>{STATUT_LABELS[ressource.statut]}</strong> → <strong>{STATUT_LABELS[ressource.pendingStatut]}</strong>, en attente de validation.
        </div>
      )}

      {/* Dernier rejet */}
      {!ressource.pendingStatut && ressource.motifRejet && (
        <div className="error-banner" style={{ marginBottom: 20 }}>
          ❌ La dernière proposition de changement de statut a été rejetée.{ressource.motifRejet ? ` Motif : ${ressource.motifRejet}` : ''}
        </div>
      )}

      {/* Panneau Magasinier : proposer un changement */}
      {canPropose && !ressource.pendingStatut && (
        <form className="chantier-form" onSubmit={handleProposer} style={{ maxWidth: 560, marginBottom: 20 }}>
          <div className="form-group">
            <label>Proposer un changement de statut</label>
            <select value={proposedStatut} onChange={(e) => setProposedStatut(e.target.value)}>
              <option value="">Sélectionner un statut</option>
              {Object.entries(STATUT_LABELS)
                .filter(([val]) => val !== ressource.statut)
                .map(([val, label]) => (
                  <option key={val} value={val}>{label}</option>
                ))}
            </select>
          </div>
          <div className="form-actions">
            <button type="submit" className="btn-primary" disabled={proposing || !proposedStatut}>
              {proposing ? 'Envoi...' : 'Soumettre pour validation'}
            </button>
          </div>
        </form>
      )}

      {/* Panneau Chef de Chantier / Chef de Projet : valider ou rejeter */}
      {canValidate && ressource.pendingStatut && (
        <div className="chantier-card" style={{ maxWidth: 560, marginBottom: 20 }}>
          {showValidation ? (
            <div className="form-group">
              <label>{showValidation === 'valider' ? 'Confirmation' : 'Motif du rejet'}</label>
              <textarea
                value={motif}
                onChange={(e) => setMotif(e.target.value)}
                placeholder={showValidation === 'valider' ? 'Commentaire éventuel...' : 'Expliquez le motif du rejet...'}
              />
              <div className="form-actions">
                <button className="btn-primary" onClick={handleValidation} disabled={validating}>
                  {validating ? 'Traitement...' : `Confirmer ${showValidation === 'valider' ? 'la validation' : 'le rejet'}`}
                </button>
                <button className="btn-ghost" onClick={() => { setShowValidation(null); setMotif(''); }}>
                  Annuler
                </button>
              </div>
            </div>
          ) : (
            <div className="chantier-footer" style={{ borderTop: 'none', paddingTop: 0 }}>
              <button className="btn-primary" onClick={() => setShowValidation('valider')}>
                ✓ Valider le changement
              </button>
              <button className="btn-delete" onClick={() => setShowValidation('rejeter')}>
                ✕ Rejeter le changement
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default RessourceDetailPage;