import type { Card } from '../types/Card';

interface Props {
  cards: Card[];
  onEdit: (card: Card) => void;
  onDelete: (id: number) => void;
  loading: boolean;
}

const rarityColors: Record<string, string> = {
  Common: '#94a3b8',
  Uncommon: '#22c55e',
  Rare: '#3b82f6',
  'Ultra Rare': '#a855f7',
  'Double Rare': '#f59e0b',
  'Mythic Rare': '#ef4444',
  'Secret Rare': '#ec4899',
};

export default function CardList({ cards, onEdit, onDelete, loading }: Props) {
  if (loading) {
    return (
      <div className="loading">
        <div className="spinner" />
        <p>Chargement des cartes...</p>
      </div>
    );
  }

  if (cards.length === 0) {
    return (
      <div className="empty-state">
        <div className="empty-icon">🃏</div>
        <h3>Aucune carte trouvée</h3>
        <p>Ajoutez votre première carte à la collection</p>
      </div>
    );
  }

  const totalValue = cards.reduce((sum, c) => sum + c.price * c.quantity, 0);

  return (
    <div className="card-list-section">
      <div className="stats-bar">
        <div className="stat">
          <span className="stat-value">{cards.length}</span>
          <span className="stat-label">Cartes uniques</span>
        </div>
        <div className="stat">
          <span className="stat-value">{cards.reduce((s, c) => s + c.quantity, 0)}</span>
          <span className="stat-label">Total exemplaires</span>
        </div>
        <div className="stat">
          <span className="stat-value">{totalValue.toFixed(2)} €</span>
          <span className="stat-label">Valeur totale</span>
        </div>
      </div>

      <div className="card-grid">
        {cards.map((card) => (
          <div key={card.id} className="card-item">
            <div className="card-header">
              <span
                className="rarity-badge"
                style={{ backgroundColor: rarityColors[card.rarity] || '#64748b' }}
              >
                {card.rarity}
              </span>
              <span className="game-tag">{card.game}</span>
            </div>

            <h3 className="card-name">{card.name}</h3>
            <p className="card-set">{card.setName}</p>

            {card.description && (
              <p className="card-description">{card.description}</p>
            )}

            <div className="card-footer">
              <div className="card-meta">
                <span className="quantity">×{card.quantity}</span>
                <span className="price">{card.price.toFixed(2)} €</span>
              </div>
              <div className="card-actions">
                <button className="btn btn-edit" onClick={() => onEdit(card)}>
                  Modifier
                </button>
                <button
                  className="btn btn-delete"
                  onClick={() => card.id && onDelete(card.id)}
                >
                  Supprimer
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
