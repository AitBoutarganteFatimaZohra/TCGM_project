import { useState, useRef, useEffect } from 'react';
import useAuth from '../../hooks/useAuth';
import useTheme from '../../hooks/useTheme';
import logoLight from '../../assets/images/Logo_TCGM.svg';
import logoDark from '../../assets/images/tcgm-logo-dark.svg';

const ROLE_LABELS = {
  ADMIN: 'Administrateur',
  CHEF_PROJET: 'Chef de projet',
  CHEF_CHANTIER: 'Chef de chantier',
  OUVRIER: 'Ouvrier',
};

const getInitials = (firstName, lastName) => {
  const a = firstName?.[0] || '';
  const b = lastName?.[0] || '';
  return (a + b).toUpperCase() || '?';
};

const Navbar = () => {
  const { user, logout } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (menuRef.current && !menuRef.current.contains(e.target)) {
        setMenuOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const roleLabel = ROLE_LABELS[user?.role] || user?.role;

  return (
    <nav className="navbar">
      <div className="navbar__brand">
        <img
          src={theme === 'dark' ? logoDark : logoLight}
          alt="TCGM"
          className="navbar__logo"
        />
      </div>

      <div className="navbar__right">
        <button
          type="button"
          className="navbar__icon-btn"
          onClick={toggleTheme}
          aria-label="Changer de thème"
          title={theme === 'light' ? 'Mode sombre' : 'Mode clair'}
        >
          {theme === 'light' ? (
            <svg width="19" height="19" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
              <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
            </svg>
          ) : (
            <svg width="19" height="19" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
              <circle cx="12" cy="12" r="5" />
              <line x1="12" y1="1" x2="12" y2="3" />
              <line x1="12" y1="21" x2="12" y2="23" />
              <line x1="4.22" y1="4.22" x2="5.64" y2="5.64" />
              <line x1="18.36" y1="18.36" x2="19.78" y2="19.78" />
              <line x1="1" y1="12" x2="3" y2="12" />
              <line x1="21" y1="12" x2="23" y2="12" />
              <line x1="4.22" y1="19.78" x2="5.64" y2="18.36" />
              <line x1="18.36" y1="5.64" x2="19.78" y2="4.22" />
            </svg>
          )}
        </button>

        <div className="navbar__divider" />

        <div className="navbar__user-menu" ref={menuRef}>
          <button
            type="button"
            className="navbar__user-trigger"
            onClick={() => setMenuOpen((o) => !o)}
          >
            {user?.photoUrl ? (
              <img src={user.photoUrl} alt="" className="navbar__user-avatar-img" />
            ) : (
              <div className="navbar__user-avatar">
                {getInitials(user?.firstName, user?.lastName)}
              </div>
            )}
            <div className="navbar__user-info">
              <span className="navbar__user-name">
                {user?.firstName} {user?.lastName}
              </span>
              {roleLabel && <span className="navbar__user-role">{roleLabel}</span>}
            </div>
            <svg
              className={`navbar__chevron ${menuOpen ? 'navbar__chevron--open' : ''}`}
              width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"
            >
              <polyline points="6 9 12 15 18 9" />
            </svg>
          </button>

          {menuOpen && (
            <div className="navbar__dropdown">
              <div className="navbar__dropdown-header">
                <span className="navbar__dropdown-name">{user?.firstName} {user?.lastName}</span>
                <span className="navbar__dropdown-email">{user?.email}</span>
              </div>
              <button type="button" className="navbar__dropdown-item" onClick={logout}>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                  <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
                  <polyline points="16 17 21 12 16 7" />
                  <line x1="21" y1="12" x2="9" y2="12" />
                </svg>
                Déconnexion
              </button>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;