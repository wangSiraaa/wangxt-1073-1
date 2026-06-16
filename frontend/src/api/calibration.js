import request from '../utils/request'

export const getCalibrations = () => request.get('/calibrations')
export const getCalibrationsByScale = (scaleId) => request.get(`/calibrations/scale/${scaleId}`)
export const createCalibration = (data) => request.post('/calibrations', data)
