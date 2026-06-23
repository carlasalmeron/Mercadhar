import apiClient from './axiosConfig';

export const productApi = {
  getAll: () =>
    apiClient.get('/api/products'),

  getAvailable: () =>
    apiClient.get('/api/products/available'),

  getByCategory: (categoryId) =>
    apiClient.get(`/api/products/category/${categoryId}`),

  getById: (id) =>
    apiClient.get(`/api/products/${id}`),

  create: (productData) =>
    apiClient.post('/api/products', productData),

  update: (id, productData) =>
    apiClient.put(`/api/products/${id}`, productData),

  toggleAvailability: (id) =>
    apiClient.patch(`/api/products/${id}/toggle-availability`),

  delete: (id) =>
    apiClient.delete(`/api/products/${id}`),
};