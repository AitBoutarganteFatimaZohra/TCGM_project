import { createContext, useState, useCallback } from 'react';

const NotificationContext = createContext(null);

let idCounter = 0;

export const NotificationProvider = ({ children }) => {
  const [notifications, setNotifications] = useState([]);

  const removeNotification = useCallback((id) => {
    setNotifications((prev) => prev.filter((n) => n.id !== id));
  }, []);

  const showNotification = useCallback(
    (message, type = 'info', duration = 4000) => {
      const id = ++idCounter;
      setNotifications((prev) => [...prev, { id, message, type }]);

      if (duration > 0) {
        setTimeout(() => {
          removeNotification(id);
        }, duration);
      }
      return id;
    },
    [removeNotification]
  );

  return (
    <NotificationContext.Provider value={{ notifications, showNotification, removeNotification }}>
      {children}
      <div className="notification-container">
        {notifications.map((n) => (
          <div key={n.id} className={`notification notification--${n.type}`}>
            <span>{n.message}</span>
            <button
              className="notification__close"
              onClick={() => removeNotification(n.id)}
              aria-label="Fermer"
            >
              ×
            </button>
          </div>
        ))}
      </div>
    </NotificationContext.Provider>
  );
};

export default NotificationContext;