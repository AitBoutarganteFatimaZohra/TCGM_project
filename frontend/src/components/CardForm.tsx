import { useState, type FormEvent } from 'react';
import type { Card } from '../types/Card';
import { GAMES, RARITIES, emptyCard } from '../types/Card';

interface Props {
  card?: Card | null;
  onSubmit: (card: Card) => Promise<void>;
  onCancel: () => void;
}

export default function CardForm({ card, onSubmit, onCancel }: Props) {
  const [form, setForm] = useState<Card>(card ?? emptyCard());
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    setForm((prev) => ({
      ...prev,
      [name]: name === 'quantity' || name === 'price' ? Number(value) : value,
    }));
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      await onSubmit(form);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erreur lors de la sauvegarde');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onCancel}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <h2>{card ? 'Modifier la carte' : 'Ajouter une carte'}</h2>

        {error && <div className="error-banner">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-grid">
            <div className="form-group">
              <label htmlFor="name">Nom *</label>
              <input
                id="name"
                name="name"
                value={form.name}
                onChange={handleChange}
                required
                placeholder="Ex: Pikachu V"
              />
            </div>

            <div className="form-group">
              <label htmlFor="game">Jeu *</label>
              <select id="game" name="game" value={form.game} onChange={handleChange}>
                {GAMES.map((g) => (
                  <option key={g} value={g}>{g}</option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="setName">Extension *</label>
              <input
                id="setName"
                name="setName"
                value={form.setName}
                onChange={handleChange}
                required
                placeholder="Ex: Epee et Bouclier"
              />
            </div>

            <div className="form-group">
              <label htmlFor="rarity">Rareté *</label>
              <select id="rarity" name="rarity" value={form.rarity} onChange={handleChange}>
                {RARITIES.map((r) => (
                  <option key={r} value={r}>{r}</option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="quantity">Quantité *</label>
              <input
                id="quantity"
                name="quantity"
                type="number"
                min="0"
                value={form.quantity}
                onChange={handleChange}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="price">Prix (€) *</label>
              <input
                id="price"
                name="price"
                type="number"
                min="0"
                step="0.01"
                value={form.price}
                onChange={handleChange}
                required
              />
            </div>

            <div className="form-group full-width">
              <label htmlFor="description">Description</label>
              <textarea
                id="description"
                name="description"
                value={form.description ?? ''}
                onChange={handleChange}
                rows={3}
                placeholder="Description optionnelle..."
              />
            </div>
          </div>

          <div className="form-actions">
            <button type="button" className="btn btn-secondary" onClick={onCancel}>
              Annuler
            </button>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? 'Enregistrement...' : card ? 'Mettre à jour' : 'Ajouter'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
