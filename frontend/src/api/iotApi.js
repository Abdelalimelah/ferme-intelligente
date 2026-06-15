import api from './axiosConfig';

export const getParcelleDetail = (id) => api.get(`/parcelles/${id}/detail`);
export const getSensorHistory = (parcelleId, capteurId) =>
  api.get(`/parcelles/${parcelleId}/capteurs/${capteurId}/history`);
export const sendSensorData = (data) => api.post('/iot/data', data);
export const sendBatchSensorData = (data) => api.post('/iot/data/batch', data);
