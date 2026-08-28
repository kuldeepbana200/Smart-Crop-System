import api from './api';

export const getCurrentFarmer = async () => {
  const res = await api.get('/api/farmers/me');
  return res.data;
};

export const createFarmerProfile = async ({ district, state, latitude, longitude, landArea }) => {
  const res = await api.post('/api/farmers/profile', { district, state, latitude, longitude, landArea });
  return res.data;
};