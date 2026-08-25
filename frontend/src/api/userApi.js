import axiosInstance from './axiosConfig';

/**
 * Récupérer les utilisateurs ayant un rôle donné
 * GET /api/users/by-role/{roleName}
 */
export const getUsersByRole = async (roleName) => {
  const response = await axiosInstance.get(`/users/by-role/${roleName}`);
  return response.data;
};

/**
 * Changer le mot de passe de l'utilisateur connecté
 * PATCH /api/users/me/password
 */
export const changeMyPassword = async (data) => {
  const response = await axiosInstance.patch('/users/me/password', data);
  return response.data;
};

// Vous pouvez ajouter d'autres fonctions ici si besoin :
// export const getUserProfile = async () => { ... }
// export const updateUserProfile = async (data) => { ... }