import apiClient from './axiosConfig';

export const categoryApi = {
  getAll: () =>
    apiClient.get('/api/categories'),

  create: (categoryData) =>
    apiClient.post('/api/categories', categoryData),

  update: (id, categoryData) =>
    apiClient.put(`/api/categories/${id}`, categoryData),

  delete: (id) =>
    apiClient.delete(`/api/categories/${id}`),
};