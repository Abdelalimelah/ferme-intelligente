import api from './axiosConfig';

export const getCapteurs = () => api.get('/capteurs');
export const getCapteurById = (id) => api.get(`/capteurs/${id}`);
export const getCapteursByParcelle = (parcelleId) => api.get(`/capteurs/parcelle/${parcelleId}`);
export const createCapteur = (data) => api.post('/capteurs', data);
export const updateCapteur = (id, data) => api.put(`/capteurs/${id}`, data);
export const deleteCapteur = (id) => api.delete(`/capteurs/${id}`);
