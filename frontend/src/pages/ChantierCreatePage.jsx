import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import useChantiers from '../hooks/useChantiers';

const initialForm = {
  name: '',
  reference: '',
  address: '',
  status: 'PLANIFIE',
  startDate: '',
  endDate: '',
  clientId: '',
  chefProjetId: '',
  magasinierId: '',
  agentSaisieId: '',
  chefChantierId: '',
};

// Le backend attend des LocalDateTime, pas de simples dates.
// On complète avec T00:00:00 pour que Jackson parse correctement.
const toLocalDateTime = (dateStr) => (dateStr ? `${dateStr}T00:00:00` : null);

// Les champs d'ID optionnels doivent être null (pas "") si vides,
// sinon Spring tentera de parser une chaîne vide en Long et plantera.
const toNullableLong = (value) => (value ? Number(value) : null);

const ChantierCreatePage = () => {
  const { addChantier, loading, error } = useChantiers();
  const navigate = useNavigate();
  const [form, setForm] = useState(initialForm);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const payload = {
      name: form.name,
      reference: form.reference || null,
      address: form.address || null,
      status: form.status,
      startDate: toLocalDateTime(form.startDate),
      endDate: toLocalDateTime(form.endDate),
      clientId: toNullableLong(form.clientId),
      chefProjetId: toNullableLong(form.chefProjetId),
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
      <div className="page-header">
        <h1>🏗️ Nouveau chantier</h1>
      </div>

      {error && <div className="error-banner">❌ {error}</div>}

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

        {/*
          TEMPORAIRE : IDs saisis à la main en attendant useClients.js et
          la liste des utilisateurs par rôle, pour remplacer ça par de
          vrais <select> avec les noms (Client, Chef de projet, etc.).
        */}
        <div className="form-row">
          <div className="form-group">
            <label>ID Client *</label>
            <input type="number" name="clientId" value={form.clientId} onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label>ID Chef de projet *</label>
            <input type="number" name="chefProjetId" value={form.chefProjetId} onChange={handleChange} required />
          </div>
        </div>

        <div className="form-row">
          <div className="form-group">
            <label>ID Magasinier</label>
            <input type="number" name="magasinierId" value={form.magasinierId} onChange={handleChange} />
          </div>
          <div className="form-group">
            <label>ID Agent de saisie</label>
            <input type="number" name="agentSaisieId" value={form.agentSaisieId} onChange={handleChange} />
          </div>
        </div>

        <div className="form-group">
          <label>ID Chef de chantier</label>
          <input type="number" name="chefChantierId" value={form.chefChantierId} onChange={handleChange} />
        </div>

        <div className="form-actions">
          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? 'Création...' : 'Créer le chantier'}
          </button>
          <button type="button" className="btn-view" onClick={() => navigate('/chantiers')}>
            Annuler
          </button>
        </div>
      </form>
    </div>
  );
};

export default ChantierCreatePage;