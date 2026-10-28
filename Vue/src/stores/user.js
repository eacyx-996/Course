import { defineStore } from 'pinia'
import { getUserInfo, login } from '@/api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: null
  }),
  getters: {
    role: (state) => state.userInfo?.role || '',
    isAdmin: (state) => state.userInfo?.role === 'admin',
    isTeacherOrAdmin: (state) =>
      ['admin', 'teacher'].includes(state.userInfo?.role)
  },
  actions: {
    async login(form) {
      const { data } = await login(form)
      this.token = data.token
      localStorage.setItem('token', data.token)
      this.userInfo = {
        userId: data.userId,
        username: data.username,
        realName: data.realName,
        role: data.role
      }
    },
    async fetchUserInfo() {
      const { data } = await getUserInfo()
      this.userInfo = data
    },
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
    }
  }
})
