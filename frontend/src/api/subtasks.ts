import api from './client'
import type { SubTask } from '../types/api'

export const subTaskApi = {
  async getForTask(taskId: number) {
    const { data } = await api.get<SubTask[]>(`/tasks/${taskId}/subtasks`)
    return data
  },

  async create(taskId: number, title: string) {
    const { data } = await api.post<SubTask>(`/tasks/${taskId}/subtasks`, { title })
    return data
  },

  async complete(subTaskId: number) {
    const { data } = await api.patch<SubTask>(`/subtasks/${subTaskId}/complete`)
    return data
  },

  async reopen(subTaskId: number) {
    const { data } = await api.patch<SubTask>(`/subtasks/${subTaskId}/reopen`)
    return data
  },

  async remove(subTaskId: number) {
    await api.delete(`/subtasks/${subTaskId}`)
  },
}
