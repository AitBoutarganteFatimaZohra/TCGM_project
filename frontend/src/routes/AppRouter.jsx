import {
  Routes,
  Route,
  Navigate,
} from 'react-router-dom';

import useAuth from '../hooks/useAuth';

import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';
import DashboardPage from '../pages/DashboardPage';

import ChantiersPage from '../pages/ChantiersPage';
import ChantierCreatePage from '../pages/ChantierCreatePage';
import ChantierDetailPage from '../pages/ChantierDetailPage';
import ChantierEditPage from '../pages/ChantierEditPage';

import AuthLayout from '../layouts/AuthLayout';
import DashboardLayout from '../layouts/DashboardLayout';

const AppRouter = () => {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return (
      <div
        style={{
          minHeight: '100vh',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: '24px',
        }}
      >
        Chargement de l'authentification...
      </div>
    );
  }

  return (
    <Routes>

      {/* ============================= */}
      {/* PAGES PUBLIQUES (Login uniquement) */}
      {/* ============================= */}

      <Route element={<AuthLayout />}>
        <Route path="/login" element={<LoginPage />} />
      </Route>

      {/* ============================= */}
      {/* ESPACE PROTÉGÉ (Dashboard + modules) */}
      {/* ============================= */}

      <Route
        element={
          isAuthenticated ? <DashboardLayout /> : <Navigate to="/login" replace />
        }
      >
        <Route path="/dashboard" element={<DashboardPage />} />

        {/* Création de compte réservée à l'Admin (vérifié dans RegisterPage) */}
        <Route path="/utilisateurs/nouveau" element={<RegisterPage />} />

        <Route path="/chantiers" element={<ChantiersPage />} />
        <Route path="/chantiers/create" element={<ChantierCreatePage />} />
        <Route path="/chantiers/:id" element={<ChantierDetailPage />} />
        <Route path="/chantiers/edit/:id" element={<ChantierEditPage />} />

        {/* Ajoute ici les routes des autres modules (clients, ouvriers,
            taches, travaux, affectations, pointage, journal, statistiques)
            au même format, une fois leurs pages prêtes. */}
      </Route>

      {/* ============================= */}
      {/* PAGE D'ACCUEIL : toujours le login en premier */}
      {/* ============================= */}

      <Route path="/" element={<Navigate to="/login" replace />} />

      {/* ============================= */}
      {/* PAGE 404                      */}
      {/* ============================= */}

      <Route
        path="*"
        element={
          <div style={{ padding: '50px', textAlign: 'center' }}>
            <h1>404</h1>
            <p>Cette page n'existe pas.</p>
          </div>
        }
      />

    </Routes>
  );
};

export default AppRouter;