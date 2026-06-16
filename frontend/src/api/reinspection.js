import request from '../utils/request'

export const getReinspections = () => request.get('/reinspections')
export const getReinspection = (id) => request.get(`/reinspections/${id}`)
export const createReinspection = (data) => request.post('/reinspections', data)
export const scheduleReinspection = (id, data) =>
  request.put(`/reinspections/${id}/schedule`, data)
export const startReinspection = (id) => request.put(`/reinspections/${id}/start`)
export const completeReinspection = (id, data) =>
  request.put(`/reinspections/${id}/complete`, data)
export const cancelReinspection = (id, reason) =>
  request.put(`/reinspections/${id}/cancel`, { reason })
