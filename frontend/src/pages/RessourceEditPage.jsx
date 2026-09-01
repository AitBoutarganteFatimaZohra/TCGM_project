import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import useRessources from '../hooks/useRessources';
import useAuth from '../hooks/useAuth';

const RessourceEditPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { fetchRessourceById, editRessource, loading, error } = useRessources();
  const { user } = useAuth();
  const [form, setForm] = useState(null);
  const [siteId, setSiteId] = useState(null);

  // ⚠️ REDIRECTION SI CHEF DE CHANTIER
  useEffect(() => {
    if (user?.role === 'CHEF_CHANTIER') {
      navigate('/ressources');
    }
  }, [user, navigate]);

  useEffect(() => {
    fetchRessourceById(id).then((data) => {
      setForm({
        nom: data.nom || '',
        type: data.type || 'MATERIEL',
        quantite: data.quantite ?? '',
        unite: data.unite || '',
        statut: data.statut || 'DISPONIBLE',
        codeInterne: data.codeInterne || '',
        numeroSerie: data.numeroSerie || '',
        seuilAlerte: data.seuilAlerte ?? '',
        description: data.description || '',
      });
      setSiteId(data.siteId);
    });
  }, [id]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const payload = {
      nom: form.nom,
      type: form.type,
      quantite: form.quantite ? Number(form.quantite) : null,
      unite: form.unite || null,
      statut: form.statut,
      codeInterne: form.codeInterne || null,
      numeroSerie: form.numeroSerie || null,
      seuilAlerte: form.seuilAlerte ? Number(form.seuilAlerte) : null,
      description: form.description || null,
      siteId,
    };

    try {
      await editRessource(id, payload);
      navigate(`/ressources/${id}`);
    } catch {
      // erreur déjà exposée via `error`
    }
  };

  // ⚠️ SI CHEF DE CHANTIER, AFFICHER UN MESSAGE D'ACCÈS REFUSÉ
  if (user?.role === 'CHEF_CHANTIER') {
    return (
      <div className="chantiers-page">
        <div className="error-banner" style={{ marginTop: '2rem', padding: '1.5rem', fontSize: '1.1rem' }}>
          ⛔ Accès refusé. Les Chefs de chantier ne peuvent pas modifier les ressources.
        </div>
        <button className="btn-view" onClick={() => navigate('/ressources')} style={{ marginTop: '1rem' }}>
          Retour aux ressources
        </button>
      </div>
    );
  }

  if (!form) {
    return <div className="loading">Chargement de la ressource...</div>;
  }

  return (
    <div className="chantiers-page">
      <div className="page-header">
        <h1>📦 Modifier la ressource</h1>
      </div>

      {error && <div className="error-banner">❌ {error}</div>}

      <form onSubmit={handleSubmit} className="chantier-form">
        <div className="form-group">
          <label>Nom *</label>
          <input type="text" name="nom" value={form.nom} onChange={handleChange} required />
        </div>

        <div className="form-row">
          <div className="form-group">
            <label>Type *</label>
            <select name="type" value={form.type} onChange={handleChange}>
              <option value="MATERIEL">Matériel</option>
              <option value="EQUIPEMENT">Équipement</option>
              <option value="OUTIL">Outil</option>
              <option value="CONSOMMABLE">Consommable</option>
            </select>
          </div>
          <div className="form-group">
            <label>Statut</label>
            <select name="statut" value={form.statut} onChange={handleChange}>
              <option value="DISPONIBLE">Disponible</option>
              <option value="EN_UTILISATION">En utilisation</option>
              <option value="HORS_SERVICE">Hors service</option>
              <option value="EN_MAINTENANCE">En maintenance</option>
            </select>
          </div>
        </div>

        <div className="form-row">
          <div className="form-group">
            <label>Code interne</label>
            <input type="text" name="codeInterne" value={form.codeInterne} onChange={handleChange} />
          </div>
          <div className="form-group">
            <label>N° de série</label>
            <input type="text" name="numeroSerie" value={form.numeroSerie} onChange={handleChange} />
          </div>
        </div>

        <div className="form-row">
          <div className="form-group">
            <label>Quantité</label>
            <input type="number" name="quantite" value={form.quantite} onChange={handleChange} min="0" />
          </div>
          <div className="form-group">
            <label>Unité</label>
            <input type="text" name="unite" value={form.unite} onChange={handleChange} />
          </div>
        </div>

        <div className="form-group">
          <label>Seuil d'alerte (stock critique)</label>
          <input type="number" name="seuilAlerte" value={form.seuilAlerte} onChange={handleChange} min="0" />
        </div>

        <div className="form-group">
          <label>Description</label>
          <textarea name="description" value={form.description} onChange={handleChange} rows={4} />
        </div>

        <div className="form-actions">
          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? 'Enregistrement...' : 'Enregistrer les modifications'}
          </button>
          <button type="button" className="btn-view" onClick={() => navigate(`/ressources/${id}`)}>
            Annuler
          </button>
        </div>
      </form>
    </div>
  );
};

export default RessourceEditPage;