import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import useChantiers from '../hooks/useChantiers';
import ChantierMap from '../Components/ChantierMap';
import { getSiteStats } from '../api/statistiqueApi';
import {
  validerModificationSite,
  rejeterModificationSite,
} from '../api/chantierApi';

const STATUS_LABELS = {
  PLANIFIE: 'Planifié',
  EN_COURS: 'En cours',
  TERMINE: 'Terminé',
  SUSPENDU: 'Suspendu',
};

const formatDate = (isoString) =>
  isoString
    ? new Date(isoString).toLocaleDateString('fr-FR')
    : 'N/A';

const formatUser = (user) =>
  user
    ? `${user.firstName || ''} ${user.lastName || ''}`.trim() || 'N/A'
    : 'N/A';

const ChantierDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';

  const {
    fetchChantierById,
    removeChantier,
    loading,
  } = useChantiers();

  const [chantier, setChantier] = useState(null);
  const [error, setError] = useState(null);

  const [stats, setStats] = useState(null);
  const [statsError, setStatsError] = useState(null);

  const [showValidation, setShowValidation] = useState(null);
  const [motif, setMotif] = useState('');
  const [validating, setValidating] = useState(false);

  const load = () =>
    fetchChantierById(id)
      .then(setChantier)
      .catch(() => setError('Impossible de charger ce chantier.'));

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
      .catch(() =>
        setStatsError("Impossible de charger l'avancement.")
      );
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
      setError(
        err.response?.data?.message ||
          'Erreur lors de la validation.'
      );
    } finally {
      setValidating(false);
    }
  };

  if (loading && !chantier) {
    return (
      <div className="loading">
        Chargement du chantier...
      </div>
    );
  }

  if (error && !chantier) {
    return (
      <div className="error-banner">
        ❌ {error}
      </div>
    );
  }

  if (!chantier) {
    return null;
  }

  const tauxAvancement = stats?.tauxAvancement ?? 0;
  const tachesTerminees = stats?.tachesTerminees ?? 0;

  const totalTachesStats =
    stats?.totalTaches ??
    chantier.totalTaches ??
    0;

  const hasPendingModification =
    !!chantier.pendingStatus ||
    !!chantier.pendingStartDate ||
    !!chantier.pendingEndDate;

  const statusClass = (
    chantier.status || ''
  ).toLowerCase();

  return (
    <div className="chantiers-page chantier-detail-page">

      {/* =====================================================
          HEADER
          ===================================================== */}
      <div className="detail-header">

        <div className="detail-header__left">

          <div className="detail-title-icon">
            🏗️
          </div>

          <div>
            <div className="detail-reference">
              {chantier.reference || 'CHANTIER'}
            </div>

            <h1>
              {chantier.name}
            </h1>

            <div className="detail-status-row">
              <span
                className={`status-badge status-${statusClass}`}
              >
                {STATUS_LABELS[chantier.status] ||
                  chantier.status}
              </span>

              {chantier.address && (
                <span className="detail-location">
                  📍 {chantier.address}
                </span>
              )}
            </div>
          </div>

        </div>

        <div className="detail-header__actions">

          <Link
            to={`/chantiers/edit/${id}`}
            className="detail-btn detail-btn--edit"
          >
            ✏️ Modifier
          </Link>

          <button
            onClick={handleDelete}
            className="detail-btn detail-btn--delete"
          >
            🗑️ Supprimer
          </button>

          <Link
            to="/chantiers"
            className="detail-btn detail-btn--back"
          >
            ← Retour
          </Link>

        </div>

      </div>

      {/* =====================================================
          ERREUR
          ===================================================== */}
      {error && (
        <div className="detail-alert detail-alert--error">
          <span className="detail-alert__icon">✕</span>

          <div>
            <strong>Une erreur est survenue</strong>
            <p>{error}</p>
          </div>
        </div>
      )}

      {/* =====================================================
          MODIFICATION EN ATTENTE
          ===================================================== */}
      {hasPendingModification && (
        <div className="detail-alert detail-alert--pending">

          <div className="detail-alert__icon">
            ⏳
          </div>

          <div className="detail-alert__content">

            <strong>
              Modification en attente de validation
            </strong>

            <p>
              Une modification majeure de ce chantier
              attend actuellement la validation de
              l'administrateur.
            </p>

            <div className="pending-changes">

              {chantier.pendingStatus && (
                <span>
                  Statut :
                  <strong>
                    {STATUS_LABELS[chantier.pendingStatus] ||
                      chantier.pendingStatus}
                  </strong>
                </span>
              )}

              {chantier.pendingStartDate && (
                <span>
                  Début :
                  <strong>
                    {formatDate(
                      chantier.pendingStartDate
                    )}
                  </strong>
                </span>
              )}

              {chantier.pendingEndDate && (
                <span>
                  Fin :
                  <strong>
                    {formatDate(
                      chantier.pendingEndDate
                    )}
                  </strong>
                </span>
              )}

            </div>

          </div>

        </div>
      )}

      {/* =====================================================
          DERNIER REJET
          ===================================================== */}
      {!hasPendingModification &&
        chantier.motifRejet && (
          <div className="detail-alert detail-alert--rejected">

            <div className="detail-alert__icon">
              ✕
            </div>

            <div>
              <strong>
                Dernière modification rejetée
              </strong>

              <p>
                Motif : {chantier.motifRejet}
              </p>
            </div>

          </div>
        )}

      {/* =====================================================
          PANNEAU ADMIN
          ===================================================== */}
      {isAdmin && hasPendingModification && (
        <div className="admin-validation-card">

          <div className="admin-validation-card__header">
            <div className="admin-validation-card__icon">
              🛡️
            </div>

            <div>
              <h3>
                Validation administrateur
              </h3>

              <p>
                Une modification majeure nécessite votre
                validation.
              </p>
            </div>
          </div>

          {showValidation ? (
            <div className="validation-form">

              <div className="form-group">
                <label>
                  {showValidation === 'valider'
                    ? 'Commentaire'
                    : 'Motif du rejet'}
                </label>

                <textarea
                  value={motif}
                  onChange={(e) =>
                    setMotif(e.target.value)
                  }
                  placeholder={
                    showValidation === 'valider'
                      ? 'Commentaire éventuel...'
                      : 'Expliquez le motif du rejet...'
                  }
                />
              </div>

              <div className="form-actions">

                <button
                  className="btn-primary"
                  onClick={handleValidation}
                  disabled={validating}
                >
                  {validating
                    ? 'Traitement...'
                    : `Confirmer ${
                        showValidation === 'valider'
                          ? 'la validation'
                          : 'le rejet'
                      }`}
                </button>

                <button
                  className="btn-ghost"
                  onClick={() => {
                    setShowValidation(null);
                    setMotif('');
                  }}
                >
                  Annuler
                </button>

              </div>

            </div>
          ) : (
            <div className="admin-validation-actions">

              <button
                className="admin-btn admin-btn--approve"
                onClick={() =>
                  setShowValidation('valider')
                }
              >
                ✓ Valider la modification
              </button>

              <button
                className="admin-btn admin-btn--reject"
                onClick={() =>
                  setShowValidation('rejeter')
                }
              >
                ✕ Rejeter la modification
              </button>

            </div>
          )}

        </div>
      )}

      {/* =====================================================
          STATISTIQUES
          ===================================================== */}
      <div className="detail-kpi-grid">

        <div className="detail-kpi-card">

          <div className="detail-kpi-icon detail-kpi-icon--orange">
            ✓
          </div>

          <div>
            <div className="detail-kpi-value">
              {chantier.totalTaches ?? 0}
            </div>

            <div className="detail-kpi-label">
              Tâches
            </div>
          </div>

        </div>

        <div className="detail-kpi-card">

          <div className="detail-kpi-icon detail-kpi-icon--blue">
            👷
          </div>

          <div>
            <div className="detail-kpi-value">
              {chantier.totalOuvriers ?? 0}
            </div>

            <div className="detail-kpi-label">
              Ouvriers
            </div>
          </div>

        </div>

        <div className="detail-kpi-card">

          <div className="detail-kpi-icon detail-kpi-icon--green">
            ◷
          </div>

          <div>
            <div className="detail-kpi-value">
              {chantier.totalPointages ?? 0}
            </div>

            <div className="detail-kpi-label">
              Pointages
            </div>
          </div>

        </div>

      </div>

      {/* =====================================================
          INFORMATIONS + AVANCEMENT
          ===================================================== */}
      <div className="detail-main-grid">

        {/* AVANCEMENT */}
        <div className="detail-card detail-progress-card">

          <div className="detail-card-header">

            <div>
              <span className="detail-card-eyebrow">
                SUIVI DU PROJET
              </span>

              <h2>
                Avancement du chantier
              </h2>
            </div>

            <div className="progress-percentage">
              {Math.round(tauxAvancement)}%
            </div>

          </div>

          {statsError ? (
            <div className="detail-progress-error">
              {statsError}
            </div>
          ) : (
            <>
              <div className="detail-progress-bar">

                <div
                  className={`detail-progress-fill ${
                    tauxAvancement >= 100
                      ? 'detail-progress-fill--complete'
                      : ''
                  }`}
                  style={{
                    width: `${Math.min(
                      100,
                      Math.round(tauxAvancement)
                    )}%`,
                  }}
                />

              </div>

              <div className="detail-progress-info">

                <span>
                  <strong>
                    {tachesTerminees}
                  </strong>{' '}
                  tâches terminées
                </span>

                <span>
                  sur{' '}
                  <strong>
                    {totalTachesStats}
                  </strong>
                </span>

              </div>
            </>
          )}

        </div>

        {/* INFORMATIONS */}
        <div className="detail-card">

          <div className="detail-card-header">

            <div>
              <span className="detail-card-eyebrow">
                INFORMATIONS
              </span>

              <h2>
                Détails du chantier
              </h2>
            </div>

          </div>

          <div className="detail-info-list">

            <div className="detail-info-row">
              <span className="detail-info-label">
                Référence
              </span>

              <span className="detail-info-value detail-info-value--strong">
                {chantier.reference || 'N/A'}
              </span>
            </div>

            <div className="detail-info-row">
              <span className="detail-info-label">
                Adresse
              </span>

              <span className="detail-info-value">
                {chantier.address || 'N/A'}
              </span>
            </div>

            <div className="detail-info-row">
              <span className="detail-info-label">
                Date de début
              </span>

              <span className="detail-info-value">
                {formatDate(chantier.startDate)}
              </span>
            </div>

            <div className="detail-info-row">
              <span className="detail-info-label">
                Fin prévisionnelle
              </span>

              <span className="detail-info-value">
                {formatDate(chantier.endDate)}
              </span>
            </div>

          </div>

        </div>

      </div>

      {/* =====================================================
          CLIENT ET ÉQUIPE
          ===================================================== */}
      <div className="detail-card detail-team-card">

        <div className="detail-card-header">

          <div>
            <span className="detail-card-eyebrow">
              ORGANISATION
            </span>

            <h2>
              Client et équipe
            </h2>
          </div>

        </div>

        <div className="detail-team-grid">

          <div className="detail-person-card detail-person-card--client">

            <div className="detail-person-icon">
              🏢
            </div>

            <div>
              <span className="detail-person-role">
                Client
              </span>

              <strong>
                {chantier.client?.name || 'N/A'}
              </strong>

              {chantier.client?.phone && (
                <small>
                  ☎ {chantier.client.phone}
                </small>
              )}
            </div>

          </div>

          <div className="detail-person-card">

            <div className="detail-person-icon">
              👨‍💼
            </div>

            <div>
              <span className="detail-person-role">
                Chef de projet
              </span>

              <strong>
                {formatUser(chantier.chefProjet)}
              </strong>
            </div>

          </div>

          <div className="detail-person-card">

            <div className="detail-person-icon">
              👷
            </div>

            <div>
              <span className="detail-person-role">
                Chef de chantier
              </span>

              <strong>
                {formatUser(chantier.chefChantier)}
              </strong>
            </div>

          </div>

          <div className="detail-person-card">

            <div className="detail-person-icon">
              📦
            </div>

            <div>
              <span className="detail-person-role">
                Magasinier
              </span>

              <strong>
                {formatUser(chantier.magasinier)}
              </strong>
            </div>

          </div>

          <div className="detail-person-card">

            <div className="detail-person-icon">
              📝
            </div>

            <div>
              <span className="detail-person-role">
                Agent de saisie
              </span>

              <strong>
                {formatUser(chantier.agentSaisie)}
              </strong>
            </div>

          </div>

        </div>

      </div>

      {/* =====================================================
          DESCRIPTION
          ===================================================== */}
      {chantier.description && (
        <div className="detail-card detail-description-card">

          <div className="detail-card-header">

            <div>
              <span className="detail-card-eyebrow">
                À PROPOS DU CHANTIER
              </span>

              <h2>
                Description
              </h2>
            </div>

          </div>

          <p className="detail-description">
            {chantier.description}
          </p>

        </div>
      )}

      {/* =====================================================
          CARTE
          ===================================================== */}
      <div className="detail-card detail-map-card">

        <div className="detail-card-header">

          <div>
            <span className="detail-card-eyebrow">
              LOCALISATION
            </span>

            <h2>
              Localisation du chantier
            </h2>
          </div>

        </div>

        <ChantierMap
          latitude={chantier.latitude}
          longitude={chantier.longitude}
          name={chantier.name}
        />

      </div>

      {/* =====================================================
          TÂCHES
          ===================================================== */}
      {chantier.taches?.length > 0 && (
        <div className="detail-card detail-table-card">

          <div className="detail-card-header">

            <div>
              <span className="detail-card-eyebrow">
                TRAVAUX
              </span>

              <h2>
                Tâches
              </h2>
            </div>

            <span className="detail-count-badge">
              {chantier.taches.length}
            </span>

          </div>

          <div className="detail-table-wrapper">

            <table className="detail-table">

              <thead>
                <tr>
                  <th>Titre</th>
                  <th>Statut</th>
                  <th>Priorité</th>
                </tr>
              </thead>

              <tbody>
                {chantier.taches.map((t) => (
                  <tr key={t.id}>

                    <td>
                      <strong>
                        {t.title}
                      </strong>
                    </td>

                    <td>
                      <span className="table-status">
                        {t.status}
                      </span>
                    </td>

                    <td>
                      {t.priority || '—'}
                    </td>

                  </tr>
                ))}
              </tbody>

            </table>

          </div>

        </div>
      )}

      {/* =====================================================
          OUVRIERS
          ===================================================== */}
      {chantier.ouvriers?.length > 0 && (
        <div className="detail-card detail-table-card">

          <div className="detail-card-header">

            <div>
              <span className="detail-card-eyebrow">
                RESSOURCES HUMAINES
              </span>

              <h2>
                Ouvriers affectés
              </h2>
            </div>

            <span className="detail-count-badge">
              {chantier.ouvriers.length}
            </span>

          </div>

          <div className="detail-table-wrapper">

            <table className="detail-table">

              <thead>
                <tr>
                  <th>Nom</th>
                  <th>CIN</th>
                  <th>Spécialité</th>
                </tr>
              </thead>

              <tbody>
                {chantier.ouvriers.map((o) => (
                  <tr key={o.id}>

                    <td>
                      <div className="worker-name">

                        <div className="worker-avatar">
                          {(o.firstName?.[0] || '') +
                            (o.lastName?.[0] || '')}
                        </div>

                        <strong>
                          {o.firstName} {o.lastName}
                        </strong>

                      </div>
                    </td>

                    <td className="cell-mono">
                      {o.cin || '—'}
                    </td>

                    <td>
                      {o.specialite || '—'}
                    </td>

                  </tr>
                ))}
              </tbody>

            </table>

          </div>

        </div>
      )}

    </div>
  );
};

export default ChantierDetailPage;