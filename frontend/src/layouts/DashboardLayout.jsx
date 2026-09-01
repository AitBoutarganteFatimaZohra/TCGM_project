import { Outlet } from 'react-router-dom';

import Sidebar from '../Components/common/Sidebar';
import Navbar from '../Components/common/Navbar';
import TopNavPills from '../Components/common/TopNavPills';
import MagasinierNavPills from '../components/common/MagasinierNavPills';
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
        className="dashboard-layout"
        style={{
          width: '100%',
        }}
      >
        <Navbar />

        <main
          className="dashboard-content"
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
      className="dashboard-layout"
      style={{
        display: 'flex',
        width: '100%',
      }}
    >
      <Sidebar />

      {/* marginLeft: 0 est OBLIGATOIRE ici : la classe CSS "dashboard-main"
          contient margin-left: 260px (pensé pour une sidebar en
          position:fixed). Comme la Sidebar est ici un enfant flex normal,
          flexbox gère déjà le décalage tout seul — sans ce reset on obtient
          un double espace (260px de flex + 260px de margin-left = le grand
          vide que tu vois entre la sidebar et le contenu). */}
      <div
        className="dashboard-main"
        style={{
          flex: 1,
          display: 'flex',
          flexDirection: 'column',
          minWidth: 0,
          marginLeft: 0,
        }}
      >
        <Navbar />

        <main
          className="dashboard-content"
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