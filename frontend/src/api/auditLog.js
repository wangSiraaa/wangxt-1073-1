import request from '../utils/request'

export const getAuditLogs = () => request.get('/audit-logs')
export const getAuditLogsByModule = (module) => request.get(`/audit-logs/module/${module}`)
