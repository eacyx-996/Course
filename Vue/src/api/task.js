import request from '@/utils/request'

// 实训任务模块 /task
export const createTask = (data) => request.post('/task', data)
export const updateTask = (taskId, data) => request.put(`/task/${taskId}`, data)
export const deleteTask = (taskId) => request.delete(`/task/${taskId}`)
export const pageTasks = (params) => request.get('/task/page', { params })
export const getTaskDetail = (taskId) => request.get(`/task/${taskId}`)
