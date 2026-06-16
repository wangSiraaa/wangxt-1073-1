import request from '../utils/request'

export const getScaleBorrows = () => request.get('/scale-borrows')
export const getActiveBorrows = () => request.get('/scale-borrows/active')
export const borrowScale = (data) => request.post('/scale-borrows/borrow', data)
export const returnScale = (id, returnRemark) =>
  request.put(`/scale-borrows/${id}/return`, { returnRemark })
