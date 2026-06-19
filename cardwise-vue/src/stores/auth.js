import { defineStore } from 'pinia'
import api from '../api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || null,
    user: JSON.parse(localStorage.getItem('user') || 'null')
  }),
  getters: {
    isAuthenticated: (state) => !!state.token,
    userName: (state) => state.user?.name || '',
    userEmail: (state) => state.user?.email || ''
  },
  actions: {
    async login(email, password) {
      const { data } = await api.post('/auth/login', { email, password })
      this.token = data.token
      this.user = { userId: data.userId, email: data.email, name: data.name }
      localStorage.setItem('token', data.token)
      localStorage.setItem('user', JSON.stringify(this.user))
    },
    async register(email, password, name) {
      const { data } = await api.post('/auth/register', { email, password, name })
      this.token = data.token
      this.user = { userId: data.userId, email: data.email, name: data.name }
      localStorage.setItem('token', data.token)
      localStorage.setItem('user', JSON.stringify(this.user))
    },
    logout() {
      this.token = null
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})
