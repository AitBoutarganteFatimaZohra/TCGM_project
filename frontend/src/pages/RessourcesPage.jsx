import { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import useRessources from '../hooks/useRessources';
import useMySites from '../hooks/useMySites';
import {
  updateRessourceStatut,
  validerRessource,
  rejeterRessource,
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

const STATUT_BADGE_CLASS = {
  DISPONIBLE: 'badge badge--success',
  EN_UTILISATION: 'badge-site',
  HORS_SERVICE: 'badge badge--danger',
  EN_MAINTENANCE: 'badge badge--warning',
};

const ACTION_LABELS = {
  CREATION: 'création',
  MODIFICATION: 'modification',
  CHANGEMENT_STATUT: 'changement de statut',
  SUPPRESSION: 'suppression',
};

// Qui peut valider/rejeter dépend du NIVEAU (voir ci-dessous) — le backend
// vérifie de toute façon le rôle exact ; ceci ne sert qu'à afficher ou
// masquer les boutons côté UI.
const ROLES_NIVEAU1 = ['ADMIN', 'CHEF_CHANTIER'];
const ROLES_NIVEAU2 = ['ADMIN', 'CHEF_PROJET'];
const ROLES_PROPOSITION = ['ADMIN', 'MAGASINIER'];
const ROLES_CREATE = ['ADMIN', 'MAGASINIER'];
const ROLES_EDIT = ['ADMIN', 'MAGASINIER'];
const ROLES_DELETE = ['ADMIN', 'MAGASINIER'];

const RessourcesPage = () => {
  const { user } = useAuth();
  const canPropose = ROLES_PROPOSITION.includes(user?.role);
  const canCreate = ROLES_CREATE.includes(user?.role);
  const canEdit = ROLES_EDIT.includes(user?.role);
  const canDelete = ROLES_DELETE.includes(user?.role);

  const { sites, loading: loadingSites } = useMySites();
  const { ressources, loading, error, fetchRessources, removeRessource } = useRessources();

  const [selectedSiteId, setSelectedSiteId] = useState(null);
  const [search, setSearch] = useState('');
  const [typeFilter, setTypeFilter] = useState('');
  const [statutFilter, setStatutFilter] = useState('');
  const [actionError, setActionError] = useState(null);
  const [pendingRowId, setPendingRowId] = useState(null);
  const debounceRef = useRef(null);

  useEffect(() => {
    if (sites.length > 0 && !selectedSiteId) setSelectedSiteId(sites[0].id);
  }, [sites]);

  const reload = () => {
    if (!selectedSiteId) return;
    fetchRessources(selectedSiteId, {
      search: search || undefined,
      type: typeFilter || undefined,
      statut: statutFilter || undefined,
    });
  };

  useEffect(() => {
    if (!selectedSiteId) return;
    clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(reload, 400);
    return () => clearTimeout(debounceRef.current);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedSiteId, search, typeFilter, statutFilter]);

  const handleDelete = async (id) => {
    if (window.confirm('Soumettre la suppression de cette ressource pour validation ?')) {
      try {
        await removeRessource(id);
        reload();
      } catch {
        alert('Erreur lors de la suppression');
      }
    }
  };

  // "Proposer" un nouveau statut = updateRessourceStatut côté backend :
  // ça place la ressource en EN_ATTENTE_CHEF_CHANTIER (niveau 1).
  const handleProposer = async (id, statut) => {
    if (!statut) return;
    setActionError(null);
    setPendingRowId(id);
    try {
      await updateRessourceStatut(id, statut);
      reload();
    } catch (err) {
      setActionError(err.response?.data?.message || 'Erreur lors de la proposition.');
    } finally {
      setPendingRowId(null);
    }
  };

  const handleValider = async (id) => {
    setActionError(null);
    setPendingRowId(id);
    try {
      const result = await validerRessource(id);
      if (!result) {
        // La ressource a été supprimée (validation d'une action SUPPRESSION)
      }
      reload();
    } catch (err) {
      setActionError(err.response?.data?.message || 'Erreur lors de la validation.');
    } finally {
      setPendingRowId(null);
    }
  };

  const handleRejeter = async (id) => {
    const motif = window.prompt('Motif du rejet (optionnel) :') || null;
    setActionError(null);
    setPendingRowId(id);
    try {
      await rejeterRessource(id, motif);
      reload();
    } catch (err) {
      setActionError(err.response?.data?.message || 'Erreur lors du rejet.');
    } finally {
      setPendingRowId(null);
    }
  };

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
    <div className="ouvriers-page">
      <div className="page-header">
        <h1>
          📦 Ressources
          <span className="counter-badge">{ressources.length}</span>
        </h1>
        {/* ⚠️ SEULS LE MAGASINIER ET L'ADMIN PEUVENT CRÉER */}
        {canCreate && (
          <Link to="/ressources/create" className="btn-primary">
            + Nouvelle ressource
          </Link>
        )}
      </div>

      <div className="filters">
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

        <div className="search-wrapper">
          <span className="search-icon">🔍</span>
          <input
            type="text"
            placeholder="Rechercher une ressource..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="search-input"
          />
        </div>

        <select className="filter-select" value={typeFilter} onChange={(e) => setTypeFilter(e.target.value)}>
          <option value="">Tous les types</option>
          {Object.entries(TYPE_LABELS).map(([val, label]) => (
            <option key={val} value={val}>{label}</option>
          ))}
        </select>

        <select className="filter-select" value={statutFilter} onChange={(e) => setStatutFilter(e.target.value)}>
          <option value="">Tous les statuts</option>
          {Object.entries(STATUT_LABELS).map(([val, label]) => (
            <option key={val} value={val}>{label}</option>
          ))}
        </select>
      </div>

      {(error || actionError) && <div className="error-banner">❌ {error || actionError}</div>}

      {!loading && ressources.length === 0 ? (
        <div className="empty-state">
          <p>Aucune ressource trouvée</p>
          {canCreate && (
            <Link to="/ressources/create" className="btn-primary">Ajouter la première ressource</Link>
          )}
        </div>
      ) : (
        <div className="table-container">
          <table className="ouvriers-table">
            <thead>
              <tr>
                <th>Nom</th>
                <th>Type</th>
                <th>Quantité</th>
                <th>Statut</th>
                <th className="col-actions">Actions</th>
              </tr>
            </thead>
            <tbody>
              {ressources.map((r) => {
                const isPendingNiveau1 = r.validationStatus === 'EN_ATTENTE_CHEF_CHANTIER';
                const isPendingNiveau2 = r.validationStatus === 'EN_ATTENTE_CHEF_PROJET';
                const isPending = isPendingNiveau1 || isPendingNiveau2;

                const canValidateThisRow =
                  (isPendingNiveau1 && ROLES_NIVEAU1.includes(user?.role)) ||
                  (isPendingNiveau2 && ROLES_NIVEAU2.includes(user?.role));

                return (
                  <tr key={r.id}>
                    <td>{r.nom}</td>
                    <td><span className="badge-specialite">{TYPE_LABELS[r.type] || r.type}</span></td>
                    <td>{r.quantite ?? '—'} {r.unite || ''}</td>
                    <td>
                      <span className={STATUT_BADGE_CLASS[r.statut]}>{STATUT_LABELS[r.statut] || r.statut}</span>
                      {isPending && (
                        <div className="activity-meta">
                          ⏳ {ACTION_LABELS[r.pendingAction] || r.pendingAction} — en attente (
                          {isPendingNiveau1 ? 'Chef de Chantier' : 'Chef de Projet, recours'})
                        </div>
                      )}
                      {r.validationStatus === 'REJETEE' && r.rejectionReason && (
                        <div className="activity-meta">✘ Rejetée : {r.rejectionReason}</div>
                      )}
                    </td>
                    <td className="col-actions">
                      <div className="row-actions">
                        <Link to={`/ressources/${r.id}`} className="icon-btn icon-btn--view" title="Voir">👁️</Link>
                        
                        {/* ⚠️ SEULS LE MAGASINIER ET L'ADMIN PEUVENT MODIFIER */}
                        {canEdit && (
                          <Link to={`/ressources/${r.id}/modifier`} className="icon-btn icon-btn--edit" title="Modifier">✏️</Link>
                        )}

                        {canPropose && !isPending && (
                          <select
                            className="filter-select"
                            style={{ padding: '2px 6px', fontSize: 12 }}
                            value=""
                            disabled={pendingRowId === r.id}
                            onChange={(e) => handleProposer(r.id, e.target.value)}
                          >
                            <option value="">Proposer...</option>
                            {Object.entries(STATUT_LABELS)
                              .filter(([val]) => val !== r.statut)
                              .map(([val, label]) => (
                                <option key={val} value={val}>{label}</option>
                              ))}
                          </select>
                        )}

                        {canValidateThisRow && (
                          <>
                            <button
                              className="icon-btn icon-btn--success"
                              title="Valider"
                              disabled={pendingRowId === r.id}
                              onClick={() => handleValider(r.id)}
                            >
                              ✓
                            </button>
                            <button
                              className="icon-btn icon-btn--danger"
                              title="Rejeter"
                              disabled={pendingRowId === r.id}
                              onClick={() => handleRejeter(r.id)}
                            >
                              ✕
                            </button>
                          </>
                        )}

                        {/* ⚠️ SEULS LE MAGASINIER ET L'ADMIN PEUVENT SUPPRIMER */}
                        {canDelete && (
                          <button onClick={() => handleDelete(r.id)} className="icon-btn icon-btn--danger" title="Supprimer">🗑️</button>
                        )}
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default RessourcesPage;