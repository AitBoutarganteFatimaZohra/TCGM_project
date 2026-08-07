export interface Card {
  id?: number;
  name: string;
  game: string;
  setName: string;
  rarity: string;
  quantity: number;
  price: number;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
}

export const GAMES = ['Pokemon', 'Magic: The Gathering', 'Yu-Gi-Oh!', 'One Piece', 'Lorcana', 'Autre'];

export const RARITIES = ['Common', 'Uncommon', 'Rare', 'Ultra Rare', 'Double Rare', 'Mythic Rare', 'Secret Rare'];

export const emptyCard = (): Card => ({
  name: '',
  game: 'Pokemon',
  setName: '',
  rarity: 'Common',
  quantity: 1,
  price: 0,
  description: '',
});
