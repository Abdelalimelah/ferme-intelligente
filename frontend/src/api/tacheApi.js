import api from './axiosConfig';

export const getTaches = () => api.get('/taches');
export const getTacheById = (id) => api.get(`/taches/${id}`);
export const getTachesByAgriculteur = (agricId) => api.get(`/taches/agriculteur/${agricId}`);
export const getTachesByGestionnaire = (gestId) => api.get(`/taches/gestionnaire/${gestId}`);
export const createTache = (data) => api.post('/taches', data);
export const updateTache = (id, data) => api.put(`/taches/${id}`, data);
export const markTacheDemarree = (id) => api.put(`/taches/${id}/demarrer`);
export const markTacheTerminee = (id) => api.put(`/taches/${id}/terminer`);
