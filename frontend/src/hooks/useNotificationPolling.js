import { useEffect, useRef, useState } from 'react';
import { getPendingNotificationCount } from '../api/notificationApi';
import useNotification from './useNotification';
import useAuth from './useAuth';

const POLL_INTERVAL_MS = 15000;

const useNotificationPolling = () => {
  const { isAuthenticated } = useAuth();
  const { showNotification } = useNotification();
  const [counts, setCounts] = useState(null);
  const previousTotalRef = useRef(null);

  useEffect(() => {
    if (!isAuthenticated) return undefined;

    let cancelled = false;

    const poll = async () => {
      try {
        const data = await getPendingNotificationCount();
        if (cancelled) return;

        if (previousTotalRef.current !== null && data.total > previousTotalRef.current) {
          showNotification('Une nouvelle demande attend votre validation', 'info');
        }
        previousTotalRef.current = data.total;
        setCounts(data);
      } catch {
        // silencieux : un échec ponctuel de polling ne doit pas spammer l'UI
      }
    };


    poll();
    const intervalId = setInterval(poll, POLL_INTERVAL_MS);

    return () => {
      cancelled = true;
      clearInterval(intervalId);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isAuthenticated]);

  return counts;
};

export default useNotificationPolling;