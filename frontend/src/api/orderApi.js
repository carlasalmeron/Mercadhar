import apiClient from './axiosConfig';

export const orderApi = {
  create: (orderData) =>
    apiClient.post('/api/orders', orderData),

  getMyOrders: () =>
    apiClient.get('/api/orders/my-orders'),

  getAll: () =>
    apiClient.get('/api/orders'),

  updateStatus: (id, status) =>
    apiClient.patch(`/api/orders/${id}/status`, { status }),

  cancel: (id) =>
    apiClient.patch(`/api/orders/${id}/cancel`),
};