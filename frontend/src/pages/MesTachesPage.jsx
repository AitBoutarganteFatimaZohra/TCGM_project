import { useEffect, useState } from 'react';
import useChantiers from '../hooks/useChantiers';
import useMySites from '../hooks/useMySites';

// Consultation en lecture seule des tâches du site du Magasinier — utile
// pour anticiper les besoins en matériel. Réutilise les tâches déjà
// embarquées dans la réponse détaillée du site (pas de nouvel endpoint).
const MesTachesPage = () => {
  const { sites, loading: loadingSites } = useMySites();
  const { fetchChantierById } = useChantiers();

  const [selectedSiteId, setSelectedSiteId] = useState(null);
  const [chantier, setChantier] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (sites.length > 0 && !selectedSiteId) setSelectedSiteId(sites[0].id);
  }, [sites]);

  useEffect(() => {
    if (!selectedSiteId) return;
    setLoading(true);
    fetchChantierById(selectedSiteId)
      .then(setChantier)
      .finally(() => setLoading(false));
  }, [selectedSiteId]);

  if (loadingSites) return <div className="loading">Chargement...</div>;

  if (sites.length === 0) {
    return (
      <div className="empty-state">
        <p>Vous n'êtes affecté à aucun chantier pour le moment.</p>
      </div>
    );
  }

  return (
    <div className="chantiers-page">
      <div className="page-header">
        <h1>✅ Tâches du site</h1>
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
      </div>

      {loading || !chantier ? (
        <div className="loading">Chargement des tâches...</div>
      ) : !chantier.taches || chantier.taches.length === 0 ? (
        <div className="empty-state">
          <p>Aucune tâche enregistrée pour ce site.</p>
        </div>
      ) : (
        <div className="table-container">
          <table className="ouvriers-table">
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
                  <td>{t.title}</td>
                  <td>{t.status}</td>
                  <td>{t.priority ?? '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default MesTachesPage;