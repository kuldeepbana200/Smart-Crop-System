import { createContext, useContext, useState } from 'react';
import { login as loginRequest, register as registerRequest } from '../services/authService';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [role, setRole] = useState(localStorage.getItem('role') || null);

  const login = async (credentials) => {
    const data = await loginRequest(credentials);
    localStorage.setItem('token', data.token);
    // Backend currently only issues FARMER accounts; default until officer/admin roles exist
    const userRole = data.role || 'FARMER';
    localStorage.setItem('role', userRole);
    setToken(data.token);
    setRole(userRole);
    return data;
  };

  const register = async (payload) => {
    return await registerRequest(payload);
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    setToken(null);
    setRole(null);
  };

  return (
    <AuthContext.Provider value={{ token, role, isAuthenticated: !!token, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);