import api from './client'
import type { TaskList } from '../types/api'

export const listApi = {
  async getAll() {
    const { data } = await api.get<TaskList[]>('/lists')
    return data
  },

  async create(name: string) {
    const { data } = await api.post<TaskList>('/lists', { name })
    return data
  },
}
