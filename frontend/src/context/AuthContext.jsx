import React, {
  createContext,
  useContext,
  useEffect,
  useState,
} from 'react';

import {
  login as loginApi,
  logout as logoutApi,
} from '../api/authApi';

const AuthContext = createContext(null);

// ⚠️ Le backend renvoie l'identifiant utilisateur sous la clé `userId`
// (voir AuthResponse.java), pas `id`. On normalise ici une bonne fois
// pour toutes pour que tout le reste du frontend puisse utiliser
// `user.id` de façon fiable (comparaisons de propriétaire, affectations...).
const normalizeUser = (data) => {
  if (!data) return null;
  return {
    ...data,
    id: data.id ?? data.userId ?? null,
  };
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  /*
   * Vérifier si une session existe déjà
   * au démarrage de l'application.
   */
  useEffect(() => {
    const checkAuth = () => {
      try {
        const token = localStorage.getItem('accessToken');
        const savedUser = localStorage.getItem('user');

        if (token && savedUser) {
          const parsedUser = normalizeUser(JSON.parse(savedUser));

          setUser(parsedUser);
          setIsAuthenticated(true);
        } else {
          setUser(null);
          setIsAuthenticated(false);
        }
      } catch (error) {
        console.error(
          'Erreur lors de la récupération de la session :',
          error
        );

        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');

        setUser(null);
        setIsAuthenticated(false);
      } finally {
        setLoading(false);
      }
    };

    checkAuth();
  }, []);

  /*
   * Connexion
   */
  const login = async (email, password) => {
    try {
      const data = await loginApi(email, password);

      console.log('Réponse du login :', data);

      if (!data || !data.accessToken) {
        return {
          success: false,
          message: 'Le serveur n’a pas retourné de token.',
        };
      }

      localStorage.setItem(
        'accessToken',
        data.accessToken
      );

      if (data.refreshToken) {
        localStorage.setItem(
          'refreshToken',
          data.refreshToken
        );
      }

      // On stocke la réponse brute du backend en localStorage (inchangé),
      // mais on normalise l'objet utilisé dans le contexte React pour
      // garantir un champ `id` fiable partout dans l'app.
      localStorage.setItem(
        'user',
        JSON.stringify(data)
      );

      const normalizedUser = normalizeUser(data);
      setUser(normalizedUser);
      setIsAuthenticated(true);

      return {
        success: true,
        data: normalizedUser,
      };
    } catch (error) {
      console.error(
        'Erreur de connexion :',
        error
      );

      return {
        success: false,
        message:
          error?.response?.data?.message ||
          'Erreur de connexion',
      };
    }
  };

  /*
   * Déconnexion
   */
  const logout = async () => {
    try {
      await logoutApi();
    } catch (error) {
      console.error(
        'Erreur lors de la déconnexion :',
        error
      );
    } finally {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');

      setUser(null);
      setIsAuthenticated(false);
    }
  };

  const value = {
    user,
    loading,
    isAuthenticated,
    login,
    logout,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

/*
 * Hook nommé
 */
export const useAuth = () => {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error(
      'useAuth doit être utilisé à l’intérieur d’un AuthProvider'
    );
  }

  return context;
};

/*
 * Export par défaut
 */
export default AuthContext;