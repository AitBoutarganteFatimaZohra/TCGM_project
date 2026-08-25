import { Outlet } from 'react-router-dom';

import Sidebar from '../Components/common/Sidebar';
import Navbar from '../Components/common/Navbar';
import TopNavPills from '../Components/common/TopNavPills';
import MagasinierNavPills from '../Components/common/MagasinierNavPills';
import useAuth from '../hooks/useAuth';

// Rôles qui utilisent une nav en pilules (sans sidebar classique) au lieu
// de la Sidebar habituelle — leur interface se veut plus ciblée que celle
// de l'Admin ("supervision" pour Chef de Projet, "opérationnel/mon site"
// pour Magasinier).
const PILL_NAV_ROLES = ['CHEF_PROJET', 'MAGASINIER'];

// Quelle nav en pilules afficher selon le rôle — chaque rôle a ses propres
// liens (voir TopNavPills.jsx pour Chef de Projet, MagasinierNavPills.jsx
// pour Magasinier).
const PillNav = ({ role }) => {
  if (role === 'MAGASINIER') return <MagasinierNavPills />;
  return <TopNavPills />;
};

const DashboardLayout = () => {
  const { user } = useAuth();
  const usesPillNav = PILL_NAV_ROLES.includes(user?.role);

  // ======================= LAYOUT SANS SIDEBAR (Chef de Projet, Magasinier) =======================
  if (usesPillNav) {
    return (
      <div
        style={{
          minHeight: '100vh',
          width: '100%',
          backgroundColor: '#f3f4f6',
        }}
      >
        <Navbar />

        <main
          style={{
            padding: '25px',
            boxSizing: 'border-box',
          }}
        >
          <PillNav role={user?.role} />
          <Outlet />
        </main>
      </div>
    );
  }

  // ======================= LAYOUT AVEC SIDEBAR (Admin, autres rôles) =======================
  return (
    <div
      style={{
        display: 'flex',
        minHeight: '100vh',
        width: '100%',
        backgroundColor: '#f3f4f6',
      }}
    >
      <Sidebar />

      <div
        style={{
          flex: 1,
          display: 'flex',
          flexDirection: 'column',
          minWidth: 0,
        }}
      >
        <Navbar />

        <main
          style={{
            flex: 1,
            padding: '25px',
            boxSizing: 'border-box',
          }}
        >
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default DashboardLayout;