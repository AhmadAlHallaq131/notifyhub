import { Client } from '@stomp/stompjs'
import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { notificationApi } from '../api/notifications'
import { useAuth } from './AuthContext'
import type { Notification } from '../types/api'

interface NotificationsContextValue {
  notifications: Notification[]
  unreadCount: number
  isLoading: boolean
  refresh: () => Promise<void>
  markRead: (notificationId: number) => Promise<void>
  markAllRead: () => Promise<void>
}

const NotificationsContext = createContext<NotificationsContextValue | undefined>(undefined)

function getBrokerUrl() {
  const scheme = window.location.protocol === 'https:' ? 'wss' : 'ws'
  return `${scheme}://${window.location.host}/ws`
}

export function NotificationsProvider({ children }: { children: React.ReactNode }) {
  const { user } = useAuth()
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [isLoading, setIsLoading] = useState(false)

  const refresh = useCallback(async () => {
    if (!user) {
      setNotifications([])
      return
    }

    setIsLoading(true)
    try {
      setNotifications(await notificationApi.getAll())
    } finally {
      setIsLoading(false)
    }
  }, [user])

  useEffect(() => {
    void refresh()
  }, [refresh])

  useEffect(() => {
    const token = localStorage.getItem('notifyhub.accessToken')
    if (!user || !token) {
      return
    }

    const client = new Client({
      brokerURL: getBrokerUrl(),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 5000,
      debug: () => undefined,
      onConnect: () => {
        client.subscribe('/user/queue/notifications', (message) => {
          const notification = JSON.parse(message.body) as Notification
          setNotifications((current) => [notification, ...current.filter((item) => item.id !== notification.id)])
        })
      },
    })

    client.activate()
    return () => { void client.deactivate() }
  }, [user])

  const value = useMemo<NotificationsContextValue>(() => ({
    notifications,
    unreadCount: notifications.filter((notification) => !notification.read).length,
    isLoading,
    refresh,
    async markRead(notificationId) {
      const updated = await notificationApi.markRead(notificationId)
      setNotifications((current) => current.map((item) => item.id === updated.id ? updated : item))
    },
    async markAllRead() {
      const updated = await notificationApi.markAllRead()
      setNotifications(updated)
    },
  }), [isLoading, notifications, refresh])

  return <NotificationsContext.Provider value={value}>{children}</NotificationsContext.Provider>
}

export function useNotifications() {
  const context = useContext(NotificationsContext)
  if (!context) {
    throw new Error('useNotifications must be used inside NotificationsProvider')
  }
  return context
}
