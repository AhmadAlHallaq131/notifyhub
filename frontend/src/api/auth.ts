import api from './client'
import type { LoginResponse, User } from '../types/api'

export const authApi = {
  async login(email: string, password: string) {
    const { data } = await api.post<LoginResponse>('/auth/login', { email, password })
    return data
  },

  async register(email: string, password: string) {
    const { data } = await api.post<User>('/auth/register', { email, password })
    return data
  },

  async getCurrentUser() {
    const { data } = await api.get<User>('/auth/me')
    return data
  },
}
