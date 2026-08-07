import { useState, useEffect, useCallback } from 'react';
import type { Card } from './types/Card';
import { cardApi } from './api/cardApi';
import CardList from './components/CardList';
import CardForm from './components/CardForm';

export default function App() {
  const [cards, setCards] = useState<Card[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editingCard, setEditingCard] = useState<Card | null>(null);
  const [error, setError] = useState('');

  const loadCards = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = search.trim()
        ? await cardApi.search(search.trim())
        : await cardApi.getAll();
      setCards(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Impossible de charger les cartes');
    } finally {
      setLoading(false);
    }
  }, [search]);

  useEffect(() => {
    const timer = setTimeout(loadCards, search ? 300 : 0);
    return () => clearTimeout(timer);
  }, [loadCards, search]);

  const handleCreate = async (card: Card) => {
    await cardApi.create(card);
    setShowForm(false);
    await loadCards();
  };

  const handleUpdate = async (card: Card) => {
    if (!card.id) return;
    await cardApi.update(card.id, card);
    setEditingCard(null);
    await loadCards();
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Supprimer cette carte de la collection ?')) return;
    try {
      await cardApi.delete(id);
      await loadCards();
    } catch (err) {
      alert(err instanceof Error ? err.message : 'Erreur lors de la suppression');
    }
  };

  return (
    <div className="app">
      <header className="header">
        <div className="header-content">
          <div className="logo">
            <span className="logo-icon">🃏</span>
            <div>
              <h1>TCGM</h1>
              <p>Gestionnaire de Cartes à Collectionner</p>
            </div>
          </div>
          <button
            className="btn btn-primary btn-add"
            onClick={() => setShowForm(true)}
          >
            + Ajouter une carte
          </button>
        </div>
      </header>

      <main className="main">
        <div className="search-bar">
          <input
            type="text"
            placeholder="Rechercher une carte..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          {search && (
            <button className="btn-clear" onClick={() => setSearch('')}>
              ✕
            </button>
          )}
        </div>

        {error && (
          <div className="error-banner">
            {error}
            <button onClick={loadCards}>Réessayer</button>
          </div>
        )}

        <CardList
          cards={cards}
          loading={loading}
          onEdit={setEditingCard}
          onDelete={handleDelete}
        />
      </main>

      {showForm && (
        <CardForm
          onSubmit={handleCreate}
          onCancel={() => setShowForm(false)}
        />
      )}

      {editingCard && (
        <CardForm
          card={editingCard}
          onSubmit={handleUpdate}
          onCancel={() => setEditingCard(null)}
        />
      )}
    </div>
  );
}
