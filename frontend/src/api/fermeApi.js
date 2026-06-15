import api from './axiosConfig';

export const getFermes = () => api.get('/fermes');
export const getFermeById = (id) => api.get(`/fermes/${id}`);
export const getFermesByProprietaire = (propId) => api.get(`/fermes/proprietaire/${propId}`);
export const createFerme = (data) => api.post('/fermes', data);
export const updateFerme = (id, data) => api.put(`/fermes/${id}`, data);
export const deleteFerme = (id) => api.delete(`/fermes/${id}`);
