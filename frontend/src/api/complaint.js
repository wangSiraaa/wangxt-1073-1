import request from '../utils/request'

export const getComplaints = () => request.get('/complaints')
export const getComplaint = (id) => request.get(`/complaints/${id}`)
export const createComplaint = (data) => request.post('/complaints', data)
export const investigateComplaint = (id) => request.put(`/complaints/${id}/investigate`)
export const establishComplaint = (id, handleResult) =>
  request.put(`/complaints/${id}/establish`, { handleResult })
export const rejectComplaint = (id, handleResult) =>
  request.put(`/complaints/${id}/reject`, { handleResult })
export const cancelComplaint = (id, reason) =>
  request.put(`/complaints/${id}/cancel`, { reason })
export const closeComplaint = (id) => request.put(`/complaints/${id}/close`)
