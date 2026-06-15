import api from './axiosConfig';

export const getUtilisateurs = () => api.get('/utilisateurs');
export const getUtilisateurById = (id) => api.get(`/utilisateurs/${id}`);
export const getUtilisateursByRole = (role) => api.get(`/utilisateurs/role/${role}`);
export const createUtilisateur = (data) => api.post('/utilisateurs', data);
export const updateUtilisateur = (id, data) => api.put(`/utilisateurs/${id}`, data);
export const deleteUtilisateur = (id) => api.delete(`/utilisateurs/${id}`);
export const assignGestionnaireToFerme = (userId, fermeId) => api.put(`/utilisateurs/${userId}/fermes/${fermeId}`);
export const assignAgriculteurToParcelle = (userId, parcelleId) => api.put(`/utilisateurs/${userId}/parcelles/${parcelleId}`);
