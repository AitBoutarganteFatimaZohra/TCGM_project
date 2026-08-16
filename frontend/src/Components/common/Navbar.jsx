import useAuth from '../../hooks/useAuth';

const Navbar = () => {
  const { user, logout } = useAuth();

  return (
    <nav className="navbar">
      <h2 className="navbar__title">
        {document?.title || 'Tableau de bord'}
      </h2>

      <div className="navbar__right">
        <span className="navbar__user">
          👋 {user?.firstName || ''} {user?.lastName || ''}
        </span>

        <button type="button" onClick={logout} className="navbar__logout">
          Déconnexion
        </button>
      </div>
    </nav>
  );
};

export default Navbar;