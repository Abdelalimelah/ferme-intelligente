import api from './axiosConfig';

export const getDonneesByCapteur = (capteurId) => api.get(`/donnees-capteur/capteur/${capteurId}`);
export const getDonneesByRange = (capteurId, start, end) =>
  api.get(`/donnees-capteur/capteur/${capteurId}/range`, { params: { start, end } });
export const createDonneeCapteur = (data) => api.post('/donnees-capteur', data);
