import api from './api';

export const register = async ({ name, email, phone, password }) => {
  const res = await api.post('/api/auth/register', { name, email, phone, password });
  return res.data;
};

export const login = async ({ email, password }) => {
  const res = await api.post('/api/auth/login', { email, password });
  return res.data; // expected to contain the JWT
};