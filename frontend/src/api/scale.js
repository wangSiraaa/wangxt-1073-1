import request from '../utils/request'

export const getScales = () => request.get('/scales')
export const getScale = (id) => request.get(`/scales/${id}`)
export const getScalesByStall = (stallId) => request.get(`/scales/stall/${stallId}`)
export const getAvailableScales = () => request.get('/scales/available')
export const createScale = (data) => request.post('/scales', data)
export const updateScale = (id, data) => request.put(`/scales/${id}`, data)
export const deleteScale = (id) => request.delete(`/scales/${id}`)
export const bindScaleStall = (id, stallId) =>
  request.put(`/scales/${id}/bind-stall`, { stallId })
export const updateScaleQualified = (id, qualified) =>
  request.put(`/scales/${id}/business-qualified`, { qualified })
export const checkExpiredScales = () => request.post('/scales/check-expired')
