import { useAuth } from '../hooks/useAuth';
import DashboardPage from './DashboardPage';
import ChefProjetDashboardPage from './ChefProjetDashboardPage';
import MagasinierDashboardPage from './MagasinierDashboardPage';
import ChefChantierDashboardPage from './ChefChantierDashboardPage';
import AgentSaisieDashboardPage from './AgentSaisieDashboardPage';

const DashboardRouter = () => {
  const { user } = useAuth();

  if (user?.role === 'CHEF_PROJET') {
    return <ChefProjetDashboardPage />;
  }

  if (user?.role === 'MAGASINIER') {
    return <MagasinierDashboardPage />;
  }

  if (user?.role === 'CHEF_CHANTIER') {
    return <ChefChantierDashboardPage />;
  }

  if (user?.role === 'AGENT_SAISIE') {
    return <AgentSaisieDashboardPage />;
  }

  return <DashboardPage />;
};

export default DashboardRouter;