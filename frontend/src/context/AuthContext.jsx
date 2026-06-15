import { createContext, useState, useEffect } from 'react';
import { logout as apiLogout } from '../api/authApi';

export const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user,  setUser]  = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const savedToken   = localStorage.getItem('token');
    const savedUser    = localStorage.getItem('user');
    if (savedToken && savedUser) {
      setToken(savedToken);
      setUser(JSON.parse(savedUser));
    }
    setLoading(false);
  }, []);

  const loginUser = (tokenValue, refreshTokenValue, userData) => {
    localStorage.setItem('token',        tokenValue);
    localStorage.setItem('refreshToken', refreshTokenValue);
    localStorage.setItem('user',         JSON.stringify(userData));
    setToken(tokenValue);
    setUser(userData);
  };

  const updateUser = (userData) => {
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
  };

  const logout = async () => {
    const rt = localStorage.getItem('refreshToken');
    if (rt) {
      // Revoke refresh token on backend (fire-and-forget)
      apiLogout(rt).catch(() => {});
    }
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
  };

  const role = user?.role?.toLowerCase();

  return (
    <AuthContext.Provider value={{
      user, token, role, loading,
      loginUser, updateUser, logout,
      isAuthenticated: !!token,
    }}>
      {children}
    </AuthContext.Provider>
  );
}
