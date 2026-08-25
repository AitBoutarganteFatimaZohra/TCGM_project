import { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import useChantiers from '../hooks/useChantiers';
import { getClients } from '../api/clientApi';
import { getUsersByRole } from '../api/userApi';

const userLabel = (u, roleLabel) => `${u.firstName} ${u.lastName} (${roleLabel})`;

const ChantiersPage = () => {
  const { chantiers, loading, error, fetchChantiers, removeChantier } = useChantiers();
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [clientFilter, setClientFilter] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [responsableFilter, setResponsableFilter] = useState('');
  const [hasLoadedOnce, setHasLoadedOnce] = useState(false);
  const debounceRef = useRef(null);

  const [clients, setClients] = useState([]);
  const [responsables, setResponsables] = useState([]);
  const [loadingFilters, setLoadingFilters] = useState(true);

  // Charger les listes des filtres (clients + tous les responsables possibles)
  useEffect(() => {
    const loadFilterOptions = async () => {
      setLoadingFilters(true);
      try {
        const [clientsRes, chefsProjet, chefsChantier, magasiniers, agentsSaisie] = await Promise.all([
          getClients({ size: 200 }),
          getUsersByRole('CHEF_PROJET'),
          getUsersByRole('CHEF_CHANTIER'),
          getUsersByRole('MAGASINIER'),
          getUsersByRole('AGENT_SAISIE'),
        ]);
        setClients(clientsRes?.content ?? clientsRes ?? []);
        setResponsables([
          ...(chefsProjet ?? []).map((u) => ({ ...u, roleLabel: 'Chef de Projet' })),
          ...(chefsChantier ?? []).map((u) => ({ ...u, roleLabel: 'Chef de Chantier' })),
          ...(magasiniers ?? []).map((u) => ({ ...u, roleLabel: 'Magasinier' })),
          ...(agentsSaisie ?? []).map((u) => ({ ...u, roleLabel: 'Agent de Saisie' })),
        ]);
      } catch (err) {
        // Si l'utilisateur n'a pas les droits (ex. pas ADMIN/CHEF_PROJET), on
        // laisse simplement les filtres client/responsable vides sans bloquer la page.
      } finally {
        setLoadingFilters(false);
      }
    };
    loadFilterOptions();
  }, []);

  useEffect(() => {
    // Debounce : on attend que l'utilisateur arrête de taper avant d'appeler l'API
    clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      fetchChantiers({
        search,
        status: statusFilter || undefined,
        clientId: clientFilter || undefined,
        startDate: startDate || undefined,
        endDate: endDate || undefined,
        responsableId: responsableFilter || undefined,
      }).finally(() => setHasLoadedOnce(true));
    }, 400);

    return () => clearTimeout(debounceRef.current);
  }, [search, statusFilter, clientFilter, startDate, endDate, responsableFilter]);

  const resetFilters = () => {
    setSearch('');
    setStatusFilter('');
    setClientFilter('');
    setStartDate('');
    setEndDate('');
    setResponsableFilter('');
  };

  const handleDelete = async (id) => {
    if (window.confirm('Êtes-vous sûr de vouloir supprimer ce chantier ?')) {
      try {
        await removeChantier(id);
      } catch (err) {
        alert('Erreur lors de la suppression');
      }
    }
  };

  // Loading plein écran uniquement au tout premier chargement
  if (loading && !hasLoadedOnce) {
    return <div className="loading">Chargement des chantiers...</div>;
  }

  return (
    <div className="chantiers-page">
      <div className="page-header">
        <h1>🏗️ Chantiers</h1>
        <Link to="/chantiers/create" className="btn-primary">
          + Nouveau chantier
        </Link>
      </div>

      <div className="filters">
        <input
          type="text"
          placeholder="🔍 Rechercher un chantier..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="search-input"
        />
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="filter-select"
        >
          <option value="">Tous les statuts</option>
          <option value="PLANIFIE">Planifié</option>
          <option value="EN_COURS">En cours</option>
          <option value="TERMINE">Terminé</option>
          <option value="SUSPENDU">Suspendu</option>
        </select>

        <select
          value={clientFilter}
          onChange={(e) => setClientFilter(e.target.value)}
          className="filter-select"
          disabled={loadingFilters}
        >
          <option value="">{loadingFilters ? 'Chargement...' : 'Tous les clients'}</option>
          {clients.map((c) => (
            <option key={c.id} value={c.id}>{c.name}</option>
          ))}
        </select>

        <select
          value={responsableFilter}
          onChange={(e) => setResponsableFilter(e.target.value)}
          className="filter-select"
          disabled={loadingFilters}
        >
          <option value="">{loadingFilters ? 'Chargement...' : 'Tous les responsables'}</option>
          {responsables.map((u) => (
            <option key={`${u.roleLabel}-${u.id}`} value={u.id}>{userLabel(u, u.roleLabel)}</option>
          ))}
        </select>

        <input
          type="date"
          className="filter-select"
          value={startDate}
          onChange={(e) => setStartDate(e.target.value)}
          title="Actifs à partir du"
        />
        <input
          type="date"
          className="filter-select"
          value={endDate}
          onChange={(e) => setEndDate(e.target.value)}
          title="Actifs jusqu'au"
        />

        <button className="btn-ghost" onClick={resetFilters}>Réinitialiser</button>
      </div>

      {error && <div className="error-banner">❌ {error}</div>}

      {chantiers.length === 0 ? (
        <div className="empty-state">
          <p>Aucun chantier trouvé</p>
          <Link to="/chantiers/create" className="btn-primary">
            Créer le premier chantier
          </Link>
        </div>
      ) : (
        <div className="chantiers-grid">
          {chantiers.map((chantier) => (
            <div key={chantier.id} className="chantier-card">
              <div className="chantier-header">
                <h3>{chantier.name}</h3>
                <span className={`status-badge status-${chantier.status?.toLowerCase()}`}>
                  {chantier.status}
                </span>
              </div>
              <div className="chantier-body">
                <p><strong>Référence:</strong> {chantier.reference || 'N/A'}</p>
                <p><strong>Client:</strong> {chantier.client?.name || 'N/A'}</p>
                <p><strong>Chef de projet:</strong> {chantier.chefProjet?.firstName} {chantier.chefProjet?.lastName}</p>
                <p><strong>Tâches:</strong> {chantier.totalTaches || 0}</p>
                <p><strong>Ouvriers:</strong> {chantier.totalOuvriers || 0}</p>
              </div>
              <div className="chantier-footer">
                <Link to={`/chantiers/${chantier.id}`} className="btn-view">
                  Voir
                </Link>
                <Link to={`/chantiers/edit/${chantier.id}`} className="btn-edit">
                  Modifier
                </Link>
                <button onClick={() => handleDelete(chantier.id)} className="btn-delete">
                  Supprimer
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default ChantiersPage;