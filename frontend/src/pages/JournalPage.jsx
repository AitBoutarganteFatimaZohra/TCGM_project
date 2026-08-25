import { useState, useEffect, useCallback } from 'react';
import { Check, X, FileDown, FileSpreadsheet } from 'lucide-react';
import useJournal from '../hooks/useJournal';
import useNotification from '../hooks/useNotification';
import useAuth from '../hooks/useAuth';

const ACTION_TYPES = ['CREATION', 'MODIFICATION', 'SUPPRESSION', 'VALIDATION', 'CONNEXION', 'DECONNEXION'];
const ENTITY_TYPES = ['SITE', 'TACHE', 'OUVRIER', 'CLIENT', 'AFFECTATION', 'POINTAGE', 'UTILISATEUR'];

const ACTION_STYLES = {
  CREATION: { background: '#dcfce7', color: '#15803d' },
  VALIDATION: { background: '#dcfce7', color: '#15803d' },
  MODIFICATION: { background: '#dbeafe', color: '#1d4ed8' },
  SUPPRESSION: { background: '#fef2f2', color: '#dc2626' },
  CONNEXION: { background: '#fdf6f2', color: '#c94d25' },
  DECONNEXION: { background: '#f0ece7', color: '#8b8580' },
};

const STATUT_LABELS = { EN_ATTENTE: 'En attente', VALIDE: 'Validé', REJETE: 'Rejeté' };
const STATUT_STYLES = {
  EN_ATTENTE: { background: '#fef3c7', color: '#b45309' },
  VALIDE: { background: '#dcfce7', color: '#15803d' },
  REJETE: { background: '#fef2f2', color: '#dc2626' },
};

// 🔧 CORRIGÉ : CHEF_CHANTIER ajouté aux deux (voir accessConfig.js et
// JournalController.java pour la cohérence back/front).
const CAN_VALIDATE_ROLES = ['ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER'];
const CAN_EXPORT_ROLES = ['ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER'];

const ActionBadge = ({ actionType }) => (
  <span className="badge" style={ACTION_STYLES[actionType] || { background: '#f0ece7', color: '#8b8580' }}>
    {actionType}
  </span>
);

const StatutBadge = ({ status }) => (
  <span className="badge" style={STATUT_STYLES[status] || { background: '#f0ece7', color: '#8b8580' }}>
    {STATUT_LABELS[status] || status || 'En attente'}
  </span>
);

const JournalPage = () => {
  const { journal, pagination, loading, error, fetchJournal, validateEntry, rejectEntry, exportJournal } = useJournal();
  const { showNotification } = useNotification();
  const { user } = useAuth();

  const canValidate = CAN_VALIDATE_ROLES.includes(user?.role);
  const canExport = CAN_EXPORT_ROLES.includes(user?.role);

  const [search, setSearch] = useState('');
  const [actionType, setActionType] = useState('');
  const [entityType, setEntityType] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [page, setPage] = useState(0);
  const [processingId, setProcessingId] = useState(null);
  const [exporting, setExporting] = useState(false);

  const buildParams = useCallback(
    () => ({
      page,
      size: 10,
      ...(search && { search }),
      ...(actionType && { actionType }),
      ...(entityType && { entityType }),
      ...(startDate && { startDate }),
      ...(endDate && { endDate }),
    }),
    [page, search, actionType, entityType, startDate, endDate]
  );

  useEffect(() => {
    const timeout = setTimeout(() => {
      fetchJournal(buildParams()).catch(() =>
        showNotification('Erreur lors du chargement du journal', 'error')
      );
    }, 350);
    return () => clearTimeout(timeout);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [buildParams]);

  const resetFilters = () => {
    setSearch('');
    setActionType('');
    setEntityType('');
    setStartDate('');
    setEndDate('');
    setPage(0);
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleString('fr-FR', {
      day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit',
    });
  };

  const handleValidate = async (id) => {
    setProcessingId(id);
    try {
      await validateEntry(id);
      showNotification('Entrée validée', 'success');
    } catch {
      showNotification('Erreur lors de la validation', 'error');
    } finally {
      setProcessingId(null);
    }
  };

  const handleReject = async (id) => {
    setProcessingId(id);
    try {
      await rejectEntry(id);
      showNotification('Entrée rejetée', 'success');
    } catch {
      showNotification('Erreur lors du rejet', 'error');
    } finally {
      setProcessingId(null);
    }
  };

  const handleExport = async (format) => {
    setExporting(true);
    try {
      await exportJournal(format, {
        ...(startDate && { startDate }),
        ...(endDate && { endDate }),
        ...(entityType && { entityType }),
      });
    } catch {
      showNotification("Erreur lors de l'export du journal", 'error');
    } finally {
      setExporting(false);
    }
  };

  return (
    <div className="journal-page">
      <div className="page-header">
        <h1>
          Journal de traçabilité
          <span className="counter-badge">{pagination.totalElements}</span>
        </h1>
        {canExport && (
          <div style={{ display: 'flex', gap: 8 }}>
            <button className="btn-ghost" onClick={() => handleExport('pdf')} disabled={exporting}>
              <FileDown size={16} style={{ marginRight: 4 }} />
              Export PDF
            </button>
            <button className="btn-ghost" onClick={() => handleExport('excel')} disabled={exporting}>
              <FileSpreadsheet size={16} style={{ marginRight: 4 }} />
              Export Excel
            </button>
          </div>
        )}
      </div>

      {error && <div className="error-banner">{error}</div>}

      <div className="filters">
        <div className="search-wrapper">
          <span className="search-icon">🔍</span>
          <input
            className="search-input"
            type="text"
            placeholder="Rechercher dans les détails..."
            value={search}
            onChange={(e) => { setPage(0); setSearch(e.target.value); }}
          />
        </div>

        <select className="filter-select" value={actionType} onChange={(e) => { setPage(0); setActionType(e.target.value); }}>
          <option value="">Toutes les actions</option>
          {ACTION_TYPES.map((type) => <option key={type} value={type}>{type}</option>)}
        </select>

        <select className="filter-select" value={entityType} onChange={(e) => { setPage(0); setEntityType(e.target.value); }}>
          <option value="">Toutes les entités</option>
          {ENTITY_TYPES.map((type) => <option key={type} value={type}>{type}</option>)}
        </select>

        <input className="filter-select" type="date" value={startDate} onChange={(e) => { setPage(0); setStartDate(e.target.value); }} />
        <input className="filter-select" type="date" value={endDate} onChange={(e) => { setPage(0); setEndDate(e.target.value); }} />

        <button className="btn-ghost" onClick={resetFilters}>Réinitialiser</button>
      </div>

      {loading ? (
        <div className="loading">Chargement du journal...</div>
      ) : journal.length === 0 ? (
        <div className="empty-state"><p>Aucune opération trouvée pour ces filtres.</p></div>
      ) : (
        <div className="table-container">
          <table className="ouvriers-table">
            <thead>
              <tr>
                <th>Date</th><th>Utilisateur</th><th>Action</th><th>Entité</th>
                <th>Détails</th><th>Statut</th>
                {canValidate && <th>Validation</th>}
              </tr>
            </thead>
            <tbody>
              {journal.map((entry) => (
                <tr key={entry.id}>
                  <td className="cell-mono">{formatDate(entry.createdAt)}</td>
                  <td>{entry.user ? `${entry.user.firstName} ${entry.user.lastName}` : <span style={{ color: '#8b8580' }}>Système</span>}</td>
                  <td><ActionBadge actionType={entry.actionType} /></td>
                  <td><span className="badge-specialite">{entry.entityType}{entry.entityId ? ` #${entry.entityId}` : ''}</span></td>
                  <td>{entry.details || '-'}</td>
                  <td><StatutBadge status={entry.status} /></td>
                  {canValidate && (
                    <td>
                      {(!entry.status || entry.status === 'EN_ATTENTE') ? (
                        <div style={{ display: 'flex', gap: 6 }}>
                          <button type="button" className="btn-ghost" title="Valider" disabled={processingId === entry.id}
                            onClick={() => handleValidate(entry.id)} style={{ color: '#15803d', padding: '4px 8px' }}>
                            <Check size={16} />
                          </button>
                          <button type="button" className="btn-ghost" title="Rejeter" disabled={processingId === entry.id}
                            onClick={() => handleReject(entry.id)} style={{ color: '#dc2626', padding: '4px 8px' }}>
                            <X size={16} />
                          </button>
                        </div>
                      ) : (
                        <span style={{ color: '#8b8580', fontSize: 13 }}>
                          {entry.validatedBy ? `par ${entry.validatedBy.firstName} ${entry.validatedBy.lastName}` : '—'}
                        </span>
                      )}
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>

          <div className="pagination">
            <span className="pagination-info">
              Page <strong>{pagination.number + 1}</strong> sur <strong>{pagination.totalPages || 1}</strong>
              {' — '}<strong>{pagination.totalElements}</strong> opérations
            </span>
            <div className="pagination-controls">
              <button className={`pagination-btn ${page === 0 ? 'pagination-btn--disabled' : ''}`} onClick={() => setPage((p) => Math.max(0, p - 1))} disabled={page === 0}>←</button>
              <button className={`pagination-btn ${page + 1 >= pagination.totalPages ? 'pagination-btn--disabled' : ''}`} onClick={() => setPage((p) => p + 1)} disabled={page + 1 >= pagination.totalPages}>→</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default JournalPage;