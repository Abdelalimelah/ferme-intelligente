import api from './axiosConfig';

export const getOwnerStats = (userId) => api.get(`/dashboard/owner/${userId}`);
export const getManagerStats = (userId) => api.get(`/dashboard/manager/${userId}`);
export const getWorkerStats = (userId) => api.get(`/dashboard/worker/${userId}`);
