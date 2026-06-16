import { useState } from 'react';
import { AuthContext } from './AuthContext';
import { logout as apiLogout } from '../api/authApi';

function readStoredUser() {
  const savedUser = localStorage.getItem('user');
  return savedUser ? JSON.parse(savedUser) : null;
}

export function AuthProvider({ children }) {
  const [user,  setUser]  = useState(readStoredUser);
  const [token, setToken] = useState(() => localStorage.getItem('token'));

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
      user, token, role, loading: false,
      loginUser, updateUser, logout,
      isAuthenticated: !!token,
    }}>
      {children}
    </AuthContext.Provider>
  );
}
