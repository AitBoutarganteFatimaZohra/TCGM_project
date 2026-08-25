import {
  Routes,
  Route,
  Navigate,
  useLocation,
} from 'react-router-dom';

import useAuth from '../hooks/useAuth';
import { hasAccess } from '../config/accessConfig';

import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';
import DashboardRouter from '../pages/DashboardRouter';

import ChantiersPage from '../pages/ChantiersPage';
import ChantierCreatePage from '../pages/ChantierCreatePage';
import ChantierDetailPage from '../pages/ChantierDetailPage';
import ChantierEditPage from '../pages/ChantierEditPage';

import ClientsPage from '../pages/ClientsPage';
import ClientCreatePage from '../pages/ClientCreatePage';
import ClientDetailPage from '../pages/ClientDetailPage';
import ClientEditPage from '../pages/ClientEditPage';

import OuvriersPage from '../pages/OuvriersPage';
import OuvrierCreatePage from '../pages/OuvrierCreatePage';
import OuvrierDetailPage from '../pages/OuvrierDetailPage';
import OuvrierEditPage from '../pages/OuvrierEditPage';

import TachesPage from '../pages/TachesPage';
import TacheCreatePage from '../pages/TacheCreatePage';
import TacheDetailPage from '../pages/TacheDetailPage';
import TacheEditPage from '../pages/TacheEditPage';

import TravauxPage from '../pages/TravauxPage';
import TravauxCreatePage from '../pages/TravauxCreatePage';
import TravauxDetailPage from '../pages/TravauxDetailPage';
import TravauxEditPage from '../pages/TravauxEditPage';

import AffectationsPage from '../pages/AffectationsPage';
import AffectationCreatePage from '../pages/AffectationCreatePage';
import AffectationDetailPage from '../pages/AffectationDetailPage';
import AffectationEditPage from '../pages/AffectationEditPage';

import PointagePage from '../pages/PointagePage';
import PointageCreatePage from '../pages/PointageCreatePage';
import PointageDetailPage from '../pages/PointageDetailPage';
import PointageEditPage from '../pages/PointageEditPage';

// ↓ Nouveau : module Ressources (Magasinier)
import RessourcesPage from '../pages/RessourcesPage';
import RessourceCreatePage from '../pages/RessourceCreatePage';
import RessourceDetailPage from '../pages/RessourceDetailPage';
import RessourceEditPage from '../pages/RessourceEditPage';
import MesTachesPage from '../pages/MesTachesPage';
import MonJournalPage from '../pages/MonJournalPage';
// ✅ NOUVEAU : journal personnel de l'Agent de Saisie
import MonJournalAgentPage from '../pages/MonJournalAgentPage';
import MonProfilPage from '../pages/MonProfilPage';

import JournalPage from '../pages/JournalPage';

import AuthLayout from '../layouts/AuthLayout';
import DashboardLayout from '../layouts/DashboardLayout';

import StatistiquesPage from '../pages/StatistiquesPage';

// =========================================================
// GARDE DE ROLE
// =========================================================
const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, user } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // Vérification des accès par rôle
  if (!hasAccess(user?.role, location.pathname)) {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
};

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
      {/* Routes publiques */}
      <Route element={<AuthLayout />}>
        <Route path="/login" element={<LoginPage />} />
      </Route>

      {/* Routes protégées */}
      <Route
        element={
          isAuthenticated ? (
            <ProtectedRoute>
              <DashboardLayout />
            </ProtectedRoute>
          ) : (
            <Navigate to="/login" replace />
          )
        }
      >
        {/* Dashboard */}
        <Route path="/dashboard" element={<DashboardRouter />} />

        {/* Utilisateurs */}
        <Route path="/utilisateurs/nouveau" element={<RegisterPage />} />

        {/* Chantiers */}
        <Route path="/chantiers" element={<ChantiersPage />} />
        <Route path="/chantiers/create" element={<ChantierCreatePage />} />
        <Route path="/chantiers/:id" element={<ChantierDetailPage />} />
        <Route path="/chantiers/edit/:id" element={<ChantierEditPage />} />

        {/* Clients */}
        <Route path="/clients" element={<ClientsPage />} />
        <Route path="/clients/create" element={<ClientCreatePage />} />
        <Route path="/clients/:id" element={<ClientDetailPage />} />
        <Route path="/clients/edit/:id" element={<ClientEditPage />} />

        {/* Ouvriers */}
        <Route path="/ouvriers" element={<OuvriersPage />} />
        <Route path="/ouvriers/nouveau" element={<OuvrierCreatePage />} />
        <Route path="/ouvriers/:id" element={<OuvrierDetailPage />} />
        <Route path="/ouvriers/:id/modifier" element={<OuvrierEditPage />} />

        {/* Tâches */}
        <Route path="/taches" element={<TachesPage />} />
        <Route path="/taches/nouveau" element={<TacheCreatePage />} />
        <Route path="/taches/:id" element={<TacheDetailPage />} />
        <Route path="/taches/:id/modifier" element={<TacheEditPage />} />

        {/* Travaux */}
        <Route path="/travaux" element={<TravauxPage />} />
        <Route path="/travaux/nouveau" element={<TravauxCreatePage />} />
        <Route path="/travaux/:id" element={<TravauxDetailPage />} />
        <Route path="/travaux/edit/:id" element={<TravauxEditPage />} />

        {/* Affectations */}
        <Route path="/affectations" element={<AffectationsPage />} />
        <Route path="/affectations/nouveau" element={<AffectationCreatePage />} />
        <Route path="/affectations/:id" element={<AffectationDetailPage />} />
        <Route path="/affectations/:id/modifier" element={<AffectationEditPage />} />

        {/* Pointage */}
        <Route path="/pointage" element={<PointagePage />} />
        <Route path="/pointage/nouveau" element={<PointageCreatePage />} />
        <Route path="/pointage/:id" element={<PointageDetailPage />} />
        <Route path="/pointage/:id/modifier" element={<PointageEditPage />} />

        {/* Ressources (Magasinier) */}
        <Route path="/ressources" element={<RessourcesPage />} />
        <Route path="/ressources/create" element={<RessourceCreatePage />} />
        <Route path="/ressources/:id" element={<RessourceDetailPage />} />
        <Route path="/ressources/:id/modifier" element={<RessourceEditPage />} />

        {/* Consultation (Magasinier) */}
        <Route path="/mes-taches" element={<MesTachesPage />} />
        <Route path="/mon-journal" element={<MonJournalPage />} />

        {/* ✅ NOUVEAU : journal personnel (Agent de Saisie) */}
        <Route path="/mon-journal-agent" element={<MonJournalAgentPage />} />

        <Route path="/mon-profil" element={<MonProfilPage />} />

        {/* Journal */}
        <Route path="/journal" element={<JournalPage />} />

        {/* Statistiques */}
        <Route path="/statistiques" element={<StatistiquesPage />} />
      </Route>

      {/* Redirection par défaut */}
      <Route path="/" element={<Navigate to="/login" replace />} />

      {/* Page 404 */}
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