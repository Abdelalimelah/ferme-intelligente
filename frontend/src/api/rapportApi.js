import api from './axiosConfig';

export const getRapports = () => api.get('/rapports');
export const getRapportById = (id) => api.get(`/rapports/${id}`);
export const getRapportsByAuteur = (auteurId) => api.get(`/rapports/auteur/${auteurId}`);
export const getRapportsByType = (type) => api.get(`/rapports/type/${type}`);
export const createRapport = (data) => api.post('/rapports', data);
export const updateRapportStatut = (id, statut) => api.put(`/rapports/${id}/statut`, null, { params: { statut } });
