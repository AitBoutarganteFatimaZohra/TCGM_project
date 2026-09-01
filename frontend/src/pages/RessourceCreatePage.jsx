import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import useRessources from '../hooks/useRessources';
import useMySites from '../hooks/useMySites';
import useAuth from '../hooks/useAuth';

const initialForm = {
  nom: '',
  type: 'MATERIEL',
  quantite: '',
  unite: '',
  statut: 'DISPONIBLE',
  codeInterne: '',
  numeroSerie: '',
  seuilAlerte: '',
  description: '',
};

const TYPE_LABELS = {
  MATERIEL: 'Matériel',
  EQUIPEMENT: 'Équipement',
  OUTIL: 'Outil',
  CONSOMMABLE: 'Consommable',
};

const STATUT_LABELS = {
  DISPONIBLE: 'Disponible',
  EN_UTILISATION: 'En utilisation',
  HORS_SERVICE: 'Hors service',
  EN_MAINTENANCE: 'En maintenance',
};

const RessourceCreatePage = () => {
  const navigate = useNavigate();
  const { addRessource, loading, error } = useRessources();
  const { sites, loading: loadingSites } = useMySites();
  const { user } = useAuth();
  const [form, setForm] = useState(initialForm);
  const [siteId, setSiteId] = useState(null);

  // ⚠️ REDIRECTION SI CHEF DE CHANTIER
  useEffect(() => {
    if (user?.role === 'CHEF_CHANTIER') {
      navigate('/ressources');
    }
  }, [user, navigate]);

  if (!loadingSites && sites.length > 0 && siteId === null) {
    setSiteId(sites[0].id);
  }

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
      const created = await addRessource(payload);
      navigate(`/ressources/${created.id}`);
    } catch {
      // erreur déjà exposée via `error`
    }
  };

  // ⚠️ SI CHEF DE CHANTIER, AFFICHER UN MESSAGE D'ACCÈS REFUSÉ
  if (user?.role === 'CHEF_CHANTIER') {
    return (
      <div className="chantiers-page">
        <div className="error-banner" style={{ marginTop: '2rem', padding: '1.5rem', fontSize: '1.1rem' }}>
          ⛔ Accès refusé. Les Chefs de chantier ne peuvent pas créer de ressources.
        </div>
        <button className="btn-view" onClick={() => navigate('/ressources')} style={{ marginTop: '1rem' }}>
          Retour aux ressources
        </button>
      </div>
    );
  }

  if (loadingSites) {
    return <div className="loading">Chargement...</div>;
  }

  return (
    <div className="chantiers-page">
      <div className="page-header">
        <h1>📦 Nouvelle ressource</h1>
      </div>

      {error && <div className="error-banner">❌ {error}</div>}

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
        {/* FORMULAIRE */}
        <form onSubmit={handleSubmit} className="chantier-form">
          {sites.length > 1 && (
            <div className="form-group">
              <label>Site *</label>
              <select value={siteId || ''} onChange={(e) => setSiteId(Number(e.target.value))}>
                {sites.map((s) => (
                  <option key={s.id} value={s.id}>{s.name}</option>
                ))}
              </select>
            </div>
          )}

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
              <input type="text" name="codeInterne" value={form.codeInterne} onChange={handleChange} placeholder="ex: MAT-0012" />
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
              <input type="text" name="unite" value={form.unite} onChange={handleChange} placeholder="ex: pièce, sac, m³" />
            </div>
          </div>

          <div className="form-group">
            <label>Seuil d'alerte (stock critique)</label>
            <input
              type="number"
              name="seuilAlerte"
              value={form.seuilAlerte}
              onChange={handleChange}
              min="0"
              placeholder="ex: 5 — alerte si la quantité descend en-dessous"
            />
          </div>

          <div className="form-group">
            <label>Description</label>
            <textarea name="description" value={form.description} onChange={handleChange} rows={4} />
          </div>

          <div className="form-actions">
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Création...' : 'Créer la ressource'}
            </button>
            <button type="button" className="btn-view" onClick={() => navigate('/ressources')}>
              Annuler
            </button>
          </div>
        </form>

        {/* APERÇU - Style exact comme dans l'image */}
        <div>
          <h3 style={{ 
            marginBottom: '1.5rem', 
            fontSize: '1rem', 
            fontWeight: 600,
            textTransform: 'uppercase',
            letterSpacing: '0.5px'
          }}>
            APERÇU
          </h3>
          <div style={{
            background: '#f9fafb',
            padding: '1.5rem',
            borderRadius: '8px',
            border: '1px solid #e5e7eb',
            position: 'sticky',
            top: '1rem'
          }}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              {/* NOM */}
              <div>
                <div style={{ fontWeight: 500, color: '#6b7280', fontSize: '0.875rem', marginBottom: '0.25rem' }}>
                  NOM
                </div>
                <div style={{ fontSize: '1rem' }}>
                  {form.nom || '——————————'}
                </div>
              </div>

              {/* TYPE */}
              <div>
                <div style={{ fontWeight: 500, color: '#6b7280', fontSize: '0.875rem', marginBottom: '0.25rem' }}>
                  TYPE
                </div>
                <div style={{ fontSize: '1rem' }}>
                  {TYPE_LABELS[form.type] || form.type || '——————————'}
                </div>
              </div>

              {/* STATUT */}
              <div>
                <div style={{ fontWeight: 500, color: '#6b7280', fontSize: '0.875rem', marginBottom: '0.25rem' }}>
                  STATUT
                </div>
                <div style={{ fontSize: '1rem' }}>
                  {STATUT_LABELS[form.statut] || form.statut || '——————————'}
                </div>
              </div>

              {/* MATÉRIEL */}
              <div>
                <div style={{ fontWeight: 500, color: '#6b7280', fontSize: '0.875rem', marginBottom: '0.25rem' }}>
                  MATÉRIEL
                </div>
                <div style={{ fontSize: '1rem' }}>
                  {form.type === 'MATERIEL' ? '✅ Oui' : '❌ Non'}
                </div>
              </div>

              {/* DISPONIBLE */}
              <div>
                <div style={{ fontWeight: 500, color: '#6b7280', fontSize: '0.875rem', marginBottom: '0.25rem' }}>
                  DISPONIBLE
                </div>
                <div style={{ fontSize: '1rem' }}>
                  {form.statut === 'DISPONIBLE' ? '✅ Oui' : '❌ Non'}
                </div>
              </div>

              {/* CODE INTERNE */}
              <div>
                <div style={{ fontWeight: 500, color: '#6b7280', fontSize: '0.875rem', marginBottom: '0.25rem' }}>
                  CODE INTERNE
                </div>
                <div style={{ fontSize: '1rem' }}>
                  {form.codeInterne || '——————————'}
                </div>
              </div>

              {/* N° DE SÉRIE */}
              <div>
                <div style={{ fontWeight: 500, color: '#6b7280', fontSize: '0.875rem', marginBottom: '0.25rem' }}>
                  N° DE SÉRIE
                </div>
                <div style={{ fontSize: '1rem' }}>
                  {form.numeroSerie || '——————————'}
                </div>
              </div>

              {/* QUANTITÉ */}
              <div>
                <div style={{ fontWeight: 500, color: '#6b7280', fontSize: '0.875rem', marginBottom: '0.25rem' }}>
                  QUANTITÉ
                </div>
                <div style={{ fontSize: '1rem' }}>
                  {form.quantite || '——————————'} {form.unite || ''}
                </div>
              </div>

              {/* UNITÉ */}
              <div>
                <div style={{ fontWeight: 500, color: '#6b7280', fontSize: '0.875rem', marginBottom: '0.25rem' }}>
                  UNITÉ
                </div>
                <div style={{ fontSize: '1rem' }}>
                  {form.unite || '——————————'}
                </div>
              </div>

              {/* SEUIL D'ALERTE */}
              <div>
                <div style={{ fontWeight: 500, color: '#6b7280', fontSize: '0.875rem', marginBottom: '0.25rem' }}>
                  SEUIL D'ALERTE
                </div>
                <div style={{ fontSize: '1rem' }}>
                  {form.seuilAlerte || '——————————'}
                </div>
              </div>

              {/* DESCRIPTION */}
              <div>
                <div style={{ fontWeight: 500, color: '#6b7280', fontSize: '0.875rem', marginBottom: '0.25rem' }}>
                  DESCRIPTION
                </div>
                <div style={{ fontSize: '1rem' }}>
                  {form.description || '——————————'}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RessourceCreatePage;