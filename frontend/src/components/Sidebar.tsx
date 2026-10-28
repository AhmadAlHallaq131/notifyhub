import { useCallback, useEffect, useRef, useState } from 'react'
import { CalendarDays, CheckCircle2, ChevronLeft, CirclePlus, ClipboardList, Home, ListTodo, LogOut, Moon, Plus, Search, Sparkles, Star, Sun, X } from 'lucide-react'
import { NavLink, useNavigate } from 'react-router-dom'
import { getApiErrorMessage } from '../api/client'
import { listApi } from '../api/lists'
import { useAuth } from '../context/AuthContext'
import { taskApi } from '../api/tasks'
import { useTheme } from '../context/ThemeContext'
import type { TaskList } from '../types/api'
import { IconButton } from './IconButton'

interface SidebarProps {
  collapsed: boolean
  onToggle: () => void
}

const primaryLinks = [
  { to: '/my-day', label: 'My Day', icon: Sparkles },
  { to: '/important', label: 'Important', icon: Star },
  { to: '/tasks', label: 'Tasks', icon: ClipboardList },
  { to: '/planned', label: 'Planned', icon: CalendarDays },
  { to: '/completed', label: 'Completed', icon: CheckCircle2 },
]

export function Sidebar({ collapsed, onToggle }: SidebarProps) {
  const [lists, setLists] = useState<TaskList[]>([])
  const [isAddingList, setIsAddingList] = useState(false)
  const [listName, setListName] = useState('')
  const [error, setError] = useState('')
  const [query, setQuery] = useState('')
  const [counts, setCounts] = useState({ myDay: 0, important: 0, planned: 0 })
  const searchInputRef = useRef<HTMLInputElement>(null)
  const navigate = useNavigate()
  const { logout, user } = useAuth()
  const { theme, toggleTheme } = useTheme()

  useEffect(() => {
    listApi.getAll().then(setLists).catch(() => setError('Unable to load lists.'))
  }, [])

  const refreshCounts = useCallback(async () => {
    try {
      const [myDayTasks, importantTasks, openTasks] = await Promise.all([
      taskApi.getMyDay(),
      taskApi.getPage({ size: 100, priority: 'HIGH', status: 'PENDING' }),
      taskApi.getPage({ size: 100, status: 'PENDING' }),
      ])
      setCounts({
        myDay: myDayTasks.length,
        important: importantTasks.totalElements,
        planned: openTasks.content.filter((task) => task.dueDate).length,
      })
    } catch {
      // Sidebar counts are helpful metadata; task pages retain their own error handling.
    }
  }, [])

  useEffect(() => {
    void refreshCounts()
    window.addEventListener('notifyhub:tasks-changed', refreshCounts)
    return () => window.removeEventListener('notifyhub:tasks-changed', refreshCounts)
  }, [refreshCounts])

  useEffect(() => {
    function focusSearch() {
      searchInputRef.current?.focus()
    }
    window.addEventListener('notifyhub:focus-search', focusSearch)
    return () => window.removeEventListener('notifyhub:focus-search', focusSearch)
  }, [])

  async function addList(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!listName.trim()) {
      return
    }

    try {
      const createdList = await listApi.create(listName.trim())
      setLists((current) => [...current, createdList])
      setListName('')
      setIsAddingList(false)
      navigate(`/lists/${createdList.id}`)
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Unable to create this list.'))
    }
  }

  function search(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const trimmedQuery = query.trim()
    navigate(trimmedQuery ? `/search?q=${encodeURIComponent(trimmedQuery)}` : '/search')
  }

  function clearSearch() {
    setQuery('')
    searchInputRef.current?.focus()
  }

  return (
    <aside className={`sidebar ${collapsed ? 'is-collapsed' : ''}`}>
      <div className="sidebar-brand">
        <div className="brand-mark"><Home size={19} /></div>
        <span>NotifyHub</span>
        <IconButton label={collapsed ? 'Expand navigation' : 'Collapse navigation'} onClick={onToggle}><ChevronLeft size={19} /></IconButton>
      </div>

      <form className="sidebar-search" onSubmit={search}>
        <Search size={16} />
        <input aria-label="Search tasks" onChange={(event) => setQuery(event.target.value)} onKeyDown={(event) => { if (event.key === 'Escape') clearSearch() }} placeholder="Search tasks" ref={searchInputRef} value={query} />
        {query ? <IconButton className="search-clear" label="Clear search" onClick={clearSearch}><X size={14} /></IconButton> : <kbd>Ctrl K</kbd>}
      </form>

      <nav className="sidebar-nav" aria-label="Main navigation">
        {primaryLinks.map(({ to, label, icon: Icon }) => {
          const count = label === 'My Day' ? counts.myDay : label === 'Important' ? counts.important : label === 'Planned' ? counts.planned : null
          return (
          <NavLink className={({ isActive }) => `sidebar-link ${isActive ? 'is-active' : ''}`} key={to} to={to}>
            <Icon size={19} />
            <span>{label}</span>
            {count !== null && count > 0 && <small className="nav-count">{count > 99 ? '99+' : count}</small>}
          </NavLink>
        )})}
      </nav>

      <div className="sidebar-section">
        <div className="sidebar-section-title">
          <span>My lists</span>
          <IconButton label="Create a list" onClick={() => setIsAddingList(true)}><Plus size={18} /></IconButton>
        </div>

        {isAddingList && (
          <form className="new-list-form" onSubmit={addList}>
            <input autoFocus maxLength={100} onChange={(event) => setListName(event.target.value)} placeholder="List name" value={listName} />
            <button aria-label="Create list" type="submit"><CirclePlus size={18} /></button>
          </form>
        )}

        {lists.map((list) => (
          <NavLink className={({ isActive }) => `sidebar-link sidebar-list-link ${isActive ? 'is-active' : ''}`} key={list.id} to={`/lists/${list.id}`}>
            <ListTodo size={18} />
            <span>{list.name}</span>
          </NavLink>
        ))}
      </div>

      {error && <p className="sidebar-error">{error}</p>}

      <div className="sidebar-footer">
        <span className="account-avatar">{user?.email.slice(0, 1).toUpperCase() ?? 'N'}</span>
        <span className="account-email">{user?.email ?? 'NotifyHub account'}</span>
        <IconButton label="Sign out" onClick={logout}><LogOut size={17} /></IconButton>
      </div>
      <button className="theme-toggle" onClick={toggleTheme} type="button">{theme === 'light' ? <Moon size={17} /> : <Sun size={17} />}<span>{theme === 'light' ? 'Dark mode' : 'Light mode'}</span></button>
    </aside>
  )
}
