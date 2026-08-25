import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import useChantiers from '../hooks/useChantiers';
import useAuth from '../hooks/useAuth';
import LocationPicker from '../components/LocationPicker';
import { getClients } from '../api/clientApi';
import { getUsersByRole } from '../api/userApi';

const initialForm = {
  name: '', reference: '', address: '', description: '',
  latitude: null, longitude: null, status: 'PLANIFIE',
  startDate: '', endDate: '', clientId: '', chefProjetId: '',
  magasinierId: '', agentSaisieId: '', chefChantierId: '',
};

const toLocalDateTime = (dateStr) => (dateStr ? `${dateStr}T00:00:00` : null);
const toNullableLong = (value) => (value ? Number(value) : null);
const userLabel = (u) => `${u.firstName} ${u.lastName}`;

const ChantierCreatePage = () => {
  const { addChantier, loading, error } = useChantiers();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState(initialForm);

  const isAdmin = user?.role === 'ADMIN';
  const isChefProjet = user?.role === 'CHEF_PROJET';

  const [clients, setClients] = useState([]);
  const [chefsProjet, setChefsProjet] = useState([]);
  const [chefsChantier, setChefsChantier] = useState([]);
  const [magasiniers, setMagasiniers] = useState([]);
  const [agentsSaisie, setAgentsSaisie] = useState([]);
  const [loadingOptions, setLoadingOptions] = useState(true);
  const [optionsError, setOptionsError] = useState(null);

  useEffect(() => {
    if (user && !isAdmin && !isChefProjet) {
      navigate('/chantiers');
    }
  }, [user, isAdmin, isChefProjet, navigate]);

  useEffect(() => {
    if (isChefProjet && user?.id) {
      setForm((prev) => ({ ...prev, chefProjetId: String(user.id) }));
    }
  }, [isChefProjet, user]);

  useEffect(() => {
    const loadOptions = async () => {
      setLoadingOptions(true);
      setOptionsError(null);
      try {
        const requests = [
          getClients({ size: 200 }),
          getUsersByRole('CHEF_CHANTIER'),
          getUsersByRole('MAGASINIER'),
          getUsersByRole('AGENT_SAISIE'),
        ];
        if (isAdmin) {
          requests.push(getUsersByRole('CHEF_PROJET'));
        }

        const results = await Promise.all(requests);
        setClients(results[0]?.content ?? results[0] ?? []);
        setChefsChantier(results[1] ?? []);
        setMagasiniers(results[2] ?? []);
        setAgentsSaisie(results[3] ?? []);
        if (isAdmin) {
          setChefsProjet(results[4] ?? []);
        }
      } catch (err) {
        setOptionsError('Impossible de charger les listes (clients/utilisateurs).');
      } finally {
        setLoadingOptions(false);
      }
    };
    loadOptions();
  }, [isAdmin]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleLocationChange = (lat, lng) => {
    setForm((prev) => ({ ...prev, latitude: lat, longitude: lng }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const payload = {
      name: form.name,
      reference: form.reference || null,
      address: form.address || null,
      description: form.description || null,
      latitude: form.latitude,
      longitude: form.longitude,
      status: form.status,
      startDate: toLocalDateTime(form.startDate),
      endDate: toLocalDateTime(form.endDate),
      clientId: toNullableLong(form.clientId),
      chefProjetId: isChefProjet ? user.id : toNullableLong(form.chefProjetId),
      magasinierId: toNullableLong(form.magasinierId),
      agentSaisieId: toNullableLong(form.agentSaisieId),
      chefChantierId: toNullableLong(form.chefChantierId),
    };

    try {
      const created = await addChantier(payload);
      navigate(`/chantiers/${created.id}`);
    } catch (err) {
      // l'erreur est déjà exposée via `error` par le hook
    }
  };

  return (
    <div className="chantiers-page">
      <div className="page-header"><h1>🏗️ Nouveau chantier</h1></div>

      {error && <div className="error-banner">❌ {error}</div>}
      {optionsError && <div className="error-banner">❌ {optionsError}</div>}

      <form onSubmit={handleSubmit} className="chantier-form">
        <div className="form-group">
          <label>Nom du site *</label>
          <input type="text" name="name" value={form.name} onChange={handleChange} required />
        </div>

        <div className="form-group">
          <label>Référence</label>
          <input type="text" name="reference" value={form.reference} onChange={handleChange} />
        </div>

        <div className="form-group">
          <label>Adresse</label>
          <input type="text" name="address" value={form.address} onChange={handleChange} />
        </div>

        <div className="form-group">
          <label>Description</label>
          <textarea name="description" value={form.description} onChange={handleChange} rows={4}
            placeholder="Détails du chantier, contraintes d'accès, particularités..." />
        </div>

        <LocationPicker address={form.address} latitude={form.latitude} longitude={form.longitude} onChange={handleLocationChange} />

        <div className="form-row">
          <div className="form-group">
            <label>Date de début</label>
            <input type="date" name="startDate" value={form.startDate} onChange={handleChange} />
          </div>
          <div className="form-group">
            <label>Date de fin prévisionnelle</label>
            <input type="date" name="endDate" value={form.endDate} onChange={handleChange} />
          </div>
        </div>

        <div className="form-group">
          <label>Statut</label>
          <select name="status" value={form.status} onChange={handleChange}>
            <option value="PLANIFIE">Planifié</option>
            <option value="EN_COURS">En cours</option>
            <option value="TERMINE">Terminé</option>
            <option value="SUSPENDU">Suspendu</option>
          </select>
        </div>

        <div className="form-row">
          <div className="form-group">
            <label>Client *</label>
            <select name="clientId" value={form.clientId} onChange={handleChange} required disabled={loadingOptions}>
              <option value="">{loadingOptions ? 'Chargement...' : '— Sélectionner un client —'}</option>
              {clients.map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>Chef de projet *</label>
            {isChefProjet ? (
              <input type="text" value={`${user.firstName || ''} ${user.lastName || ''}`.trim() || 'Vous'} disabled />
            ) : (
              <select name="chefProjetId" value={form.chefProjetId} onChange={handleChange} required disabled={loadingOptions}>
                <option value="">{loadingOptions ? 'Chargement...' : '— Sélectionner —'}</option>
                {chefsProjet.map((u) => (
                  <option key={u.id} value={u.id}>{userLabel(u)}</option>
                ))}
              </select>
            )}
          </div>
        </div>

        <div className="form-row">
          <div className="form-group">
            <label>Magasinier</label>
            <select name="magasinierId" value={form.magasinierId} onChange={handleChange} disabled={loadingOptions}>
              <option value="">{loadingOptions ? 'Chargement...' : '— Aucun —'}</option>
              {magasiniers.map((u) => (
                <option key={u.id} value={u.id}>{userLabel(u)}</option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label>Agent de saisie</label>
            <select name="agentSaisieId" value={form.agentSaisieId} onChange={handleChange} disabled={loadingOptions}>
              <option value="">{loadingOptions ? 'Chargement...' : '— Aucun —'}</option>
              {agentsSaisie.map((u) => (
                <option key={u.id} value={u.id}>{userLabel(u)}</option>
              ))}
            </select>
          </div>
        </div>

        <div className="form-group">
          <label>Chef de chantier</label>
          <select name="chefChantierId" value={form.chefChantierId} onChange={handleChange} disabled={loadingOptions}>
            <option value="">{loadingOptions ? 'Chargement...' : '— Aucun —'}</option>
            {chefsChantier.map((u) => (
              <option key={u.id} value={u.id}>{userLabel(u)}</option>
            ))}
          </select>
        </div>

        <div className="form-actions">
          <button type="submit" className="btn-primary" disabled={loading || loadingOptions}>
            {loading ? 'Création...' : 'Créer le chantier'}
          </button>
          <button type="button" className="btn-view" onClick={() => navigate('/chantiers')}>Annuler</button>
        </div>
      </form>
    </div>
  );
};

export default ChantierCreatePage;