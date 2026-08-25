import { useState, useEffect, useCallback } from 'react';
import { FileDown, FileSpreadsheet, ScrollText } from 'lucide-react';
import useJournal from '../hooks/useJournal';
import useNotification from '../hooks/useNotification';

const ACTION_TYPES = ['CREATION', 'MODIFICATION', 'SUPPRESSION', 'SOUMISSION', 'VALIDATION', 'REJET'];

const ACTION_STYLES = {
  CREATION: { background: '#dcfce7', color: '#15803d' },
  MODIFICATION: { background: '#dbeafe', color: '#1d4ed8' },
  SUPPRESSION: { background: '#fef2f2', color: '#dc2626' },
  SOUMISSION: { background: '#fef3c7', color: '#b45309' },
  VALIDATION: { background: '#dcfce7', color: '#15803d' },
  REJET: { background: '#fef2f2', color: '#dc2626' },
};

const STATUT_LABELS = { EN_ATTENTE: 'En attente', VALIDE: 'Validé', REJETE: 'Rejeté' };
const STATUT_STYLES = {
  EN_ATTENTE: { background: '#fef3c7', color: '#b45309' },
  VALIDE: { background: '#dcfce7', color: '#15803d' },
  REJETE: { background: '#fef2f2', color: '#dc2626' },
};

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

const MonJournalAgentPage = () => {
  const { journal, pagination, loading, error, fetchJournal, exportJournal } = useJournal();
  const { showNotification } = useNotification();

  const [search, setSearch] = useState('');
  const [actionType, setActionType] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [page, setPage] = useState(0);
  const [exporting, setExporting] = useState(false);

  // Pas de siteId ici : le backend restreint déjà le résultat au périmètre
  // de l'Agent de Saisie connecté (lui-même + chef de chantier/projet de
  // son site), via computeAllowedUserIds() côté JournalServiceImpl.
  const buildParams = useCallback(
    () => ({
      page,
      size: 10,
      ...(search && { search }),
      ...(actionType && { actionType }),
      ...(startDate && { startDate }),
      ...(endDate && { endDate }),
    }),
    [page, search, actionType, startDate, endDate]
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

  const handleExport = async (format) => {
    setExporting(true);
    try {
      await exportJournal(format, {
        ...(startDate && { startDate }),
        ...(endDate && { endDate }),
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
          <ScrollText size={20} strokeWidth={1.8} style={{ verticalAlign: -3, marginRight: 6 }} />
          Mon journal
          <span className="counter-badge">{pagination.totalElements}</span>
        </h1>
        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', alignItems: 'center' }}>
          <button className="btn-ghost" onClick={() => handleExport('pdf')} disabled={exporting}>
            <FileDown size={16} style={{ marginRight: 4 }} />
            Export PDF
          </button>
          <button className="btn-ghost" onClick={() => handleExport('excel')} disabled={exporting}>
            <FileSpreadsheet size={16} style={{ marginRight: 4 }} />
            Export Excel
          </button>
        </div>
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

        <input className="filter-select" type="date" value={startDate} onChange={(e) => { setPage(0); setStartDate(e.target.value); }} />
        <input className="filter-select" type="date" value={endDate} onChange={(e) => { setPage(0); setEndDate(e.target.value); }} />

        <button className="btn-ghost" onClick={resetFilters}>Réinitialiser</button>
      </div>

      {loading ? (
        <div className="loading">Chargement du journal...</div>
      ) : journal.length === 0 ? (
        <div className="empty-state"><p>Aucune opération enregistrée.</p></div>
      ) : (
        <div className="table-container">
          <table className="ouvriers-table">
            <thead>
              <tr>
                <th>Date</th><th>Utilisateur</th><th>Action</th><th>Élément concerné</th>
                <th>Détails</th><th>Statut</th>
              </tr>
            </thead>
            <tbody>
              {journal.map((entry) => (
                <tr key={entry.id}>
                  <td className="cell-mono">{formatDate(entry.createdAt)}</td>
                  <td>{entry.user ? `${entry.user.firstName} ${entry.user.lastName}` : 'Système'}</td>
                  <td><ActionBadge actionType={entry.actionType} /></td>
                  <td><span className="badge-specialite">{entry.entityType}{entry.entityId ? ` #${entry.entityId}` : ''}</span></td>
                  <td>{entry.details || '-'}</td>
                  <td><StatutBadge status={entry.status} /></td>
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

export default MonJournalAgentPage;