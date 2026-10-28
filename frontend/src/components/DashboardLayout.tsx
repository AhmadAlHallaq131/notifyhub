import { useEffect, useState } from 'react'
import { Outlet } from 'react-router-dom'
import { listApi } from '../api/lists'
import type { TaskList } from '../types/api'
import { AppHeader } from './AppHeader'
import { Sidebar } from './Sidebar'
import { MobileNav } from './MobileNav'
import { TaskDialog } from './TaskDialog'

export function DashboardLayout() {
  const [collapsed, setCollapsed] = useState(false)
  const [mobileOpen, setMobileOpen] = useState(false)
  const [isCreateTaskOpen, setIsCreateTaskOpen] = useState(false)
  const [lists, setLists] = useState<TaskList[]>([])

  useEffect(() => {
    function handleKeyDown(event: KeyboardEvent) {
      const target = event.target as HTMLElement | null
      const isTyping = target?.matches('input, textarea, select, [contenteditable="true"]')

      if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
        event.preventDefault()
        window.dispatchEvent(new Event('notifyhub:focus-search'))
      }

      if (!isTyping && event.key.toLowerCase() === 'n') {
        event.preventDefault()
        setIsCreateTaskOpen(true)
      }
    }

    window.addEventListener('keydown', handleKeyDown)
    return () => window.removeEventListener('keydown', handleKeyDown)
  }, [])

  useEffect(() => {
    if (isCreateTaskOpen) void listApi.getAll().then(setLists).catch(() => setLists([]))
  }, [isCreateTaskOpen])

  return (
    <div className="app-shell">
      <Sidebar collapsed={collapsed || mobileOpen} onToggle={() => setCollapsed((value) => !value)} />
      {mobileOpen && <button aria-label="Close navigation" className="mobile-scrim" onClick={() => setMobileOpen(false)} type="button" />}
      <div className="app-frame">
        <AppHeader onCreateTask={() => setIsCreateTaskOpen(true)} onOpenSidebar={() => setMobileOpen(true)} />
        <main className="page-content"><Outlet /></main>
        <MobileNav />
      </div>
      {isCreateTaskOpen && <TaskDialog lists={lists} onClose={() => setIsCreateTaskOpen(false)} onSaved={() => window.dispatchEvent(new Event('notifyhub:tasks-changed'))} task={null} />}
    </div>
  )
}
