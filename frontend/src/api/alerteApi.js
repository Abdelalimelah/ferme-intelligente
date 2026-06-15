import api from './axiosConfig';

export const getAlertes = () => api.get('/alertes');
export const getAlertesUnread = () => api.get('/alertes/unread');
export const getAlertesUnreadCount = () => api.get('/alertes/unread/count');
export const markAlerteAsRead = (id) => api.put(`/alertes/${id}/read`);
export const createAlerte = (data) => api.post('/alertes', data);
