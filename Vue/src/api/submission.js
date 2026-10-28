import request from '@/utils/request'

// 作业提交模块 /submission
export const submitHomework = (formData) =>
  request.post('/submission', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
export const pageMySubmissions = (params) => request.get('/submission/my', { params })
export const pageSubmissions = (params) => request.get('/submission/page', { params })
