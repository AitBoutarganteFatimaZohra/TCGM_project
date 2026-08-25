import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import useChantiers from '../hooks/useChantiers';
import ChantierMap from '../components/ChantierMap';
import { getSiteStats } from '../api/statistiqueApi';
import { validerModificationSite, rejeterModificationSite } from '../api/chantierApi';

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
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';
  const { fetchChantierById, removeChantier, loading } = useChantiers();
  const [chantier, setChantier] = useState(null);
  const [error, setError] = useState(null);

  const [stats, setStats] = useState(null);
  const [statsError, setStatsError] = useState(null);


  const [showValidation, setShowValidation] = useState(null); // 'valider' | 'rejeter' | null
  const [motif, setMotif] = useState('');
  const [validating, setValidating] = useState(false);

  const load = () =>
    fetchChantierById(id)
      .then(setChantier)
      .catch(() => setError("Impossible de charger ce chantier."));

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  useEffect(() => {
    getSiteStats(id)
      .then((data) => {
        if (data?.error) {
          setStatsError(data.error);
        } else {
          setStats(data);
        }
      })
      .catch(() => setStatsError("Impossible de charger l'avancement."));
  }, [id]);

  const handleDelete = async () => {
    if (window.confirm('Supprimer ce chantier ?')) {
      await removeChantier(id);
      navigate('/chantiers');
    }

  };

  const handleValidation = async () => {
    setValidating(true);
    try {
      if (showValidation === 'valider') {
        await validerModificationSite(id);
      } else {
        await rejeterModificationSite(id, motif || null);
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

  if (loading && !chantier) {
    return <div className="loading">Chargement du chantier...</div>;
  }

  if (error && !chantier) {
    return <div className="error-banner">❌ {error}</div>;
  }

  if (!chantier) {
    return null;
  }


  const tauxAvancement = stats?.tauxAvancement ?? 0;
  const tachesTerminees = stats?.tachesTerminees ?? 0;
  const totalTachesStats = stats?.totalTaches ?? chantier.totalTaches ?? 0;

  const hasPendingModification =
    !!chantier.pendingStatus || !!chantier.pendingStartDate || !!chantier.pendingEndDate;

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

      {error && <div className="error-banner">❌ {error}</div>}

      {/* Modification majeure en attente */}
      {hasPendingModification && (
        <div className="error-banner" style={{ background: '#e0f2fe', color: '#0369a1', borderColor: '#bae6fd', marginBottom: 20 }}>
          Modification en attente de validation par l'Administrateur :
          {chantier.pendingStatus && (
            <> Statut → <strong>{STATUS_LABELS[chantier.pendingStatus] || chantier.pendingStatus}</strong>. </>
          )}

          {chantier.pendingStartDate && <> Nouvelle date de début : <strong>{formatDate(chantier.pendingStartDate)}</strong>. </>}
          {chantier.pendingEndDate && <> Nouvelle date de fin : <strong>{formatDate(chantier.pendingEndDate)}</strong>. </>}
        </div>
      )}

      {!hasPendingModification && chantier.motifRejet && (
        <div className="error-banner" style={{ marginBottom: 20 }}>
          ❌ La dernière modification majeure proposée a été rejetée. Motif : {chantier.motifRejet}
        </div>
      )}

      {/* Panneau Administrateur : valider ou rejeter */}
      {isAdmin && hasPendingModification && (
        <div className="chantier-card" style={{ maxWidth: 620, marginBottom: 20 }}>
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
                ✓ Valider la modification
              </button>
              <button className="btn-delete" onClick={() => setShowValidation('rejeter')}>
                ✕ Rejeter la modification
              </button>
            </div>
          )}
        </div>
      )}

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
        <p style={{ marginBottom: 8 }}><strong>Avancement du chantier</strong></p>
        {statsError ? (
          <p style={{ color: '#8b8580', fontSize: 13 }}>{statsError}</p>
        ) : (

          <>
            <div
              style={{
                width: '100%',
                height: 10,
                borderRadius: 6,
                background: '#f0ece7',
                overflow: 'hidden',
                marginBottom: 6,
              }}
            >
              <div
                style={{
                  width: `${Math.min(100, Math.round(tauxAvancement))}%`,
                  height: '100%',
                  background: tauxAvancement >= 100 ? '#15803d' : '#c94d25',
                  transition: 'width 0.3s ease',
                }}
              />
            </div>
            <p style={{ fontSize: 13, color: '#4b5563' }}>
              <strong>{Math.round(tauxAvancement)}%</strong> — {tachesTerminees} / {totalTachesStats} tâches terminées
            </p>
          </>
        )}
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