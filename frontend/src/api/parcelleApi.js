import api from './axiosConfig';

export const getParcelles = () => api.get('/parcelles');
export const getParcelleById = (id) => api.get(`/parcelles/${id}`);
export const getParcellesByFerme = (fermeId) => api.get(`/parcelles/ferme/${fermeId}`);
export const createParcelle = (data) => api.post('/parcelles', data);
export const updateParcelle = (id, data) => api.put(`/parcelles/${id}`, data);
export const deleteParcelle = (id) => api.delete(`/parcelles/${id}`);
