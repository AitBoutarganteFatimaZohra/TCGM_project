import { useContext } from 'react';
import NotificationContext from '../context/NotificationContext';

const useNotification = () => {
  const context = useContext(NotificationContext);
  
  if (!context) {
    throw new Error('useNotification doit être utilisé à l\'intérieur d\'un NotificationProvider');
  }
  
  return context;
};

export default useNotification;