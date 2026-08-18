import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import useClients from '../hooks/useClients';

const ClientEditPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { fetchClientById, editClient, loading, error } = useClients();
  const [form, setForm] = useState(null);

  useEffect(() => {
    fetchClientById(id).then((data) =>
      setForm({
        name: data.name || '',
        contact: data.contact || '',
        address: data.address || '',
        phone: data.phone || '',
        email: data.email || '',
        ice: data.ice || '',
        rc: data.rc || '',
      })
    );
  }, [id]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const payload = {
      name: form.name,
      contact: form.contact || null,
      address: form.address || null,
      phone: form.phone || null,
      email: form.email || null,
      ice: form.ice || null,
      rc: form.rc || null,
    };

    try {
      await editClient(id, payload);
      navigate(`/clients/${id}`);
    } catch (err) {
      // l'erreur est déjà exposée via `error` par le hook
    }
  };

  if (!form) {
    return <div className="loading">Chargement du client...</div>;
  }

  return (
    <div className="clients-page">
      <div className="page-header">
        <h1>👥 Modifier le client</h1>
      </div>

      {error && <div className="error-banner">❌ {error}</div>}

      <form onSubmit={handleSubmit} className="chantier-form">
        <div className="form-group">
          <label>Raison sociale *</label>
          <input type="text" name="name" value={form.name} onChange={handleChange} required />
        </div>

        <div className="form-group">
          <label>Contact</label>
          <input type="text" name="contact" value={form.contact} onChange={handleChange} />
        </div>

        <div className="form-group">
          <label>Adresse</label>
          <input type="text" name="address" value={form.address} onChange={handleChange} />
        </div>

        <div className="form-row">
          <div className="form-group">
            <label>Téléphone</label>
            <input type="text" name="phone" value={form.phone} onChange={handleChange} />
          </div>
          <div className="form-group">
            <label>E-mail</label>
            <input type="email" name="email" value={form.email} onChange={handleChange} />
          </div>
        </div>

        <div className="form-row">
          <div className="form-group">
            <label>ICE</label>
            <input type="text" name="ice" value={form.ice} onChange={handleChange} />
          </div>
          <div className="form-group">
            <label>RC</label>
            <input type="text" name="rc" value={form.rc} onChange={handleChange} />
          </div>
        </div>

        <div className="form-actions">
          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? 'Enregistrement...' : 'Enregistrer les modifications'}
          </button>
          <button type="button" className="btn-view" onClick={() => navigate(`/clients/${id}`)}>
            Annuler
          </button>
        </div>
      </form>
    </div>
  );
};

export default ClientEditPage;