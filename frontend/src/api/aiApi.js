import api from './axiosConfig';

export const analyzeImage          = (imageId)    => api.post(`/ai/analyze/${imageId}`);
export const getDiseasesByParcelle = (parcelleId) => api.get(`/ai/diseases/parcelle/${parcelleId}`);
export const analyserDataset       = (parcelleId) => api.post(`/ai/analyse/dataset/${parcelleId}`);

export const analyserUpload = (parcelleId, file) => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('parcelleId', parcelleId);
  return api.post('/ai/analyse/upload', formData, {
    headers: { 'Content-Type': undefined }, // let the browser set multipart boundary
  });
};

export const getDroneSimulationStatus = () => api.get('/simulation/drone/status');
export const toggleDroneSimulation    = () => api.post('/simulation/drone/toggle');
