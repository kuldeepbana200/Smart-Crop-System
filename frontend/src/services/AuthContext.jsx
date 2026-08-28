import { createContext, useContext, useState } from 'react';
import { login as loginRequest, register as registerRequest } from '../services/authService';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(localStorage.getItem('token'));

  const login = async (credentials) => {
    const data = await loginRequest(credentials);
    localStorage.setItem('token', data.token);
    setToken(data.token);
    return data;
  };

  const register = async (payload) => {
    return await registerRequest(payload);
  };

  const logout = () => {
    localStorage.removeItem('token');
    setToken(null);
  };

  return (
    <AuthContext.Provider value={{ token, isAuthenticated: !!token, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);