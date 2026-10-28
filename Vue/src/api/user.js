import request from '@/utils/request'

// 用户模块 /user
export const login = (data) => request.post('/user/login', data)
export const register = (data) => request.post('/user/register', data)
export const getUserInfo = () => request.get('/user/info')
export const pageUsers = (params) => request.get('/user/page', { params })
export const updateUserStatus = (userId, status) =>
  request.put(`/user/${userId}/status`, { status })
