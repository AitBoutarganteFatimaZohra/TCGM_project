import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Building2, ScrollText, BarChart3, ListChecks, Package } from 'lucide-react';

// Nav en pilules utilisée à la place de la Sidebar pour les rôles de
// supervision (actuellement : Chef de Projet). Voir accessConfig.js pour
// la liste des modules autorisés à ce rôle.
const pillItems = [
  { path: '/dashboard', label: 'Dashboard', Icon: LayoutDashboard },
  { path: '/chantiers', label: 'Chantiers', Icon: Building2 },
  { path: '/taches', label: 'Tâches', Icon: ListChecks },
  // ✅ NOUVEAU : le Chef de Projet valide/rejette les propositions de
  // changement de statut soumises par le Magasinier (circuit de
  // validation des ressources, niveau 2 en cas de recours) — voir
  // accessConfig.js où '/ressources' lui a été ouvert.
  { path: '/ressources', label: 'Ressources', Icon: Package },
  { path: '/journal', label: 'Journal', Icon: ScrollText },
  { path: '/statistiques', label: 'Statistiques', Icon: BarChart3 },
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