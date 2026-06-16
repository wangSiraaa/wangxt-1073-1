import request from '../utils/request'

export const getSuspensions = () => request.get('/suspensions')
export const getSuspensionsByStall = (stallId) => request.get(`/suspensions/stall/${stallId}`)
export const getActiveSuspension = (stallId) => request.get(`/suspensions/stall/${stallId}/active`)
export const suspendStall = (data) => request.post('/suspensions', data)
export const reopenStall = (id, reopenReason) =>
  request.put(`/suspensions/${id}/reopen`, { reopenReason })
