import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Bell } from 'lucide-react';
import useNotificationPolling from '../../hooks/useNotificationPolling';

const CATEGORY_LINKS = {
  tachesEnAttente: { label: 'Tâches en attente de validation', path: '/taches' },
  affectationsEnAttente: { label: 'Affectations en attente de validation', path: '/affectations' },
  ressourcesEnAttente: { label: 'Ressources en attente de validation', path: '/ressources' },
  sitesEnAttente: { label: 'Chantiers avec modification en attente', path: '/chantiers' },
};

const NotificationBell = () => {
  const counts = useNotificationPolling();
  const [open, setOpen] = useState(false);
  const ref = useRef(null);
  const navigate = useNavigate();

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (ref.current && !ref.current.contains(e.target)) {
        setOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const total = counts?.total ?? 0;

  const activeCategories = Object.entries(CATEGORY_LINKS).filter(
    ([key]) => (counts?.[key] ?? 0) > 0
  );

  return (
    <div ref={ref} style={{ position: 'relative' }}>
      <button
        type="button"
        className="navbar__icon-btn"
        onClick={() => setOpen((o) => !o)}
        aria-label="Notifications"
        title="Notifications"
        style={{ position: 'relative' }}
      >
        <Bell size={19} strokeWidth={1.8} />
        {total > 0 && (
          <span
            style={{
              position: 'absolute',
              top: -4,
              right: -4,
              background: '#dc2626',
              color: 'white',
              borderRadius: 999,
              fontSize: 11,
              lineHeight: '16px',
              minWidth: 16,
              height: 16,
              padding: '0 4px',
              textAlign: 'center',
              fontWeight: 600,
            }}
          >
            {total > 99 ? '99+' : total}
          </span>
        )}
      </button>

      {open && (
        <div className="navbar__dropdown" style={{ right: 0, minWidth: 280 }}>
          <div className="navbar__dropdown-header">
            <span className="navbar__dropdown-name">Notifications</span>
          </div>
          {activeCategories.length === 0 ? (
            <div style={{ padding: '12px 16px', fontSize: 13, color: '#8b8580' }}>
              Rien en attente de validation.
            </div>
          ) : (
            activeCategories.map(([key, { label, path }]) => (
              <button
                key={key}
                type="button"
                className="navbar__dropdown-item"
                onClick={() => {
                  setOpen(false);
                  navigate(path);
                }}
              >
                <span style={{ flex: 1, textAlign: 'left' }}>{label}</span>
                <span
                  style={{
                    background: '#fef3c7',
                    color: '#b45309',
                    borderRadius: 999,
                    fontSize: 12,
                    fontWeight: 600,
                    padding: '2px 8px',
                    marginLeft: 8,
                  }}
                >
                  {counts[key]}
                </span>
              </button>
            ))
          )}
        </div>
      )}
    </div>
  );
};

export default NotificationBell;