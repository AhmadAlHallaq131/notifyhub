import api from './client'
import type { PageResponse, Task, TaskPayload, TaskPriority, TaskStatus } from '../types/api'

export interface TaskPageParams {
  page?: number
  size?: number
  sortBy?: string
  direction?: 'ASC' | 'DESC'
  status?: TaskStatus
  priority?: TaskPriority
  taskListId?: number
  q?: string
}

export const taskApi = {
  async getPage(params: TaskPageParams = {}) {
    const { data } = await api.get<PageResponse<Task>>('/tasks/page', { params })
    return data
  },

  async getMyDay() {
    const { data } = await api.get<Task[]>('/tasks/my-day')
    return data
  },

  async search(query: string) {
    const { data } = await api.get<Task[]>('/tasks/search', { params: { q: query } })
    return data
  },

  async create(payload: TaskPayload) {
    const { data } = await api.post<Task>('/tasks', payload)
    return data
  },

  async update(taskId: number, payload: TaskPayload) {
    const { data } = await api.put<Task>(`/tasks/${taskId}`, payload)
    return data
  },

  async complete(taskId: number) {
    const { data } = await api.patch<Task>(`/tasks/${taskId}/complete`)
    return data
  },

  async reopen(taskId: number) {
    const { data } = await api.patch<Task>(`/tasks/${taskId}/reopen`)
    return data
  },

  async addToMyDay(taskId: number) {
    const { data } = await api.patch<Task>(`/tasks/${taskId}/my-day`)
    return data
  },

  async removeFromMyDay(taskId: number) {
    const { data } = await api.delete<Task>(`/tasks/${taskId}/my-day`)
    return data
  },

  async remove(taskId: number) {
    await api.delete(`/tasks/${taskId}`)
  },

  async addTag(taskId: number, tagId: number) {
    const { data } = await api.post<Task>(`/tasks/${taskId}/tags/${tagId}`)
    return data
  },

  async removeTag(taskId: number, tagId: number) {
    const { data } = await api.delete<Task>(`/tasks/${taskId}/tags/${tagId}`)
    return data
  },
}
