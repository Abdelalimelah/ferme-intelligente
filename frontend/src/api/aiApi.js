import api from './axiosConfig';

export const analyzeImage = (imageId) => api.post(`/ai/analyze/${imageId}`);
export const getDiseasesByParcelle = (parcelleId) => api.get(`/ai/diseases/parcelle/${parcelleId}`);
