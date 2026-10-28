import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

/**
 * axios 实例:统一 baseURL / Token 携带 / Result 解包
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    // 文件下载等二进制场景直接放行
    if (response.config.responseType === 'blob') {
      return response
    }
    const { code, message, data } = response.data
    if (code === 200) {
      return response.data
    }
    ElMessage.error(message || '请求失败')
    if (code === 401) {
      localStorage.removeItem('token')
      router.push('/login')
    }
    return Promise.reject(response.data)
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      router.push('/login')
    }
    ElMessage.error(error.response?.data?.message || '网络异常')
    return Promise.reject(error)
  }
)

export default request
