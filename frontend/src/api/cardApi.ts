import type { Card } from '../types/Card';

const API_BASE = '/api/cards';

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Erreur serveur' }));
    throw new Error(error.message || `Erreur ${response.status}`);
  }
  if (response.status === 204) return undefined as T;
  return response.json();
}

export const cardApi = {
  getAll: (): Promise<Card[]> =>
    fetch(API_BASE).then((res) => handleResponse<Card[]>(res)),

  getById: (id: number): Promise<Card> =>
    fetch(`${API_BASE}/${id}`).then((res) => handleResponse<Card>(res)),

  search: (query: string): Promise<Card[]> =>
    fetch(`${API_BASE}/search?q=${encodeURIComponent(query)}`).then((res) =>
      handleResponse<Card[]>(res)
    ),

  create: (card: Card): Promise<Card> =>
    fetch(API_BASE, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(card),
    }).then((res) => handleResponse<Card>(res)),

  update: (id: number, card: Card): Promise<Card> =>
    fetch(`${API_BASE}/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(card),
    }).then((res) => handleResponse<Card>(res)),

  delete: (id: number): Promise<void> =>
    fetch(`${API_BASE}/${id}`, { method: 'DELETE' }).then((res) =>
      handleResponse<void>(res)
    ),
};
