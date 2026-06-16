import request from '../utils/request'

export const getReminders = () => request.get('/rectification-reminders')
export const getReminder = (id) => request.get(`/rectification-reminders/${id}`)
export const getRemindersByStall = (stallId) => request.get(`/rectification-reminders/stall/${stallId}`)
export const getPendingReminders = () => request.get('/rectification-reminders/pending')
export const getSentReminders = () => request.get('/rectification-reminders/sent')
export const processDueReminders = () => request.post('/rectification-reminders/process')
