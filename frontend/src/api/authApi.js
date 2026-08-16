import axiosInstance from './axiosConfig';

// =============================================================
// AUTHENTIFICATION
// =============================================================

/**
 * Connexion d'un utilisateur
 * POST /api/auth/login
 */
export const login = async (email, password) => {
  const response = await axiosInstance.post('/auth/login', { email, password });
  return response.data;
};

/**
 * Inscription d'un utilisateur
 * POST /api/auth/register
 */
export const register = async (userData) => {
  const response = await axiosInstance.post('/auth/register', userData);
  return response.data;
};

/**
 * Rafraîchir le token
 * POST /api/auth/refresh
 */
export const refreshToken = async (refreshToken) => {
  const response = await axiosInstance.post('/auth/refresh', { refreshToken });
  return response.data;
};

/**
 * Déconnexion
 * POST /api/auth/logout
 */
export const logout = async () => {
  const token = localStorage.getItem('accessToken');
  const response = await axiosInstance.post('/auth/logout', null, {
    headers: { Authorization: `Bearer ${token}` }
  });
  return response.data;
};

/**
 * Récupérer l'utilisateur courant
 * GET /api/auth/me
 */
export const getCurrentUser = async () => {
  const response = await axiosInstance.get('/auth/me');
  return response.data;
};