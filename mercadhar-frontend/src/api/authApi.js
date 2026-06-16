import apiClient from './axiosConfig';

export const authApi = {
  register: (userData) =>
    apiClient.post('/api/auth/register', userData),

  login: (credentials) =>
    apiClient.post('/api/auth/login', credentials),
};