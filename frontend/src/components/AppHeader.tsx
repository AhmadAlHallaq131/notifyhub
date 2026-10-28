import { Bell, BellRing, CheckCheck, Menu, Plus } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
import { getApiErrorMessage } from '../api/client'
import { useNotifications } from '../context/NotificationsContext'
import { formatDateTime } from '../utils/dates'
import { IconButton } from './IconButton'

interface AppHeaderProps {
  onOpenSidebar: () => void
  onCreateTask: () => void
}

export function AppHeader({ onOpenSidebar, onCreateTask }: AppHeaderProps) {
  const [isNotificationsOpen, setIsNotificationsOpen] = useState(false)
  const [error, setError] = useState('')
  const notificationsRef = useRef<HTMLDivElement>(null)
  const { notifications, unreadCount, isLoading, markRead, markAllRead } = useNotifications()

  useEffect(() => {
    function closeOnOutsidePress(event: MouseEvent) {
      if (!notificationsRef.current?.contains(event.target as Node)) setIsNotificationsOpen(false)
    }

    function closeOnEscape(event: KeyboardEvent) {
      if (event.key === 'Escape') setIsNotificationsOpen(false)
    }

    document.addEventListener('mousedown', closeOnOutsidePress)
    window.addEventListener('keydown', closeOnEscape)
    return () => {
      document.removeEventListener('mousedown', closeOnOutsidePress)
      window.removeEventListener('keydown', closeOnEscape)
    }
  }, [])

  async function handleMarkRead(notificationId: number) {
    try {
      setError('')
      await markRead(notificationId)
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Unable to update this notification.'))
    }
  }

  async function handleMarkAllRead() {
    try {
      setError('')
      await markAllRead()
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Unable to update notifications.'))
    }
  }

  return (
    <header className="app-header">
      <div className="app-header-mobile-brand">
        <IconButton label="Open navigation" onClick={onOpenSidebar}><Menu size={21} /></IconButton>
        <strong>NotifyHub</strong>
      </div>
      <div className="app-header-actions">
        <button className="header-new-task" onClick={onCreateTask} type="button"><Plus size={17} /> <span>New task</span></button>
        <div className="notifications-menu" ref={notificationsRef}>
          <IconButton aria-expanded={isNotificationsOpen} className={`notification-trigger ${unreadCount ? 'has-unread' : ''}`} label={`Notifications${unreadCount ? `, ${unreadCount} unread` : ''}`} onClick={() => setIsNotificationsOpen((value) => !value)}>
            <Bell size={19} />
            {unreadCount > 0 && <span className="notification-badge">{unreadCount > 9 ? '9+' : unreadCount}</span>}
          </IconButton>

          {isNotificationsOpen && <section aria-label="Notifications" className="notifications-popover">
            <header>
              <div><strong>Notifications</strong><span>{unreadCount ? `${unreadCount} unread` : 'You are all caught up'}</span></div>
              {unreadCount > 0 && <button onClick={() => void handleMarkAllRead()} type="button"><CheckCheck size={16} /> Mark all read</button>}
            </header>
            {error && <p className="notifications-error" role="alert">{error}</p>}
            {isLoading ? <p className="notifications-loading">Loading notifications...</p> : notifications.length ? <div className="notifications-popover-list">{notifications.slice(0, 6).map((notification) => <button className={notification.read ? '' : 'is-unread'} key={notification.id} onClick={() => !notification.read && void handleMarkRead(notification.id)} type="button">
              <span className="popover-notification-icon"><BellRing size={16} /></span>
              <span><strong>{notification.title}</strong><small>{notification.message}</small><time>{formatDateTime(notification.createdAt)}</time></span>
            </button>)}</div> : <div className="notifications-popover-empty"><BellRing size={21} /><p>No notifications yet</p><span>Upcoming reminders will appear here.</span></div>}
          </section>}
        </div>
      </div>
    </header>
  )
}
