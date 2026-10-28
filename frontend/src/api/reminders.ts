import api from './client'
import type { Reminder, ReminderRepeatFrequency } from '../types/api'

interface ReminderPayload {
  remindAt: string
  repeatFrequency: ReminderRepeatFrequency
}

export const reminderApi = {
  async getForTask(taskId: number) {
    const { data } = await api.get<Reminder[]>(`/tasks/${taskId}/reminders`)
    return data
  },

  async create(taskId: number, payload: ReminderPayload) {
    const { data } = await api.post<Reminder>(`/tasks/${taskId}/reminders`, payload)
    return data
  },

  async update(reminderId: number, payload: ReminderPayload) {
    const { data } = await api.put<Reminder>(`/reminders/${reminderId}`, payload)
    return data
  },

  async remove(reminderId: number) {
    await api.delete(`/reminders/${reminderId}`)
  },
}
