import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  Building2,
  Users,
  HardHat,
  ListChecks,
  Wrench,
  Pin,
  Clock,
  ScrollText,
  BarChart3,
  UserPlus,
} from 'lucide-react';
import logo from '../../assets/images/Logo_TCGM.svg';
import useAuth from '../../hooks/useAuth';

const menuItems = [
  { path: '/dashboard', label: 'Dashboard', Icon: LayoutDashboard },
  { path: '/chantiers', label: 'Chantiers', Icon: Building2 },
  { path: '/clients', label: 'Clients', Icon: Users },
  { path: '/ouvriers', label: 'Ouvriers', Icon: HardHat },
  { path: '/taches', label: 'Tâches', Icon: ListChecks },
  { path: '/travaux', label: 'Travaux', Icon: Wrench },
  { path: '/affectations', label: 'Affectations', Icon: Pin },
  { path: '/pointage', label: 'Pointage', Icon: Clock },
  { path: '/journal', label: 'Journal', Icon: ScrollText },
  { path: '/statistiques', label: 'Statistiques', Icon: BarChart3 },
];

// Réservé à l'Admin : création de comptes (voir cahier des charges §6.1)
const adminItem = {
  path: '/utilisateurs/nouveau',
  label: 'Utilisateurs',
  Icon: UserPlus,
};

const Sidebar = () => {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';

  return (
    <aside className="sidebar">
      <div className="sidebar__logo">
        <img src={logo} alt="TCGM" />
      </div>

      <nav className="sidebar__nav" aria-label="Navigation principale">
        {menuItems.map(({ path, label, Icon }) => (
          <NavLink
            key={path}
            to={path}
            className={({ isActive }) =>
              `sidebar__link${isActive ? ' sidebar__link--active' : ''}`
            }
          >
            <Icon size={18} strokeWidth={1.8} className="sidebar__icon" />
            <span>{label}</span>
          </NavLink>
        ))}

        {isAdmin && (
          <NavLink
            key={adminItem.path}
            to={adminItem.path}
            className={({ isActive }) =>
              `sidebar__link${isActive ? ' sidebar__link--active' : ''}`
            }
          >
            <adminItem.Icon size={18} strokeWidth={1.8} className="sidebar__icon" />
            <span>{adminItem.label}</span>
          </NavLink>
        )}
      </nav>
    </aside>
  );
};

export default Sidebar;