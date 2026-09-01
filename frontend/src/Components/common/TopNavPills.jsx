import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Building2, ScrollText, BarChart3, ListChecks, Package, Pin, Clock, User } from 'lucide-react';

const pillItems = [
  { path: '/dashboard', label: 'Dashboard', Icon: LayoutDashboard },
  { path: '/chantiers', label: 'Chantiers', Icon: Building2 },
  { path: '/taches', label: 'Tâches', Icon: ListChecks },
  { path: '/affectations', label: 'Affectations', Icon: Pin },
  { path: '/pointage', label: 'Pointage', Icon: Clock },
  { path: '/ressources', label: 'Ressources', Icon: Package },
  { path: '/journal', label: 'Journal', Icon: ScrollText },
  { path: '/statistiques', label: 'Statistiques', Icon: BarChart3 },
  // ⚠️ NOUVEAU
  { path: '/mon-profil', label: 'Mon profil', Icon: User },
];

const TopNavPills = () => {
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

export default TopNavPills;