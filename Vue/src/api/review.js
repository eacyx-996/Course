import request from '@/utils/request'

// 批阅模块 /review
export const reviewSubmission = (data) => request.post('/review', data)
export const getReviewResult = (submissionId) =>
  request.get(`/review/submission/${submissionId}`)
export const getStatistics = (taskId, className) =>
  request.get('/review/statistics', { params: { taskId, className } })
