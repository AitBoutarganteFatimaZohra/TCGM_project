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
  Package,
  ScrollText,
  BarChart3,
  UserPlus,
} from 'lucide-react';
import logo from '../../assets/images/Logo_TCGM.svg';
import useAuth from '../../hooks/useAuth';
import { ROUTE_ACCESS } from '../../config/accessConfig';

const menuItems = [
  { path: '/dashboard', label: 'Dashboard', Icon: LayoutDashboard },
  { path: '/chantiers', label: 'Chantiers', Icon: Building2 },
  { path: '/clients', label: 'Clients', Icon: Users },
  { path: '/ouvriers', label: 'Ouvriers', Icon: HardHat },
  { path: '/taches', label: 'Tâches', Icon: ListChecks },
  { path: '/travaux', label: 'Travaux', Icon: Wrench },
  { path: '/affectations', label: 'Affectations', Icon: Pin },
  { path: '/pointage', label: 'Pointage', Icon: Clock },
  // 🔧 CORRIGÉ : entrée manquante — sans elle, même un rôle autorisé
  // dans ROUTE_ACCESS ne voyait jamais le lien (rien à filtrer).
  // Ajoutée ici pour Chef de Chantier (voir accessConfig.js pour
  // la liste complète des rôles autorisés sur /ressources).
  { path: '/ressources', label: 'Ressources', Icon: Package },
  { path: '/journal', label: 'Journal', Icon: ScrollText },
  { path: '/mon-journal-agent', label: 'Mon journal', Icon: ScrollText },
  { path: '/statistiques', label: 'Statistiques', Icon: BarChart3 },
];

const adminItem = {
  path: '/utilisateurs/nouveau',
  label: 'Utilisateurs',
  Icon: UserPlus,
};

const Sidebar = () => {
  const { user } = useAuth();
  const role = user?.role;

  const visibleItems = menuItems.filter((item) => {
    const allowed = ROUTE_ACCESS[item.path];
    return allowed ? allowed.includes(role) : true;
  });

  const isAdmin = role === 'ADMIN';

  return (
    <aside className="sidebar">
      <div className="sidebar__logo">
        <img src={logo} alt="TCGM" />
      </div>

      <nav className="sidebar__nav" aria-label="Navigation principale">
        {visibleItems.map(({ path, label, Icon }) => (
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