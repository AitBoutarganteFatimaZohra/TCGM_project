import axiosInstance from './axiosConfig';

export const getPendingNotificationCount = async () => {
  const response = await axiosInstance.get('/notifications/pending-count');
  return response.data;
};