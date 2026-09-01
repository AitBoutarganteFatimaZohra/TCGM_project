import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Building2, Package, ListChecks, ScrollText, User } from 'lucide-react';

const pillItems = [
  { path: '/dashboard', label: 'Dashboard', Icon: LayoutDashboard },
  // ⚠️ NOUVEAU : Magasinier peut voir ses chantiers (scopés côté backend)
  { path: '/chantiers', label: 'Chantiers', Icon: Building2 },
  { path: '/ressources', label: 'Ressources', Icon: Package },
  { path: '/mes-taches', label: 'Tâches', Icon: ListChecks },
  { path: '/mon-journal', label: 'Journal', Icon: ScrollText },
  { path: '/mon-profil', label: 'Mon profil', Icon: User },
];

const MagasinierNavPills = () => {
  return (
    <nav className="topnav-pills" aria-label="Navigation principale">
      {pillItems.map(({ path, label, Icon }) => (
        <NavLink
          key={path}
          to={path}
          className={({ isActive }) =>
            `topnav-pills__item${isActive ? ' topnav-pills__item--active' : ''}`
          }
        >
          <Icon size={18} strokeWidth={1.8} />
          <span>{label}</span>
        </NavLink>
      ))}
    </nav>
  );
};

export default MagasinierNavPills;