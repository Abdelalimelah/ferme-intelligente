import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
});

// ── Request interceptor: attach JWT ──────────────────────────
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// ── Response interceptor: auto-refresh on 401 ────────────────
let isRefreshing = false;
let failedQueue  = [];   // pending requests while refreshing

const processQueue = (error, newToken = null) => {
  failedQueue.forEach(({ resolve, reject }) => {
    if (error) reject(error);
    else       resolve(newToken);
  });
  failedQueue = [];
};

// Public auth endpoints — a 401 here means "wrong credentials", not "your
// session expired". Let the caller's own .catch() show that, don't redirect.
const PUBLIC_AUTH_PATHS = ['/auth/login', '/auth/register', '/auth/refresh'];

api.interceptors.response.use(
  response => response,
  async error => {
    const original = error.config;
    const isPublicAuthRequest = PUBLIC_AUTH_PATHS.some(path => original?.url?.includes(path));

    // Only attempt refresh for 401 errors that haven't already retried,
    // and never for the public auth endpoints themselves.
    if (error.response?.status === 401 && !original._retry && !isPublicAuthRequest) {

      // If already refreshing, queue this request until refresh completes
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then(newToken => {
          original.headers.Authorization = `Bearer ${newToken}`;
          return api(original);
        });
      }

      original._retry = true;
      isRefreshing    = true;

      const refreshToken = localStorage.getItem('refreshToken');
      if (!refreshToken) {
        // No refresh token → hard logout
        clearAuthAndRedirect();
        return Promise.reject(error);
      }

      try {
        // Use a raw axios call (not `api`) to avoid interceptor loop
        const { data } = await axios.post(
          `${import.meta.env.VITE_API_URL || 'http://localhost:8080/api'}/auth/refresh`,
          { refreshToken }
        );
        localStorage.setItem('token',        data.token);
        localStorage.setItem('refreshToken', data.refreshToken);
        api.defaults.headers.common.Authorization = `Bearer ${data.token}`;

        processQueue(null, data.token);
        original.headers.Authorization = `Bearer ${data.token}`;
        return api(original);

      } catch (refreshError) {
        processQueue(refreshError, null);
        clearAuthAndRedirect();
        return Promise.reject(refreshError);

      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

function clearAuthAndRedirect() {
  localStorage.removeItem('token');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('user');
  window.location.href = '/login';
}

export default api;
