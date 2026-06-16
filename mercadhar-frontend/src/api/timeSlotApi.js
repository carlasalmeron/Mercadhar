import apiClient from './axiosConfig';

export const timeSlotApi = {
  getAvailable: (date) =>
    apiClient.get(`/api/timeslots/available?date=${date}`),

  getAll: (date) =>
    apiClient.get(`/api/timeslots?date=${date}`),

  create: (slotData) =>
    apiClient.post('/api/timeslots', slotData),

  delete: (id) =>
    apiClient.delete(`/api/timeslots/${id}`),
};