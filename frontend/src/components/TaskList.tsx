import { CalendarDays, Circle, MoreHorizontal, Star, Sun, Trash2 } from 'lucide-react'
import { type ReactNode, useEffect, useRef, useState } from 'react'
import type { Task } from '../types/api'
import { formatTaskDate } from '../utils/dates'
import { IconButton } from './IconButton'

interface TaskListProps {
  tasks: Task[]
  emptyTitle: string
  emptyMessage: string
  onOpen: (task: Task) => void
  onToggle: (task: Task) => void
  onToggleImportant?: (task: Task) => void
  onToggleMyDay?: (task: Task) => void
  onDelete?: (task: Task) => void
  query?: string
  emptyAction?: ReactNode
}

function HighlightedText({ text, query }: { text: string; query?: string }) {
  if (!query?.trim()) return text
  const escapedQuery = query.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const parts = text.split(new RegExp(`(${escapedQuery})`, 'ig'))
  return <>{parts.map((part, index) => part.toLowerCase() === query.toLowerCase() ? <mark key={`${part}-${index}`}>{part}</mark> : part)}</>
}

export function TaskList({ tasks, emptyTitle, emptyMessage, onOpen, onToggle, onToggleImportant, onToggleMyDay, onDelete, query, emptyAction }: TaskListProps) {
  const [menuTaskId, setMenuTaskId] = useState<number | null>(null)
  const taskListRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    function closeMenuOnOutsidePress(event: MouseEvent) {
      if (!taskListRef.current?.contains(event.target as Node)) setMenuTaskId(null)
    }

    function closeMenuOnEscape(event: KeyboardEvent) {
      if (event.key === 'Escape') setMenuTaskId(null)
    }

    document.addEventListener('mousedown', closeMenuOnOutsidePress)
    window.addEventListener('keydown', closeMenuOnEscape)
    return () => {
      document.removeEventListener('mousedown', closeMenuOnOutsidePress)
      window.removeEventListener('keydown', closeMenuOnEscape)
    }
  }, [])

  if (!tasks.length) {
    return (
      <div className="empty-state">
        <div className="empty-state-icon"><Star size={24} /></div>
        <h3>{emptyTitle}</h3>
        <p>{emptyMessage}</p>
        {emptyAction}
      </div>
    )
  }

  return (
    <div className="task-list" ref={taskListRef}>
      {tasks.map((task) => {
        const dueLabel = formatTaskDate(task.dueDate)
        const isComplete = task.status === 'COMPLETED'

        return (
          <article className={`task-row ${isComplete ? 'is-complete' : ''} ${menuTaskId === task.id ? 'is-menu-open' : ''}`} key={task.id}>
            <button aria-label={isComplete ? `Reopen ${task.title}` : `Complete ${task.title}`} className="task-check" onClick={() => onToggle(task)} type="button">
              {isComplete ? <span>✓</span> : <Circle size={21} strokeWidth={1.8} />}
            </button>
            <button className="task-content" onClick={() => onOpen(task)} type="button">
              <strong><HighlightedText query={query} text={task.title} /></strong>
              {task.notes && <span><HighlightedText query={query} text={task.notes} /></span>}
            </button>
            <div className="task-meta">
              {dueLabel && <span className={dueLabel === 'Today' ? 'is-today' : ''}><CalendarDays size={15} />{dueLabel}</span>}
              {onToggleImportant && <IconButton className={`importance-button ${task.priority === 'HIGH' ? 'is-important' : ''}`} label={task.priority === 'HIGH' ? `Remove importance from ${task.title}` : `Mark ${task.title} as important`} onClick={() => onToggleImportant(task)}><Star fill={task.priority === 'HIGH' ? 'currentColor' : 'none'} size={18} /></IconButton>}
              <div className="task-menu-wrap">
                <IconButton label={`More actions for ${task.title}`} onClick={() => setMenuTaskId((current) => current === task.id ? null : task.id)}><MoreHorizontal size={18} /></IconButton>
                {menuTaskId === task.id && <div className="task-context-menu">
                  <button onClick={() => { onOpen(task); setMenuTaskId(null) }} type="button">Open details</button>
                  {onToggleMyDay && <button onClick={() => { onToggleMyDay(task); setMenuTaskId(null) }} type="button"><Sun size={15} /> {task.myDayDate ? 'Remove from My Day' : 'Add to My Day'}</button>}
                  {onToggleImportant && <button onClick={() => { onToggleImportant(task); setMenuTaskId(null) }} type="button"><Star size={15} /> {task.priority === 'HIGH' ? 'Remove importance' : 'Mark important'}</button>}
                  {onDelete && <button className="context-delete" onClick={() => { onDelete(task); setMenuTaskId(null) }} type="button"><Trash2 size={15} /> Delete task</button>}
                </div>}
              </div>
            </div>
          </article>
        )
      })}
    </div>
  )
}
