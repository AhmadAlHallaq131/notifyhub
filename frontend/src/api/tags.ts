import api from './client'
import type { Tag } from '../types/api'

export const tagApi = {
  async getAll() {
    const { data } = await api.get<Tag[]>('/tags')
    return data
  },

  async create(name: string) {
    const { data } = await api.post<Tag>('/tags', { name })
    return data
  },
}
