import api from './client'
import type { Notification } from '../types/api'

export const notificationApi = {
  async getAll() {
    const { data } = await api.get<Notification[]>('/notifications')
    return data
  },

  async getUnread() {
    const { data } = await api.get<Notification[]>('/notifications/unread')
    return data
  },

  async markRead(notificationId: number) {
    const { data } = await api.patch<Notification>(`/notifications/${notificationId}/read`)
    return data
  },

  async markAllRead() {
    const { data } = await api.patch<Notification[]>('/notifications/read-all')
    return data
  },
}
