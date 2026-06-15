import api from './axiosConfig';

export const login        = (email, motDePasse) => api.post('/auth/login', { email, motDePasse });
export const register     = (data)              => api.post('/auth/register', data);
export const resetPassword= (data)              => api.post('/auth/reset-password', data);
export const refreshToken = (refreshToken)      => api.post('/auth/refresh', { refreshToken });
export const logout       = (refreshToken)      => api.post('/auth/logout',  { refreshToken });
