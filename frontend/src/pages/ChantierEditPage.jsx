import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import useChantiers from '../hooks/useChantiers';
import LocationPicker from '../components/LocationPicker';

// Le backend attend des LocalDateTime, l'input HTML une simple date.
const toDateInputValue = (isoString) => (isoString ? isoString.slice(0, 10) : '');
const toLocalDateTime = (dateStr) => (dateStr ? `${dateStr}T00:00:00` : null);
const toNullableLong = (value) => (value ? Number(value) : null);

const ChantierEditPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { fetchChantierById, editChantier, loading, error } = useChantiers();
  const [form, setForm] = useState(null);

  useEffect(() => {
    fetchChantierById(id).then((data) =>
      setForm({
        name: data.name || '',
        reference: data.reference || '',
        address: data.address || '',
        description: data.description || '',
        latitude: data.latitude ?? null,
        longitude: data.longitude ?? null,
        status: data.status || 'PLANIFIE',
        startDate: toDateInputValue(data.startDate),
        endDate: toDateInputValue(data.endDate),
        // Le GET renvoie des objets imbriqués (client.id), le PUT attend des IDs à plat
        clientId: data.client?.id || '',
        chefProjetId: data.chefProjet?.id || '',
        magasinierId: data.magasinier?.id || '',
        agentSaisieId: data.agentSaisie?.id || '',
        chefChantierId: data.chefChantier?.id || '',
      })
    );
  }, [id]);

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
      chefProjetId: toNullableLong(form.chefProjetId),
      magasinierId: toNullableLong(form.magasinierId),
      agentSaisieId: toNullableLong(form.agentSaisieId),
      chefChantierId: toNullableLong(form.chefChantierId),
    };

    try {
      await editChantier(id, payload);
      navigate(`/chantiers/${id}`);
    } catch (err) {
      // l'erreur est déjà exposée via `error` par le hook
    }
  };

  if (!form) {
    return <div className="loading">Chargement du chantier...</div>;
  }

  return (
    <div className="chantiers-page">
      <div className="page-header">
        <h1>🏗️ Modifier le chantier</h1>
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

        <div className="form-group">
          <label>Description</label>
          <textarea
            name="description"
            value={form.description}
            onChange={handleChange}
            rows={4}
            placeholder="Détails du chantier, contraintes d'accès, particularités..."
          />
        </div>

        <LocationPicker
          address={form.address}
          latitude={form.latitude}
          longitude={form.longitude}
          onChange={handleLocationChange}
        />

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

        {/* TEMPORAIRE : IDs à la main, en attendant useClients.js + liste des
            utilisateurs par rôle pour de vrais <select> avec les noms. */}
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
            {loading ? 'Enregistrement...' : 'Enregistrer les modifications'}
          </button>
          <button type="button" className="btn-view" onClick={() => navigate(`/chantiers/${id}`)}>
            Annuler
          </button>
        </div>
      </form>
    </div>
  );
};

export default ChantierEditPage;