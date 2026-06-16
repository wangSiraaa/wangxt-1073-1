import request from '../utils/request'

export const getStalls = () => request.get('/stalls')
export const getStall = (id) => request.get(`/stalls/${id}`)
export const createStall = (data) => request.post('/stalls', data)
export const updateStall = (id, data) => request.put(`/stalls/${id}`, data)
export const deleteStall = (id) => request.delete(`/stalls/${id}`)
export const updateStallQualified = (id, qualified) =>
  request.put(`/stalls/${id}/business-qualified`, { qualified })
export const canStallOperate = (id) => request.get(`/stalls/${id}/can-operate`)
