import request from '../utils/request'

export const getStallScaleUsage = (stallId) => request.get(`/stalls/${stallId}/scale-usage`)
