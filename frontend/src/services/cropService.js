import api from './api';

export const getCrops = async () => {
  const res = await api.get('/api/crops');
  return res.data;
};

export const getCropById = async (id) => {
  const res = await api.get(`/api/crops/${id}`);
  return res.data;
};

export const createCrop = async ({ cropName, cropStage, sowingDate, expectedHarvestDate }) => {
  const res = await api.post('/api/crops', { cropName, cropStage, sowingDate, expectedHarvestDate });
  return res.data;
};

export const updateCrop = async (id, { cropName, cropStage, sowingDate, expectedHarvestDate }) => {
  const res = await api.put(`/api/crops/${id}`, { cropName, cropStage, sowingDate, expectedHarvestDate });
  return res.data;
};

export const deleteCrop = async (id) => {
  await api.delete(`/api/crops/${id}`);
};